package org.isda.drr.example.reporting.transaction;

import com.google.common.io.Resources;
import jakarta.inject.Inject;
import com.regnosys.drr.examples.util.ResourcesUtils;
import com.regnosys.rosetta.common.validation.ValidationReport;
import drr.projection.iso20022.esma.emir.refit.trade.functions.Project_EsmaEmirTradeReportToIso20022;
import drr.regulation.common.*;
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
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test class for demonstrating EMIR Refit trade reporting with ISO-20022 projection.
 * This class includes examples of report generation and projection with enriched reporting sides
 * during an eligibility phase for regulatory compliance.
 */
final class EMIRRefitTradeWithISOProjectionTest extends AbstractReportingTest {

    private static final Logger logger = LoggerFactory.getLogger(EMIRRefitTradeWithISOProjectionTest.class);

    // Function to generate an EMIR refit Trade Report from a TransactionReportInstruction
    @Inject
    ESMAEMIRTradeReportFunction reportFunc;

    // Function to project an EMIR refit Trade Report to ISO-20022 format
    @Inject
    Project_EsmaEmirTradeReportToIso20022 emirTradeReportToIso20022;


    /**
     * Demonstrates EMIR REFIT trade reporting with ISO-20022 projection.
     * The test performs the following steps:
     * - Loads a `ReportableEvent` from JSON.
     * - Generates an `ESMAEMIRTransactionReport` and validates it.
     * - Projects the report to ISO-20022 format.
     *
     * @throws IOException If there is an error during file reading or processing.
     */
    @Test
    void EmirRefitTradeWithISOProjectionExampleTest() throws IOException {
        // Load a ReportableEvent from the input test data
        ReportableEvent reportableEvent = ResourcesUtils.getObjectAndResolveReferences(
                ReportableEvent.class,
                "regulatory-reporting/input/rates/IR-IRS-Fixed-Float-ex01.json"
        );
        assertNotNull(reportableEvent, "No reportable event was found");

        // Generate the EMIR refit transaction report
        ESMAEMIRTransactionReport report = runReport(reportableEvent);
        assertNotNull(report, "The report is null");

        // Validate the report and print validation results
        ValidationReport validationReport = validator.runProcessStep(ESMAEMIRTransactionReport.class, report.toBuilder());
        ReportingTestUtils.printValidation(validationReport);

        // Project the report to ISO-20022 format
        ISOProjection(report);
    }

    /**
     * Demonstrates EMIR REFIT ETD reporting with ISO-20022 projection.
     * The test performs the following steps:
     * - Loads a `ReportableEvent` from JSON.
     * - Generates an `ESMAEMIRTransactionReport` and validates it.
     * - Projects the report to ISO-20022 format.
     *
     * @throws IOException If there is an error during file reading or processing.
     */
    @Test
    void EmirRefitETDWithISOprojectionExampleTest() throws IOException {
        // Load a ReportableEvent from the input test data
        ReportableEvent reportableEvent = ResourcesUtils.getObjectAndResolveReferences(
                ReportableEvent.class,
                "result-json-files/fpml-5-13/record-keeping/products/etd/mockup-etd.json"
        );
        assertNotNull(reportableEvent, "No reportable event was found");

        // Generate the EMIR refit transaction report
        ESMAEMIRTransactionReport report = runReport(reportableEvent);
        assertNotNull(report, "The report is null");

        // Validate the report and print validation results
        ValidationReport validationReport = validator.runProcessStep(ESMAEMIRTransactionReport.class, report.toBuilder());
        ReportingTestUtils.printValidation(validationReport);

        // Project the report to ISO-20022 format
        ISOProjection(report);
    }

    /**
     * Generates an EMIR refit transaction report from a `ReportableEvent`.
     *
     * @param reportableEvent The reportable event input.
     * @return The generated ESMAEMIRTransactionReport.
     * @throws IOException If there is an error during processing.
     */
    private ESMAEMIRTransactionReport runReport(ReportableEvent reportableEvent) throws IOException {
        // Create the reporting side (e.g., reporting party and counterparty)
        final ReportingSide reportingSide = ReportingTestUtils.createDefaultReportingSideFromReportableEvent(reportableEvent);

        // Create a transaction report instruction from the reportable event
        final TransactionReportInstruction reportInstruction = createTransactionReportInstructionFunc.evaluate(reportableEvent, reportingSide);

        // Generate the EMIR refit transaction report
        final ESMAEMIRTransactionReport report = reportFunc.evaluate(reportInstruction);

        // Print the generated report in JSON format for debugging
        logger.debug(mapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(report));

        return report;
    }

    /**
     * Projects an `ESMAEMIRTransactionReport` to ISO-20022 format.
     *
     * @param report The EMIR refit transaction report to project.
     * @throws IOException If there is an error during the projection process.
     */
    void ISOProjection(ESMAEMIRTransactionReport report) throws IOException {
        // Project the EMIR refit transaction report to an ISO-20022 document
        Document iso20022Document = emirTradeReportToIso20022.evaluate(report);

        // Load the ISO-20022 configuration path
        URL iso20022Auth030XmlConfig = Resources.getResource(Auth030EsmaModelConfig.XML_CONFIG_PATH);

        // Print the ISO-20022 document using the provided configuration
        ReportingTestUtils.logXMLProjection(iso20022Document, iso20022Auth030XmlConfig);
    }
}
