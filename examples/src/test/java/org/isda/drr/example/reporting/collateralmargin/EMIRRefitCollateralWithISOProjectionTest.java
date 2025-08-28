package org.isda.drr.example.reporting.collateralmargin;

import com.google.common.io.Resources;
import jakarta.inject.Inject;
import com.regnosys.drr.examples.util.ResourcesUtils;
import com.regnosys.rosetta.common.validation.ValidationReport;
import drr.projection.iso20022.esma.emir.refit.margin.functions.Project_EsmaEmirMarginReportToIso20022;
import drr.regulation.common.CollateralReportInstruction;
import drr.regulation.common.ReportableCollateral;
import drr.regulation.common.ReportingSide;
import drr.regulation.esma.emir.refit.margin.ESMAEMIRMarginReport;
import drr.regulation.esma.emir.refit.margin.reports.ESMAEMIRMarginReportFunction;
import iso20022.Auth108EsmaModelConfig;
import iso20022.auth108.esma.Document;
import org.isda.drr.example.AbstractReportingTest;
import org.isda.drr.example.util.ReportingTestUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test class to demonstrate EU EMIR Refit collateral reporting with ISO-20022 projection.
 * This class validates the transformation of a `ReportableCollateral` to an `ESMAEMIRMarginReport`,
 * followed by validation and ISO-20022 projection.
 */
final class EMIRRefitCollateralWithISOProjectionTest extends AbstractReportingTest {

    private static final Logger logger = LoggerFactory.getLogger(EMIRRefitCollateralWithISOProjectionTest.class);

    // Function to generate an EMIR Margin Report from a CollateralReportInstruction
    @Inject
    ESMAEMIRMarginReportFunction reportFunc;

    // Function to project an EMIR Margin Report to ISO-20022 format
    @Inject
    Project_EsmaEmirMarginReportToIso20022 emirMarginReportToIso20022;

    /**
     * Demonstrates EU EMIR Refit collateral reporting with ISO-20022 projection.
     * The test performs the following steps:
     * - Loads a `ReportableCollateral` from JSON.
     * - Generates an `ESMAEMIRMarginReport` and validates it.
     * - Projects the report to ISO-20022 format.
     *
     * @throws IOException If there is an error during file reading or processing.
     */
    @Test
    void EUEMIRCollateralWithISOProjectionExampleTest() throws IOException {
        // Load a ReportableCollateral from the input test data
        ReportableCollateral reportableCollateral = ResourcesUtils.getObjectAndResolveReferences(
                ReportableCollateral.class,
                "regulatory-reporting/input/collateral/Collateral-ex01.json"
        );
        assertNotNull(reportableCollateral, "No reportable collateral was found");

        // Generate the EMIR margin report
        ESMAEMIRMarginReport report = runReport(reportableCollateral);
        assertNotNull(report, "The report is null");

        // Validate the report and print validation results
        ValidationReport validationReport = validator.runProcessStep(ESMAEMIRMarginReport.class, report.toBuilder());
        ReportingTestUtils.printValidation(validationReport);

        // Project the report to ISO-20022 format
        ISOProjection(report);
    }

    /**
     * Generates an EMIR margin report from a `ReportableCollateral`.
     *
     * @param reportableCollateral The reportable collateral input.
     * @return The generated ESMAEMIRMarginReport.
     * @throws IOException If there is an error during processing.
     */
    ESMAEMIRMarginReport runReport(ReportableCollateral reportableCollateral) throws IOException {
        // Create the reporting side (e.g., reporting party and counterparty)
        final ReportingSide reportingSide = ReportingTestUtils.createDefaultReportingSideFromReportableCollateral(reportableCollateral);

        // Create a collateral report instruction from the reportable collateral
        final CollateralReportInstruction reportInstruction = createCollateralReportInstructionFunc.evaluate(reportableCollateral, reportingSide);

        // Generate the EMIR margin report
        final ESMAEMIRMarginReport report = reportFunc.evaluate(reportInstruction);

        // Print the generated report in JSON format for debugging
       logger.debug(mapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(report));

        return report;
    }

    /**
     * Projects an `ESMAEMIRMarginReport` to ISO-20022 format.
     *
     * @param report The EMIR margin report to project.
     * @throws IOException If there is an error during the projection process.
     */
    void ISOProjection(ESMAEMIRMarginReport report) throws IOException {
        // Project the EMIR margin report to an ISO-20022 document
        Document iso20022Document = emirMarginReportToIso20022.evaluate(report);

        // Load the ISO-20022 configuration path
        URL iso20022Auth108XmlConfig = Resources.getResource(Auth108EsmaModelConfig.XML_CONFIG_PATH);

        // Print the ISO-20022 document using the provided configuration
        ReportingTestUtils.logXMLProjection(iso20022Document, iso20022Auth108XmlConfig);
    }
}
