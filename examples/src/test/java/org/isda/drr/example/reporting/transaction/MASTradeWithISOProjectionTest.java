package org.isda.drr.example.reporting.transaction;

import com.google.common.io.Resources;
import jakarta.inject.Inject;
import com.regnosys.drr.examples.util.ResourcesUtils;
import com.regnosys.rosetta.common.validation.ValidationReport;
import drr.projection.iso20022.mas.rewrite.trade.functions.Project_MASTradeReportToIso20022;
import drr.regulation.common.ReportableEvent;
import drr.regulation.common.ReportingSide;
import drr.regulation.common.TransactionReportInstruction;
import drr.regulation.mas.rewrite.trade.MASTransactionReport;
import drr.regulation.mas.rewrite.trade.reports.MASTradeReportFunction;
import iso20022.Auth030MasModelConfig;
import iso20022.auth030.mas.Document;
import org.isda.drr.example.AbstractReportingTest;
import org.isda.drr.example.util.ReportingTestUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test class to demonstrate MAS trade reporting with ISO-20022 projection.
 * This class validates the transformation of a `ReportableEvent` to a `MASTransactionReport`,
 * followed by validation and ISO-20022 projection.
 */
final class MASTradeWithISOProjectionTest extends AbstractReportingTest {

    private static final Logger logger = LoggerFactory.getLogger(MASTradeWithISOProjectionTest.class);

    // Function to generate an MAS Trade Report from a TransactionReportInstruction
    @Inject
    MASTradeReportFunction reportFunc;

    // Function to project an MAS Trade Report to ISO-20022 format
    @Inject
    Project_MASTradeReportToIso20022 masTradeReportToIso20022;

    /**
     * Demonstrates MAS trade reporting with ISO-20022 projection.
     * The test performs the following steps:
     * - Loads a `ReportableEvent` from JSON.
     * - Generates a `MASTransactionReport` and validates it.
     * - Projects the report to ISO-20022 format.
     *
     * @throws IOException If there is an error during file reading or processing.
     */
    @Test
    void MasTradeWithISOProjectionExampleTest() throws IOException {
        // Load a ReportableEvent from the input test data
        ReportableEvent reportableEvent = ResourcesUtils.getObjectAndResolveReferences(
                ReportableEvent.class,
                "regulatory-reporting/input/rates/IR-IRS-Fixed-Float-ex01.json"
        );
        assertNotNull(reportableEvent, "No reportable event was found");

        // Generate the MAS transaction report
        MASTransactionReport report = runReport(reportableEvent);
        assertNotNull(report, "The report is null");

        // Validate the report and print validation results
        ValidationReport validationReport = validator.runProcessStep(MASTransactionReport.class, report.toBuilder());
        ReportingTestUtils.printValidation(validationReport);

        // Project the report to ISO-20022 format
        ISOProjection(report);
    }

    /**
     * Generates an MAS transaction report from a `ReportableEvent`.
     *
     * @param reportableEvent The reportable event input.
     * @return The generated MASTransactionReport.
     * @throws IOException If there is an error during processing.
     */
    private MASTransactionReport runReport(ReportableEvent reportableEvent) throws IOException {
        // Create the reporting side (e.g., reporting party and counterparty)
        final ReportingSide reportingSide = ReportingTestUtils.createDefaultReportingSideFromReportableEvent(reportableEvent);

        // Create a transaction report instruction from the reportable event
        final TransactionReportInstruction reportInstruction = createTransactionReportInstructionFunc.evaluate(reportableEvent, reportingSide);

        // Generate the MAS transaction report
        final MASTransactionReport report = reportFunc.evaluate(reportInstruction);

        // Print the generated report in JSON format for debugging
        logger.debug(mapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(report));

        return report;
    }

    /**
     * Projects a `MASTransactionReport` to ISO-20022 format.
     *
     * @param report The MAS transaction report to project.
     * @throws IOException If there is an error during the projection process.
     */
    private void ISOProjection(MASTransactionReport report) throws IOException {
        // Project the MAS transaction report to an ISO-20022 document
        Document iso20022Document = masTradeReportToIso20022.evaluate(report);

        // Load the ISO-20022 configuration path
        URL iso20022Auth030XmlConfig = Resources.getResource(Auth030MasModelConfig.XML_CONFIG_PATH);

        // Print the ISO-20022 document using the provided configuration
        ReportingTestUtils.logXMLProjection(iso20022Document, iso20022Auth030XmlConfig);
    }
}
