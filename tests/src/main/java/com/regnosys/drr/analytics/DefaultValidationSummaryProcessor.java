package com.regnosys.drr.analytics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.io.Resources;
import com.google.inject.Injector;
import com.regnosys.rosetta.common.transform.TestPackModel;
import com.regnosys.rosetta.common.transform.TestPackUtils;
import com.regnosys.rosetta.common.validation.RosettaTypeValidator;
import com.regnosys.rosetta.common.validation.ValidationReport;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.validation.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DefaultValidationSummaryProcessor<IN extends RosettaModelObject> implements ValidationSummaryProcessor<IN> {

    private final Logger LOGGER = LoggerFactory.getLogger(DefaultValidationSummaryProcessor.class);

    private final Injector injector;
    private final RosettaTypeValidator typeValidator;
    private final ObjectMapper objectMapper;
    private final ObjectWriter xmlObjectWriter;
    private final Validator xsdValidator;

    public DefaultValidationSummaryProcessor(Injector injector, ObjectMapper defaultObjectMapper, ObjectWriter outputObjectWriter, RosettaTypeValidator typeValidator, Validator xsdValidator) {
        this.injector = injector;
        this.typeValidator = typeValidator;
        this.objectMapper = defaultObjectMapper;
        this.xmlObjectWriter = outputObjectWriter;
        this.xsdValidator = xsdValidator;
    }

    @Override
    public List<ValidationData> processValidation(Class<?> functionType, Class<IN> inputType, List<TestPackModel> pipelineTestPackModels) {
        Function<IN, RosettaModelObject> transformFunction = getTransformFunction(functionType, inputType);
        return getValidationData(functionType.getName(),
                transformFunction,
                inputType,
                pipelineTestPackModels);
    }

    private Function<IN, RosettaModelObject> getTransformFunction(Class<?> functionType, Class<IN> inputType) {
        Object functionInstance = injector.getInstance(functionType);
        Method evaluateMethod;
        try {
            evaluateMethod = functionInstance.getClass().getMethod("evaluate", inputType);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(String.format("Evaluate method with input type %s not found", inputType.getName()), e);
        }
        return (resolvedInput) -> {
            try {
                return (RosettaModelObject) evaluateMethod.invoke(functionInstance, resolvedInput);
            } catch (InvocationTargetException e) {
                Throwable targetException = e.getTargetException();
                throw new RuntimeException(String.format("Failed to invoke function %s. Caused by %s", functionType, targetException.getMessage()), targetException);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(String.format("Failed to invoke function %s", functionType), e);
            }
        };
    }

    private List<ValidationData> getValidationData(String functionName, Function<IN, RosettaModelObject> function, Class<IN> inputType, List<TestPackModel> pipelineTestPackModels) {
        List<ValidationData> validationDataList = new ArrayList<>();
        for (TestPackModel testPackModel : pipelineTestPackModels) {
            String testPack = testPackModel.getName();

            for (TestPackModel.SampleModel model : testPackModel.getSamples()) {
                // For each test pack sample
                String inputFile = model.getInputPath();
                URL inputFileUrl = Resources.getResource(inputFile);
                String sampleName = getFileName(inputFileUrl);
                LOGGER.info("Validating projection {}, test pack {}, sample {}", functionName, testPack, sampleName);

                IN transactionReport = getInputObject(inputFileUrl, inputType);
                if (transactionReport == null) {
                    continue;
                }

                TypeValidationData reportValidation;
                try {
                    // Transaction report Rosetta validation
                    ValidationReport transactionValidation =
                            typeValidator.runProcessStep(transactionReport.getClass(), transactionReport);

                    reportValidation = getReportValidation(transactionValidation);
                } catch (Exception e) {
                    LOGGER.error("Exception occurred generating report validation data", e);
                    reportValidation = new TypeValidationData(e);
                }
                
                TypeValidationData isoReportValidation;
                String xsdSchemaValidationErrors;

                try {
                    // Run projection
                    RosettaModelObject isoReport = function.apply(transactionReport);
                    // ISO report Rosetta validation
                    ValidationReport isoDocumentValidation =
                            typeValidator.runProcessStep(isoReport.getClass(), isoReport);

                    isoReportValidation = getReportValidation(isoDocumentValidation);

                    // XSD validation
                    xsdSchemaValidationErrors = getXsdSchemaValidationErrors(isoReport);
                } catch (Exception e) {
                    LOGGER.error("Exception occurred generating project validation data", e);
                    isoReportValidation = new TypeValidationData(e);
                    xsdSchemaValidationErrors = null;
                }

                ValidationData validationData = new ValidationData(functionName,
                        testPack,
                        sampleName,
                        reportValidation,
                        isoReportValidation,
                        xsdSchemaValidationErrors);
                validationDataList.add(validationData);
            }
        }
        return validationDataList;
    }

    private IN getInputObject(URL inputFileUrl, Class<IN> inputType) {
        try {
            return TestPackUtils.readFile(inputFileUrl, objectMapper, inputType);
        } catch (Exception e) {
            LOGGER.warn("Failed to read input file {} of type {}", inputFileUrl, inputType);
            return null;
        }
    }

    private String getFileName(URL inputFileUrl) {
        try {
            return Path.of(inputFileUrl.toURI()).getFileName().toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private TypeValidationData getReportValidation(ValidationReport reportValidation) {
        AttributeValidationData cardinalityValidationData = getAttributeValidationData(reportValidation, ValidationResult.ValidationType.CARDINALITY);
        AttributeValidationData typeFormatValidationData = getAttributeValidationData(reportValidation, ValidationResult.ValidationType.TYPE_FORMAT);
        ConditionValidationData conditionValidationData = getConditionValidationData(reportValidation);
        return new TypeValidationData(cardinalityValidationData, typeFormatValidationData, conditionValidationData);
    }

    private AttributeValidationData getAttributeValidationData(ValidationReport reportValidation, ValidationResult.ValidationType validationType) {
        int totalCount =
                (int) reportValidation.results()
                        .stream()
                        .filter(r -> r.getValidationType() == validationType)
                        .count();
        List<ValidationResult<?>> failures = reportValidation.validationFailures();
        List<String> failedAttributeNames = failures.stream()
                .filter(r -> r.getValidationType() == validationType)
                .map(r -> r.getFailureReason().get())
                .map(this::getAttributeName)
                .sorted()
                .collect(Collectors.toList());
        return new AttributeValidationData(failedAttributeNames.size(), failedAttributeNames, totalCount);
    }

    private ConditionValidationData getConditionValidationData(ValidationReport reportValidation) {
        int totalConditionsCount =
                (int) reportValidation.results()
                        .stream()
                        .filter(r -> r.getValidationType() == ValidationResult.ValidationType.DATA_RULE)
                        .count();
        List<String> failedConditions =
                reportValidation.validationFailures()
                        .stream()
                        .filter(r -> r.getValidationType() == ValidationResult.ValidationType.DATA_RULE)
                        .map(ValidationResult::getName)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
        return new ConditionValidationData(failedConditions.size(), failedConditions, totalConditionsCount);
    }

    private String getAttributeName(String reason) {
        try {
            String substring = reason.substring(reason.indexOf("'") + 1);
            return substring.substring(0, substring.indexOf("'"));
        } catch (Exception e) {
            LOGGER.error("Failed to get attribute name from reason {}", reason, e);
            throw e;
        }

    }

    private String getXsdSchemaValidationErrors(RosettaModelObject isoReport) {
        String actualXml;
        try {
            actualXml = xmlObjectWriter.writeValueAsString(isoReport);
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to serialise to xml", e);
            throw new RuntimeException(e);
        }
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(actualXml.getBytes(StandardCharsets.UTF_8))) {
            xsdValidator.validate(new StreamSource(inputStream));
            return null;
        } catch (SAXException e) {
            // Schema validation errors
            return e.getMessage();
        } catch (IOException e) {
            LOGGER.error("Failed to validate against xsd", e);
            throw new RuntimeException(e);
        }
    }
}
