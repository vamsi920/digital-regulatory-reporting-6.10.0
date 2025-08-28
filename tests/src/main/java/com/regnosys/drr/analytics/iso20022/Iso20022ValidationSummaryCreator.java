package com.regnosys.drr.analytics.iso20022;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.io.Resources;
import com.google.inject.Injector;
import com.regnosys.drr.analytics.DefaultValidationSummaryProcessor;
import com.regnosys.drr.analytics.TransformData;
import com.regnosys.drr.analytics.ValidationData;
import com.regnosys.drr.analytics.ValidationSummaryProcessor;
import com.regnosys.rosetta.common.serialisation.RosettaObjectMapper;
import com.regnosys.rosetta.common.transform.PipelineModel;
import com.regnosys.rosetta.common.transform.TestPackModel;
import com.regnosys.rosetta.common.transform.TestPackUtils;
import com.regnosys.rosetta.common.validation.RosettaTypeValidator;
import com.rosetta.model.lib.RosettaModelObject;
import drr.projection.dtcc.rds.harmonized.csa.rewrite.trade.functions.Project_CSATradeReportToDtccRdsHarmonized;
import drr.projection.dtcc.rds.harmonized.csa.rewrite.trade.functions.Project_CsaPpdReportToDtccRdsHarmonized;
import drr.projection.dtcc.rds.harmonized.csa.rewrite.valuation.functions.Project_CSAValuationReportToDtccRdsHarmonized;
import drr.projection.iso20022.asic.rewrite.margin.functions.Project_ASICMarginReportToIso20022;
import drr.projection.iso20022.asic.rewrite.trade.functions.Project_ASICTradeReportToIso20022;
import drr.projection.iso20022.esma.emir.refit.margin.functions.Project_EsmaEmirMarginReportToIso20022;
import drr.projection.iso20022.esma.emir.refit.trade.functions.Project_EsmaEmirTradeReportToIso20022;
import drr.projection.iso20022.fca.ukemir.refit.margin.functions.Project_FcaUkEmirMarginReportToIso20022;
import drr.projection.iso20022.fca.ukemir.refit.trade.functions.Project_FcaUkEmirTradeReportToIso20022;
import drr.projection.iso20022.hkma.rewrite.margin.dtcc.functions.Project_HKMADtccMarginReportToIso20022;
import drr.projection.iso20022.hkma.rewrite.margin.tr.functions.Project_HKMATrMarginReportToIso20022;
import drr.projection.iso20022.hkma.rewrite.trade.dtcc.functions.Project_HKMADtccTradeReportToIso20022;
import drr.projection.iso20022.hkma.rewrite.trade.tr.functions.Project_HKMATrTradeReportToIso20022;
import drr.projection.iso20022.hkma.rewrite.valuation.dtcc.functions.Project_HKMADtccValuationReportToIso20022;
import drr.projection.iso20022.hkma.rewrite.valuation.tr.functions.Project_HKMATrValuationReportToIso20022;
import drr.projection.iso20022.jfsa.rewrite.margin.functions.Project_JFSARewriteMarginReportToIso20022;
import drr.projection.iso20022.jfsa.rewrite.trade.functions.Project_JFSARewriteTradeReportToIso20022;
import drr.projection.iso20022.mas.rewrite.margin.functions.Project_MASMarginReportToIso20022;
import drr.projection.iso20022.mas.rewrite.trade.functions.Project_MASTradeReportToIso20022;
import iso20022.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import jakarta.inject.Inject;
import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Iso20022ValidationSummaryCreator {

    public static final TransformData ASIC_TRADE = new TransformData(Project_ASICTradeReportToIso20022.class, Auth030AsicModelConfig.SCHEMA_PATH);
    public static final TransformData ASIC_MARGIN = new TransformData(Project_ASICMarginReportToIso20022.class, Auth108AsicModelConfig.SCHEMA_PATH);

    public static final TransformData CSA_TRADE = new TransformData(Project_CSATradeReportToDtccRdsHarmonized.class, DtccRdsHarmonizedModelConfig.SCHEMA_PATH);
    public static final TransformData CSA_PPD_TRADE = new TransformData(Project_CsaPpdReportToDtccRdsHarmonized.class, DtccRdsHarmonizedModelConfig.SCHEMA_PATH);
    public static final TransformData CSA_VALUATION = new TransformData(Project_CSAValuationReportToDtccRdsHarmonized.class, DtccRdsHarmonizedModelConfig.SCHEMA_PATH);

    public static final TransformData EMIR_TRADE = new TransformData(Project_EsmaEmirTradeReportToIso20022.class, Auth030EsmaModelConfig.SCHEMA_PATH);
    public static final TransformData EMIR_MARGIN = new TransformData(Project_EsmaEmirMarginReportToIso20022.class, Auth108EsmaModelConfig.SCHEMA_PATH);

    public static final TransformData FCA_UKEMIR_TRADE = new TransformData(Project_FcaUkEmirTradeReportToIso20022.class, Auth030FcaModelConfig.SCHEMA_PATH);
    public static final TransformData FCA_UKEMIR_MARGIN = new TransformData(Project_FcaUkEmirMarginReportToIso20022.class, Auth108FcaModelConfig.SCHEMA_PATH);

    public static final TransformData HKMA_DTCC_TRADE = new TransformData(Project_HKMADtccTradeReportToIso20022.class, Auth030HkmaDtccModelConfig.SCHEMA_PATH);
    public static final TransformData HKMA_DTCC_MARGIN = new TransformData(Project_HKMADtccMarginReportToIso20022.class, Auth108HkmaDtccModelConfig.SCHEMA_PATH);
    public static final TransformData HKMA_DTCC_VALUATION = new TransformData(Project_HKMADtccValuationReportToIso20022.class, Auth030HkmaDtccModelConfig.SCHEMA_PATH);
    public static final TransformData HKMA_TR_TRADE = new TransformData(Project_HKMATrTradeReportToIso20022.class, Auth108HkmaTrModelConfig.SCHEMA_PATH);
    public static final TransformData HKMA_TR_MARGIN = new TransformData(Project_HKMATrMarginReportToIso20022.class, Auth108HkmaTrModelConfig.SCHEMA_PATH);
    public static final TransformData HKMA_TR_VALUATION = new TransformData(Project_HKMATrValuationReportToIso20022.class, Auth108HkmaTrModelConfig.SCHEMA_PATH);

    public static final TransformData JFSA_TRADE = new TransformData(Project_JFSARewriteTradeReportToIso20022.class, Auth030JfsaModelConfig.SCHEMA_PATH);
    public static final TransformData JFSA_MARGIN = new TransformData(Project_JFSARewriteMarginReportToIso20022.class, Auth108JfsaModelConfig.SCHEMA_PATH);

    public static final TransformData MAS_TRADE = new TransformData(Project_MASTradeReportToIso20022.class, Auth030MasModelConfig.SCHEMA_PATH);
    public static final TransformData MAS_MARGIN = new TransformData(Project_MASMarginReportToIso20022.class, Auth108MasModelConfig.SCHEMA_PATH);

    private static final Logger LOGGER = LoggerFactory.getLogger(Iso20022ValidationSummaryCreator.class);
    private static final ObjectMapper DEFAULT_OBJECT_MAPPER = RosettaObjectMapper.getNewRosettaObjectMapper();
    private static final ObjectWriter DEFAULT_OBJECT_WRITER = DEFAULT_OBJECT_MAPPER.writerWithDefaultPrettyPrinter();

    @Inject
    Injector injector;

    @Inject
    RosettaTypeValidator validator;

    public <IN extends RosettaModelObject> void generateValidationSummaryAndWriteCsv(List<TransformData> transformDataList, Path configPath, Path analyticsPath) throws IOException {
        ObjectMapper pipelineObjectMapper = new ObjectMapper();
        List<PipelineModel> allPipelineModels = TestPackUtils.getPipelineModels(configPath, getClass().getClassLoader(), pipelineObjectMapper);
        List<TestPackModel> allTestPackModels = TestPackUtils.getTestPackModels(configPath, getClass().getClassLoader(), pipelineObjectMapper);

        List<ValidationData> validationDataList = new ArrayList<>();

        // Process each report type
        for (TransformData transformData : transformDataList) {
            Class<?> functionClass = transformData.getFunctionClass();
            PipelineModel pipelineModel = TestPackUtils.getPipelineModel(allPipelineModels, functionClass.getName(), transformData.getModelId());

            Class<IN> inputClass = toClass(pipelineModel.getTransform().getInputType());
            List<TestPackModel> pipelineTestPackModels = TestPackUtils.getTestPackModels(allTestPackModels, pipelineModel.getId());

            ObjectWriter outputObjectWriter = TestPackUtils.getObjectWriter(pipelineModel.getOutputSerialisation()).orElse(DEFAULT_OBJECT_WRITER);

            URL xmlSchemaUrl = Resources.getResource(transformData.getSchemaPath());
            Validator xsdValidator = getXsdValidator(xmlSchemaUrl);

            ValidationSummaryProcessor<IN> validationSummaryProcessor = new DefaultValidationSummaryProcessor<>(injector, DEFAULT_OBJECT_MAPPER, outputObjectWriter, validator, xsdValidator);
            validationDataList.addAll(validationSummaryProcessor.processValidation(functionClass, inputClass, pipelineTestPackModels));
        }
        // Write results
        Collections.sort(validationDataList);
        new Iso20022ValidationSummaryWriter(analyticsPath).writeCsv(validationDataList);
        new Iso20022ValidationFileSummaryWriter(analyticsPath).writeCsv(validationDataList);
    }

    @SuppressWarnings("unchecked")
    private <IN extends RosettaModelObject> Class<IN> toClass(String className) {
        try {
            return (Class<IN>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Validator getXsdValidator(URL xsdSchema) {
        try {
            SchemaFactory schemaFactory = SchemaFactory
                    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            // required to process xml elements with an maxOccurs greater than 5000 (rather than unbounded)
            schemaFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
            Schema schema = schemaFactory.newSchema(xsdSchema);
            return schema.newValidator();
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }
}
