package org.isda.drr.example.reporting.collateralmargin;

import com.google.common.io.Resources;
import jakarta.inject.Inject;
import com.regnosys.drr.examples.util.ResourcesUtils;
import com.regnosys.rosetta.common.validation.ValidationReport;
import drr.projection.iso20022.asic.rewrite.margin.functions.Project_ASICMarginReportToIso20022;
import drr.regulation.asic.rewrite.margin.ASICMarginReport;
import drr.regulation.asic.rewrite.margin.reports.ASICMarginReportFunction;
import drr.regulation.common.CollateralReportInstruction;
import drr.regulation.common.ReportableCollateral;
import drr.regulation.common.ReportingSide;
import iso20022.Auth108AsicModelConfig;
import iso20022.auth108.asic.Document;
import org.isda.drr.example.AbstractReportingTest;
import org.isda.drr.example.util.ReportingTestUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test class to demonstrate ASIC collateral reporting with ISO-20022 projection.
 * This class validates the transformation of a `ReportableCollateral` to an `ASICMarginReport`,
 * followed by validation and ISO-20022 projection.
 */
final class ASICCollateralWithISOProjectionTest extends AbstractReportingTest {

    private static final Logger logger = LoggerFactory.getLogger(ASICCollateralWithISOProjectionTest.class);

    // Function to generate an ASIC Margin Report from a CollateralReportInstruction
    @Inject
    ASICMarginReportFunction reportFunc;

    // Function to project an ASIC Margin Report to ISO-20022 format
    @Inject
    Project_ASICMarginReportToIso20022 asicMarginReportToIso20022;

    /**
     * Demonstrates ASIC collateral reporting with ISO-20022 projection.
     * The test performs the following steps:
     * - Loads a `ReportableCollateral` from JSON.
     * - Generates an `ASICMarginReport` and validates it.
     * - Projects the report to ISO-20022 format.
     *
     * @throws IOException If there is an error during file reading or processing.
     */
    @Test
    void ASICCollateralWithISOProjectionExampleTest() throws IOException {
        // Load a ReportableCollateral from the input test data
        ReportableCollateral reportableCollateral = ResourcesUtils.getObjectAndResolveReferences(
                ReportableCollateral.class,
                "regulatory-reporting/input/collateral/Collateral-ex01.json"
        );
        assertNotNull(reportableCollateral, "No reportable collateral was found");

        // Generate the ASIC margin report
        ASICMarginReport report = runReport(reportableCollateral);
        assertNotNull(report, "The report is null");

        // Validate the report and print validation results
        ValidationReport validationReport = validator.runProcessStep(ASICMarginReport.class, report.toBuilder());
        ReportingTestUtils.printValidation(validationReport);

        // Project the report to ISO-20022 format
        ISOProjection(report);
    }

    /**
     * Generates an ASIC margin report from a `ReportableCollateral`.
     *
     * @param reportableCollateral The reportable collateral input.
     * @return The generated ASICMarginReport.
     * @throws IOException If there is an error during processing.
     */
    ASICMarginReport runReport(ReportableCollateral reportableCollateral) throws IOException {
        // Create the reporting side (e.g., reporting party and counterparty)
        final ReportingSide reportingSide = ReportingTestUtils.createDefaultReportingSideFromReportableCollateral(reportableCollateral);

        // Create a collateral report instruction from the reportable collateral
        final CollateralReportInstruction reportInstruction = createCollateralReportInstructionFunc.evaluate(reportableCollateral, reportingSide);

        // Generate the ASIC margin report
        final ASICMarginReport report = reportFunc.evaluate(reportInstruction);

        // Print the generated report in JSON format for debugging
        logger.debug(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(report));

        return report;
    }

    /**
     * Projects an `ASICMarginReport` to ISO-20022 format.
     *
     * @param report The ASIC margin report to project.
     * @throws IOException If there is an error during the projection process.
     */
    void ISOProjection(ASICMarginReport report) throws IOException {
        // Project the ASIC margin report to an ISO-20022 document
        Document iso20022Document = asicMarginReportToIso20022.evaluate(report);

        // Load the ISO-20022 configuration path
        URL iso20022Auth108XmlConfig = Resources.getResource(Auth108AsicModelConfig.XML_CONFIG_PATH);

        // Print the ISO-20022 document using the provided configuration
        ReportingTestUtils.logXMLProjection(iso20022Document, iso20022Auth108XmlConfig);
    }
}
