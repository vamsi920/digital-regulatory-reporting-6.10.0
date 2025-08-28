package org.isda.drr.example.reporting.valuation;

import com.google.common.io.Resources;
import jakarta.inject.Inject;
import com.regnosys.drr.examples.util.ResourcesUtils;
import com.regnosys.rosetta.common.validation.ValidationReport;
import drr.projection.dtcc.rds.harmonized.csa.rewrite.valuation.functions.Project_CSAValuationReportToDtccRdsHarmonized;
import drr.regulation.common.ReportableValuation;
import drr.regulation.common.ReportingSide;
import drr.regulation.common.ValuationReportInstruction;
import drr.regulation.csa.rewrite.trade.CSATransactionReport;
import drr.regulation.csa.rewrite.valuation.CSAValuationReport;
import drr.regulation.csa.rewrite.valuation.reports.CSAValuationReportFunction;
import iso20022.DtccRdsHarmonizedModelConfig;
import org.isda.drr.example.AbstractReportingTest;
import org.isda.drr.example.util.ReportingTestUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;

final class CSAValuationWithDtccProjectionTest extends AbstractReportingTest {

    private static final Logger logger = LoggerFactory.getLogger(CSAValuationWithDtccProjectionTest.class);

    // Function to generate a CSAValuationReport from a ValuationReportInstruction.
    @Inject
    CSAValuationReportFunction reportFunc;

    // Function to project a CSA valuation report to Dtcc Rds Harmonized format
    @Inject
    Project_CSAValuationReportToDtccRdsHarmonized csaValuationReportToDtccRdsHarmonized;

    /**
     * Demonstrates CSA valuation reporting with Dtcc Rds projection.
     * The test performs the following steps:
     * - Loads a `ReportableEvent` from JSON.
     * - Generates a `CSATransactionReport` and validates it.
     * - Projects the report to Dtcc Rds format.
     *
     * @throws IOException If there is an error during file reading or processing.
     */
    @Test
    void CSATradeWithDtccRdsProjectionExampleReportTest() throws IOException {
        // Load a ReportableValuation from the input test data.
        ReportableValuation reportableValuation = ResourcesUtils.getObjectAndResolveReferences(
                ReportableValuation.class,
                "regulatory-reporting/input/valuation/Valuation-ex01.json"
        );
        assertNotNull(reportableValuation, "No reportable event was found");

        // Generate the CSA transaction report
        CSAValuationReport report = runReport(reportableValuation);
        assertNotNull(report, "The report is null");

        // Validate the report and print validation results
        ValidationReport validationReport = validator.runProcessStep(CSATransactionReport.class, report.toBuilder());
        ReportingTestUtils.printValidation(validationReport);

        dtccRdsHarmonizedProjection(report);
    }

    /**
     * Generates a CSA valuation report from a `ReportableValuation`.
     *
     * @param reportableValuation The reportable valuation input.
     * @return The generated CSAValuationReport.
     * @throws IOException If there is an error during processing.
     */
    CSAValuationReport runReport(ReportableValuation reportableValuation) throws IOException {
        // Create the reporting side (e.g., reporting party and counterparty).
        final ReportingSide reportingSide = ReportingTestUtils.createDefaultReportingSideFromReportableValuation(reportableValuation);

        // Create a valuation report instruction from the reportable valuation.
        final ValuationReportInstruction reportInstruction = createValuationReportInstruction.evaluate(reportableValuation, reportingSide);

        // Generate the CSA valuation report.
        final CSAValuationReport report = reportFunc.evaluate(reportInstruction);

        // Print the generated report in JSON format for debugging.
        logger.debug(mapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(report));

        return report;
    }

    /**
     * Projects a `CSATransactionReport` to Dtcc Rds Harmonized format.
     *
     * @param report The CSA transaction report to project.
     * @throws IOException If there is an error during the projection process.
     */
    void dtccRdsHarmonizedProjection(CSAValuationReport report) throws IOException {
        // Project the CSA valuation report to a Dtcc Rds document
        iso20022.dtcc.rds.harmonized.Document dtccRdsHarmonizedDocument = csaValuationReportToDtccRdsHarmonized.evaluate(report);

        // Load the Dtcc Rds configuration path
        URL dtccRdsHarmonizedXmlConfig = Resources.getResource(DtccRdsHarmonizedModelConfig.XML_CONFIG_PATH);

        // Print the Dtcc Rds document using the provided configuration
        ReportingTestUtils.logXMLProjection(dtccRdsHarmonizedDocument, dtccRdsHarmonizedXmlConfig);
    }
}
