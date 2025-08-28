package com.regnosys.drr.examples;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.inject.Guice;
import jakarta.inject.Inject;
import com.google.inject.Injector;
import com.regnosys.drr.DrrRuntimeModule;
import com.regnosys.drr.examples.util.ResourcesUtils;
import com.regnosys.rosetta.common.postprocess.qualify.QualificationReport;
import com.regnosys.rosetta.common.postprocess.qualify.QualifyProcessorStep;
import com.regnosys.rosetta.common.serialisation.RosettaObjectMapper;
import com.regnosys.rosetta.common.validation.RosettaTypeValidator;
import com.regnosys.rosetta.common.validation.ValidationReport;
import drr.regulation.common.ReportableEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ValidateAndQualifySample {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidateAndQualifySample.class);
    private static final ObjectWriter OBJECT_WRITER = 
            RosettaObjectMapper
                    .getNewMinimalRosettaObjectMapper()
                    .writerWithDefaultPrettyPrinter();

    @Inject
    RosettaTypeValidator validateProcessor;

    @Inject
    QualifyProcessorStep qualifyProcessor;

    public static void main(String[] args) throws IOException {
        // Initialise guice for dependency injection
        Injector injector = Guice.createInjector(new DrrRuntimeModule());
        // Get dependency injected instance
        ValidateAndQualifySample example = injector.getInstance(ValidateAndQualifySample.class);
        // run example
        example.validateAndQualifySample();
    }

    void validateAndQualifySample() throws IOException {
        // Deserialise (and resolve references)
        String resourceName = "regulatory-reporting/input/events/New-Trade-01.json"; //"sample.json";
        ReportableEvent reportableEvent = ResourcesUtils.getObjectAndResolveReferences(ReportableEvent.class, resourceName);
        ReportableEvent.ReportableEventBuilder reportableEventBuilder = reportableEvent.toBuilder();

        // Qualify 
        QualificationReport report = qualifyProcessor.runProcessStep(ReportableEvent.class, reportableEventBuilder);
        report.logReport();
        LOGGER.info(OBJECT_WRITER.writeValueAsString(report.getResultObject()));

        // Validate
        ValidationReport validationReport = validateProcessor.runProcessStep(ReportableEvent.class, reportableEventBuilder);
        validationReport.logReport(); // logs validation failures as DEBUG and validation success as TRACE
    }
}
