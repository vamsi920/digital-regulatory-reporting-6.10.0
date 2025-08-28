package org.isda.drr.example.reporting.transaction;

import com.google.common.io.Resources;
import jakarta.inject.Inject;
import com.regnosys.drr.examples.util.ResourcesUtils;
import com.regnosys.rosetta.common.validation.ValidationReport;
import drr.enrichment.common.trade.functions.Create_TransactionReportInstruction;
import drr.projection.dtcc.rds.harmonized.csa.rewrite.trade.functions.Project_CsaPpdReportToDtccRdsHarmonized;
import drr.regulation.common.ReportableEvent;
import drr.regulation.common.ReportingSide;
import drr.regulation.common.TransactionReportInstruction;
import drr.regulation.csa.rewrite.trade.CSATransactionReport;
import drr.regulation.csa.rewrite.trade.reports.CSAPPDReportFunction;
import iso20022.DtccRdsHarmonizedModelConfig;
import org.isda.drr.example.AbstractReportingTest;
import org.isda.drr.example.util.ReportingTestUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;

final class CSAPPDTradeWithDtccProjectionTest extends AbstractReportingTest {

    private static final Logger logger = LoggerFactory.getLogger(CSAPPDTradeWithDtccProjectionTest.class);

    // Function to create a transaction report instruction from a ReportableEvent
    @Inject
    Create_TransactionReportInstruction createInstructionFunc;

    // Function to generate a CSA PPD Transaction Report
    @Inject
    CSAPPDReportFunction reportFunc;

    // Function to project a CSA Ppd transaction report to Dtcc Rds Harmonized format
    @Inject
    Project_CsaPpdReportToDtccRdsHarmonized csaPpdReportToDtccRdsHarmonized;

    /**
     * Demonstrates CSA PPD transaction reporting with Dtcc Rds projection.
     * The test performs the following steps:
     * - Loads a `ReportableEvent` from JSON.
     * - Generates a `CSATransactionReport` and validates it.
     * - Projects the report to Dtcc Rds format.
     *
     * @throws IOException If there is an error during file reading or processing.
     */
    @Test
    void CSATradeWithDtccRdsProjectionExampleReportTest() throws IOException {
        // Load a ReportableEvent from the input test data
        ReportableEvent reportableEvent = ResourcesUtils.getObjectAndResolveReferences(
                ReportableEvent.class,
                "regulatory-reporting/input/rates/IR-IRS-Fixed-Float-ex01.json"
        );
        assertNotNull(reportableEvent, "No reportable event was found");

        // Generate the CSA PPD transaction report
        CSATransactionReport report = runReport(reportableEvent);
        assertNotNull(report, "The report is null");

        // Validate the report and print validation results
        ValidationReport validationReport = validator.runProcessStep(CSATransactionReport.class, report.toBuilder());
        ReportingTestUtils.printValidation(validationReport);

        dtccRdsHarmonizedProjection(report);
    }


    /**
     * Generates a CSA PPD Tansaction report from a `ReportableEvent`.
     *
     * @param reportableEvent The reportable event input.
     * @return The generated CSATransactionReport.
     * @throws IOException If there is an error during processing.
     */
    CSATransactionReport runReport(ReportableEvent reportableEvent) throws IOException {
        // Create the reporting side (e.g., reporting party and counterparty)
        final ReportingSide reportingSide = ReportingTestUtils.createDefaultReportingSideFromReportableEvent(reportableEvent);

        // Create a transaction report instruction from the reportable event
        final TransactionReportInstruction reportInstruction = createInstructionFunc.evaluate(reportableEvent, reportingSide);

        // Generate the CSA PPD transaction report
        final CSATransactionReport report = reportFunc.evaluate(reportInstruction);

        // Print the generated report in JSON format for debugging
        logger.debug(mapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(report));

        return report;
    }

    /**
     * Projects a `CSATransactionReport` to Dtcc Rds Harmonized format.
     *
     * @param report The CSA PPD transaction report to project.
     * @throws IOException If there is an error during the projection process.
     */
    void dtccRdsHarmonizedProjection(CSATransactionReport report) throws IOException {
        // Project the CSA PPD transaction report to a Dtcc Rds document
        iso20022.dtcc.rds.harmonized.Document dtccRdsHarmonizedDocument = csaPpdReportToDtccRdsHarmonized.evaluate(report);

        // Load the Dtcc Rds configuration path
        URL dtccRdsHarmonizedXmlConfig = Resources.getResource(DtccRdsHarmonizedModelConfig.XML_CONFIG_PATH);

        // Print the Dtcc Rds document using the provided configuration
        ReportingTestUtils.logXMLProjection(dtccRdsHarmonizedDocument, dtccRdsHarmonizedXmlConfig);
    }
}
