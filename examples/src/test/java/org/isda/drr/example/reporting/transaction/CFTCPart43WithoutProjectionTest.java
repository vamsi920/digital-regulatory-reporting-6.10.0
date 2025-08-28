package org.isda.drr.example.reporting.transaction;

import jakarta.inject.Inject;
import com.regnosys.drr.examples.util.ResourcesUtils;
import com.regnosys.rosetta.common.validation.ValidationReport;
import drr.enrichment.common.trade.functions.Create_TransactionReportInstruction;
import drr.regulation.cftc.rewrite.CFTCPart43TransactionReport;
import drr.regulation.cftc.rewrite.reports.CFTCPart43ReportFunction;
import drr.regulation.common.ReportableEvent;
import drr.regulation.common.ReportingSide;
import drr.regulation.common.TransactionReportInstruction;
import org.isda.drr.example.AbstractReportingTest;
import org.isda.drr.example.util.ReportingTestUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test class to demonstrate CFTC Part 43 transaction reporting without ISO-20022 projection.
 * This class validates the transformation of a `ReportableEvent` to a `CFTCPart43TransactionReport`,
 * followed by validation of the generated report.
 */
final class CFTCPart43WithoutProjectionTest extends AbstractReportingTest {

    private static final Logger logger = LoggerFactory.getLogger(CFTCPart43WithoutProjectionTest.class);

    // Function to create a transaction report instruction from a ReportableEvent
    @Inject
    Create_TransactionReportInstruction createInstructionFunc;

    // Function to generate a CFTC Part 43 Transaction Report
    @Inject
    CFTCPart43ReportFunction reportFunc;

    /**
     * Demonstrates CFTC Part 43 transaction reporting without projection.
     * The test performs the following steps:
     * - Loads a `ReportableEvent` from JSON.
     * - Generates a `CFTCPart43TransactionReport` and validates it.
     *
     * @throws IOException If there is an error during file reading or processing.
     */
    @Test
    void CFTCPart43WithoutProjectionExampleReportTest() throws IOException {
        // Load a ReportableEvent from the input test data
        ReportableEvent reportableEvent = ResourcesUtils.getObjectAndResolveReferences(
                ReportableEvent.class,
                "regulatory-reporting/input/rates/IR-IRS-Fixed-Float-ex01.json"
        );
        assertNotNull(reportableEvent, "No reportable event was found");

        // Generate the CFTC Part 43 transaction report
        CFTCPart43TransactionReport report = runReport(reportableEvent);
        assertNotNull(report, "The report is null");

        // Validate the report and print validation results
        ValidationReport validationReport = validator.runProcessStep(CFTCPart43TransactionReport.class, report.toBuilder());
        ReportingTestUtils.printValidation(validationReport);
    }

    /**
     * Generates a CFTC Part 43 transaction report from a `ReportableEvent`.
     *
     * @param reportableEvent The reportable event input.
     * @return The generated CFTCPart43TransactionReport.
     * @throws IOException If there is an error during processing.
     */
    CFTCPart43TransactionReport runReport(ReportableEvent reportableEvent) throws IOException {
        // Create the reporting side (e.g., reporting party and counterparty)
        final ReportingSide reportingSide = ReportingTestUtils.createDefaultReportingSideFromReportableEvent(reportableEvent);

        // Create a transaction report instruction from the reportable event
        final TransactionReportInstruction reportInstruction = createInstructionFunc.evaluate(reportableEvent, reportingSide);

        // Generate the CFTC Part 43 transaction report
        final CFTCPart43TransactionReport report = reportFunc.evaluate(reportInstruction);

        // Print the generated report in JSON format for debugging
        logger.debug(mapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(report));

        return report;
    }
}
