package org.isda.drr.example.reporting.transaction;

import com.google.common.io.Resources;
import jakarta.inject.Inject;
import com.regnosys.drr.examples.util.ResourcesUtils;
import com.regnosys.rosetta.common.validation.ValidationReport;
import drr.projection.iso20022.jfsa.rewrite.trade.functions.Project_JFSARewriteTradeReportToIso20022;
import drr.regulation.common.ReportableEvent;
import drr.regulation.common.ReportingSide;
import drr.regulation.common.TransactionReportInstruction;
import drr.regulation.jfsa.rewrite.trade.JFSATransactionReport;
import drr.regulation.jfsa.rewrite.trade.reports.JFSATradeReportFunction;
import iso20022.Auth030JfsaModelConfig;
import iso20022.auth030.jfsa.Document;
import org.isda.drr.example.AbstractReportingTest;
import org.isda.drr.example.util.ReportingTestUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test class to demonstrate JFSA trade reporting with ISO-20022 projection.
 * This class validates the transformation of a `ReportableEvent` to a `JFSATransactionReport`,
 * followed by validation and ISO-20022 projection.
 */
final class JFSATradeWithISOProjectionTest extends AbstractReportingTest {

    private static final Logger logger = LoggerFactory.getLogger(JFSATradeWithISOProjectionTest.class);

    // Function to generate a JFSA Trade Report from a TransactionReportInstruction
    @Inject
    JFSATradeReportFunction reportFunc;

    // Function to project a JFSA Trade Report to ISO-20022 format
    @Inject
    Project_JFSARewriteTradeReportToIso20022 jfsaTradeReportToIso20022;

    /**
     * Demonstrates JFSA trade reporting with ISO-20022 projection.
     * The test performs the following steps:
     * - Loads a `ReportableEvent` from JSON.
     * - Generates a `JFSATransactionReport` and validates it.
     * - Projects the report to ISO-20022 format.
     *
     * @throws IOException If there is an error during file reading or processing.
     */
    @Test
    void JFSATradeWithISOProjectionExampleTest() throws IOException {
        // Load a ReportableEvent from the input test data
        ReportableEvent reportableEvent = ResourcesUtils.getObjectAndResolveReferences(
                ReportableEvent.class,
                "regulatory-reporting/input/rates/IR-IRS-Fixed-Float-ex01.json"
        );
        assertNotNull(reportableEvent, "No reportable event was found");

        // Generate the JFSA transaction report
        JFSATransactionReport report = runReport(reportableEvent);
        assertNotNull(report, "The report is null");

        // Validate the report and print validation results
        ValidationReport validationReport = validator.runProcessStep(JFSATransactionReport.class, report.toBuilder());
        ReportingTestUtils.printValidation(validationReport);

        // Project the report to ISO-20022 format
        ISOProjection(report);
    }

    /**
     * Generates a JFSA transaction report from a `ReportableEvent`.
     *
     * @param reportableEvent The reportable event input.
     * @return The generated JFSATransactionReport.
     * @throws IOException If there is an error during processing.
     */
    private JFSATransactionReport runReport(ReportableEvent reportableEvent) throws IOException {
        // Create the reporting side (e.g., reporting party and counterparty)
        final ReportingSide reportingSide = ReportingTestUtils.createDefaultReportingSideFromReportableEvent(reportableEvent);

        // Create a transaction report instruction from the reportable event
        final TransactionReportInstruction reportInstruction = createTransactionReportInstructionFunc.evaluate(reportableEvent, reportingSide);

        // Generate the JFSA transaction report
        final JFSATransactionReport report = reportFunc.evaluate(reportInstruction);

        // Print the generated report in JSON format for debugging
        logger.debug(mapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(report));

        return report;
    }

    /**
     * Projects a `JFSATransactionReport` to ISO-20022 format.
     *
     * @param report The JFSA transaction report to project.
     * @throws IOException If there is an error during the projection process.
     */
    private void ISOProjection(JFSATransactionReport report) throws IOException {
        // Project the JFSA transaction report to an ISO-20022 document
        Document iso20022Document = jfsaTradeReportToIso20022.evaluate(report);

        // Load the ISO-20022 configuration path
        URL iso20022Auth030XmlConfig = Resources.getResource(Auth030JfsaModelConfig.XML_CONFIG_PATH);

        // Print the ISO-20022 document using the provided configuration
        ReportingTestUtils.logXMLProjection(iso20022Document, iso20022Auth030XmlConfig);
    }
}
