package org.isda.drr.example.reporting.collateralmargin;

import jakarta.inject.Inject;
import com.regnosys.drr.examples.util.ResourcesUtils;
import com.regnosys.rosetta.common.validation.ValidationReport;
import drr.regulation.common.CollateralReportInstruction;
import drr.regulation.common.ReportableCollateral;
import drr.regulation.common.ReportingSide;
import drr.regulation.csa.rewrite.margin.CSAMarginReport;
import drr.regulation.csa.rewrite.margin.reports.CSAMarginReportFunction;
import org.isda.drr.example.AbstractReportingTest;
import org.isda.drr.example.util.ReportingTestUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

final class CSACollateralWithoutProjectionTest extends AbstractReportingTest {

    private static final Logger logger = LoggerFactory.getLogger(CSACollateralWithoutProjectionTest.class);

    // Function to generate a CSA Margin Report from a CollateralReportInstruction
    @Inject
    CSAMarginReportFunction reportFunc;

    /**
     * Demonstrates CSA collateral reporting without projection.
     * The test performs the following steps:
     * - Loads a `ReportableCollateral` from JSON.
     * - Generates a `CSAMarginReport` and validates it.
     *
     * @throws IOException If there is an error during file reading or processing.
     */
    @Test
    void CSACollateralWithoutProjectionExampleTest() throws IOException {
        // Load a ReportableCollateral from the input test data
        ReportableCollateral reportableCollateral = ResourcesUtils.getObjectAndResolveReferences(
                ReportableCollateral.class,
                "regulatory-reporting/input/collateral/Collateral-ex01.json"
        );
        assertNotNull(reportableCollateral, "No reportable collateral was found");

        // Generate the CSA margin report
        CSAMarginReport report = runReport(reportableCollateral);
        assertNotNull(report, "The report is null");

        // Validate the report and print validation results
        ValidationReport validationReport = validator.runProcessStep(CSAMarginReport.class, report.toBuilder());
        ReportingTestUtils.printValidation(validationReport);
    }

    /**
     * Generates a CSA margin report from a `ReportableCollateral`.
     *
     * @param reportableCollateral The reportable collateral input.
     * @return The generated CSAMarginReport.
     * @throws IOException If there is an error during processing.
     */
    CSAMarginReport runReport(ReportableCollateral reportableCollateral) throws IOException {
        // Create the reporting side (e.g., reporting party and counterparty)
        final ReportingSide reportingSide = ReportingTestUtils.createDefaultReportingSideFromReportableCollateral(reportableCollateral);

        // Create a collateral report instruction from the reportable collateral
        final CollateralReportInstruction reportInstruction = createCollateralReportInstructionFunc.evaluate(reportableCollateral, reportingSide);

        // Generate the CSA margin report
        final CSAMarginReport report = reportFunc.evaluate(reportInstruction);

        // Print the generated report in JSON format for debugging
        logger.debug(mapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(report));

        return report;
    }
}
