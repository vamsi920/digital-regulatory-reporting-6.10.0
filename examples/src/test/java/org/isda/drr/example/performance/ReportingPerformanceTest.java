package org.isda.drr.example.performance;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import com.regnosys.rosetta.common.serialisation.RosettaObjectMapperCreator;
import com.regnosys.rosetta.common.validation.ValidationReport;
import drr.projection.iso20022.esma.emir.refit.trade.functions.Project_EsmaEmirTradeReportToIso20022;
import drr.regulation.common.ReportableEvent;
import drr.regulation.common.ReportingSide;
import drr.regulation.common.TransactionReportInstruction;
import drr.regulation.esma.emir.refit.trade.ESMAEMIRTransactionReport;
import drr.regulation.esma.emir.refit.trade.reports.ESMAEMIRTradeReportFunction;
import iso20022.Auth030EsmaModelConfig;
import iso20022.auth030.esma.Document;
import org.isda.drr.example.AbstractReportingTest;
import org.isda.drr.example.util.ReportingTestUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * This test class evaluates the performance of various stages involved in
 * the DRR regulatory reporting process, including deserialization,
 * validation, report generation, and ISO20022 projection.
 */
final class ReportingPerformanceTest extends AbstractReportingTest {

    // Logger for capturing debug information and performance metrics
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportingPerformanceTest.class);

    // Directory path containing sample test data for regulatory reporting
    private static final String reportingTestPackSamples = "regulatory-reporting/input";

    // Injected dependencies for performing report generation and transformation
    @Inject
    ESMAEMIRTradeReportFunction esmaEmirTradeReportFunc;
    @Inject
    Project_EsmaEmirTradeReportToIso20022 esmaEmirTradeReportToIso20022Func;

    /**
     * Test to measure performance metrics for the ESMA EMIR reporting process.
     * The test performs the following steps:
     * - Deserialization of input JSON files into domain objects
     * - Validation of the generated transaction report
     * - Generation of a transaction report based on input data
     * - Projection of the report to ISO20022 format
     *
     * Metrics collected include execution time and memory usage for each stage.
     *
     * @throws IOException if resource files cannot be accessed
     */
    @Test
    void esmaEmirPerformanceMetrics() throws IOException {

        // Load the XML configuration for ISO20022 mapping
        final URL iso20022ProjectionConfig = this.getClass().getClassLoader().getResource(Auth030EsmaModelConfig.XML_CONFIG_PATH);
        assertNotNull(iso20022ProjectionConfig);
        final ObjectMapper xmlMapper = RosettaObjectMapperCreator.forXML(iso20022ProjectionConfig.openStream()).create();

        LOGGER.info("Collecting ESMA EMIR (ReportTransactionInstruction) performance metrics...");
        LOGGER.info("ISO20022 projection configuration: {}", iso20022ProjectionConfig);

        // Initialize lists to store performance metrics for each processing stage
        final List<PerformanceMetric> serializationMetrics = new ArrayList<>();
        final List<PerformanceMetric> validationMetrics = new ArrayList<>();
        final List<PerformanceMetric> reportGenerationMetrics = new ArrayList<>();
        final List<PerformanceMetric> isoProjectionMetrics = new ArrayList<>();

        // Locate the directory containing sample JSON files within the project dependencies
        URL url = this.getClass().getClassLoader().getResource(reportingTestPackSamples);
        assertNotNull(url, reportingTestPackSamples + " should be resolvable through project dependencies");

        // Extract the path to the JAR file containing test resources
        String[] parts = url.getPath().split("!");
        String jarPath = parts[0].substring(parts[0].indexOf("file:") + 5);
        JarFile jarFile = new JarFile(jarPath);

        // Iterate through sample JSON files in the test data directory
        jarFile.stream()
                .filter(entry -> entry.getName().startsWith(reportingTestPackSamples) && entry.getName().endsWith(".json"))
                .forEach(entry -> {
                    // Builders to measure performance metrics for each processing stage
                    PerformanceMetric.PerformanceMetricBuilder serializationMetricBuilder = PerformanceMetric.PerformanceMetricBuilder.newInstance();
                    PerformanceMetric.PerformanceMetricBuilder validationMetricBuilder = PerformanceMetric.PerformanceMetricBuilder.newInstance();
                    PerformanceMetric.PerformanceMetricBuilder reportGenerationMetricBuilder = PerformanceMetric.PerformanceMetricBuilder.newInstance();
                    PerformanceMetric.PerformanceMetricBuilder isoProjectionMetricBuilder = PerformanceMetric.PerformanceMetricBuilder.newInstance();

                    try {
                        // Deserialize the JSON file into a ReportableEvent object
                        InputStream ins = jarFile.getInputStream(entry);
                        serializationMetricBuilder.start();
                        ReportableEvent deserialized = resolveReferences(mapper.readValue(ins, ReportableEvent.class));
                        serializationMetricBuilder.end();
                        assertNotNull(deserialized);

                        // Create the reporting side from the deserialized event
                        final ReportingSide reportingSide = ReportingTestUtils.createDefaultReportingSideFromReportableEvent(deserialized);

                        // Generate a TransactionReportInstruction
                        final TransactionReportInstruction reportInstruction = createTransactionReportInstructionFunc.evaluate(deserialized, reportingSide);

                        // Validate the generated report
                        validationMetricBuilder.start();
                        ValidationReport validationReport = validate(reportInstruction);
                        validationMetricBuilder.end();
                        assertNotNull(validationReport);

                        // Generate the ESMA EMIR transaction report
                        reportGenerationMetricBuilder.start();
                        ESMAEMIRTransactionReport drrReport = esmaEmirTradeReportFunc.evaluate(reportInstruction);
                        reportGenerationMetricBuilder.end();
                        assertNotNull(drrReport);

                        // Project the transaction report to ISO20022 format
                        isoProjectionMetricBuilder.start();
                        Document projectedReport = esmaEmirTradeReportToIso20022Func.evaluate(drrReport);
                        String xmlReport = xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(projectedReport);
                        isoProjectionMetricBuilder.end();
                        assertNotNull(xmlReport);

                        // Collect performance metrics for all stages
                        serializationMetrics.add(serializationMetricBuilder.build());
                        validationMetrics.add(validationMetricBuilder.build());
                        reportGenerationMetrics.add(reportGenerationMetricBuilder.build());
                        isoProjectionMetrics.add(isoProjectionMetricBuilder.build());

                    } catch (Exception e) {
                        throw new RuntimeException(e); // Handle unexpected errors gracefully
                    }
                });

        String header = String.format("%n| %-25s | %-25s%n", "Execution time (ms)", "Memory (MB)");
        // Log performance metrics for each stage
        debugMetricCollection(":: Serialization Metrics ::", header, serializationMetrics);
        debugMetricCollection(":: Validation Metrics ::", header, validationMetrics);
        debugMetricCollection(":: Report Generation Metrics ::", header, reportGenerationMetrics);
        debugMetricCollection(":: ISO20022 Projection Metrics ::", header, isoProjectionMetrics);

        // Log average metrics across all stages
        LOGGER.info(":: Average Metrics from {} ReportableEvent samples in test pack {} ::", serializationMetrics.size(), reportingTestPackSamples);
        infoMetricAverage("Deserialization", serializationMetrics);
        infoMetricAverage("Validation", validationMetrics);
        infoMetricAverage("Report Generation", reportGenerationMetrics);
        infoMetricAverage("ISO20022 Projection", isoProjectionMetrics);
    }

    // Utility methods for logging and summarizing metrics

    private static void debugMetricCollection (String title, String header, List<PerformanceMetric> metrics) {
        LOGGER.debug(title);
        LOGGER.debug(header.concat(metrics.stream()
                .map(PerformanceMetric::toString)
                .collect(Collectors.joining("\n"))));
    }

    private static void infoMetricAverage (String processLabel, List<PerformanceMetric> metrics) {
        double avgExecutionTime = metrics.stream().mapToLong(PerformanceMetric::getExecutionTime).average().orElse(0);
        double avgMemory = metrics.stream().mapToLong(PerformanceMetric::getMemoryAllocation).average().orElse(0);
        LOGGER.info(String.format("| %-25s %s",
                        processLabel,
                        PerformanceMetric.toString(avgExecutionTime, avgMemory)
                )
        );
    }
}
