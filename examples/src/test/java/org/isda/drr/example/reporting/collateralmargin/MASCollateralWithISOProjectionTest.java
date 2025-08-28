package org.isda.drr.example.reporting.collateralmargin;

import com.google.common.io.Resources;
import jakarta.inject.Inject;
import com.regnosys.drr.examples.util.ResourcesUtils;
import com.regnosys.rosetta.common.validation.ValidationReport;
import drr.projection.iso20022.mas.rewrite.margin.functions.Project_MASMarginReportToIso20022;
import drr.regulation.common.CollateralReportInstruction;
import drr.regulation.common.ReportableCollateral;
import drr.regulation.common.ReportingSide;
import drr.regulation.mas.rewrite.margin.MASMarginReport;
import drr.regulation.mas.rewrite.margin.reports.MASMarginReportFunction;
import iso20022.Auth108MasModelConfig;
import iso20022.auth108.mas.Document;
import org.isda.drr.example.AbstractReportingTest;
import org.isda.drr.example.util.ReportingTestUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test class to demonstrate MAS collateral reporting with ISO-20022 projection.
 * This class validates the transformation of a `ReportableCollateral` to a `MASMarginReport`,
 * followed by validation and ISO-20022 projection.
 */
final class MASCollateralWithISOProjectionTest extends AbstractReportingTest {

    private static final Logger logger = LoggerFactory.getLogger(MASCollateralWithISOProjectionTest.class);

    // Function to generate a MAS Margin Report from a CollateralReportInstruction
    @Inject
    MASMarginReportFunction reportFunc;

    // Function to project a MAS Margin Report to ISO-20022 format
    @Inject
    Project_MASMarginReportToIso20022 masMarginReportToIso20022;

    /**
     * Demonstrates MAS collateral reporting with ISO-20022 projection.
     * The test performs the following steps:
     * - Loads a `ReportableCollateral` from JSON.
     * - Generates a `MASMarginReport` and validates it.
     * - Projects the report to ISO-20022 format.
     *
     * @throws IOException If there is an error during file reading or processing.
     */
    @Test
    void MASCollateralWithISOProjectionExampleTest() throws IOException {
        // Load a ReportableCollateral from the input test data
        ReportableCollateral reportableCollateral = ResourcesUtils.getObjectAndResolveReferences(
                ReportableCollateral.class,
                "regulatory-reporting/input/collateral/Collateral-ex01.json"
        );
        assertNotNull(reportableCollateral, "No reportable collateral was found");

        // Generate the MAS margin report
        MASMarginReport report = runReport(reportableCollateral);
        assertNotNull(report, "The report is null");

        // Validate the report and print validation results
        ValidationReport validationReport = validator.runProcessStep(MASMarginReport.class, report.toBuilder());
        ReportingTestUtils.printValidation(validationReport);

        // Project the report to ISO-20022 format
        ISOProjection(report);
    }

    /**
     * Generates a MAS margin report from a `ReportableCollateral`.
     *
     * @param reportableCollateral The reportable collateral input.
     * @return The generated MASMarginReport.
     * @throws IOException If there is an error during processing.
     */
    MASMarginReport runReport(ReportableCollateral reportableCollateral) throws IOException {
        // Create the reporting side (e.g., reporting party and counterparty)
        final ReportingSide reportingSide = ReportingTestUtils.createDefaultReportingSideFromReportableCollateral(reportableCollateral);

        // Create a collateral report instruction from the reportable collateral
        final CollateralReportInstruction reportInstruction = createCollateralReportInstructionFunc.evaluate(reportableCollateral, reportingSide);

        // Generate the MAS margin report
        final MASMarginReport report = reportFunc.evaluate(reportInstruction);

        // Print the generated report in JSON format for debugging
        logger.debug(mapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(report));

        return report;
    }

    /**
     * Projects a `MASMarginReport` to ISO-20022 format.
     *
     * @param report The MAS margin report to project.
     * @throws IOException If there is an error during the projection process.
     */
    void ISOProjection(MASMarginReport report) throws IOException {
        // Project the MAS margin report to an ISO-20022 document
        Document iso20022Document = masMarginReportToIso20022.evaluate(report);

        // Load the ISO-20022 configuration path
        URL iso20022Auth108XmlConfig = Resources.getResource(Auth108MasModelConfig.XML_CONFIG_PATH);

        // Print the ISO-20022 document using the provided configuration
        ReportingTestUtils.logXMLProjection(iso20022Document, iso20022Auth108XmlConfig);
    }
}
