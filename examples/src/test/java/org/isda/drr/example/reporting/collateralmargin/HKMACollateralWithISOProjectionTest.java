package org.isda.drr.example.reporting.collateralmargin;

import com.google.common.io.Resources;
import iso20022.Auth108HkmaDtccModelConfig;
import iso20022.Auth108HkmaTrModelConfig;
import jakarta.inject.Inject;
import com.regnosys.drr.examples.util.ResourcesUtils;
import com.regnosys.rosetta.common.validation.ValidationReport;
import drr.projection.iso20022.hkma.rewrite.margin.dtcc.functions.Project_HKMADtccMarginReportToIso20022;
import drr.projection.iso20022.hkma.rewrite.margin.tr.functions.Project_HKMATrMarginReportToIso20022;
import drr.regulation.common.*;
import drr.regulation.hkma.rewrite.margin.HKMAMarginReport;
import drr.regulation.hkma.rewrite.margin.reports.HKMAMarginReportFunction;
import org.isda.drr.example.AbstractReportingTest;
import org.isda.drr.example.util.ReportingTestUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;

final class HKMACollateralWithISOProjectionTest extends AbstractReportingTest {

    private static final Logger logger = LoggerFactory.getLogger(HKMACollateralWithISOProjectionTest.class);

    // Function to generate an HKMA Margin Report from a CollateralReportInstruction
    @Inject
    HKMAMarginReportFunction reportFunc;

    // Function to project an HKMA Dtcc margin report to ISO20022 format
    @Inject
    Project_HKMADtccMarginReportToIso20022 hkmaDtccMarginReportToIso20022;

    // Function to project an HKMA Tr margin report to ISO20022 format
    @Inject
    Project_HKMATrMarginReportToIso20022 hkmaTrMarginReportToIso20022;

    /**
     * Demonstrates HKMA margin reporting with Dtcc ISO-20022 projection.
     * The test projects the report to Dtcc ISO-20022 format.
     *
     * @throws IOException If there is an error during file reading or processing.
     */
    @Test
    void HKMADtccCollateralWithISOProjectionExampleTest() throws IOException {
        //Get a validated HKMA margin report
        HKMAMarginReport report = HKMAMarginReport();

        // Project the report to Dtcc ISO-20022 format
        dtccISOProjection(report);
    }

    /**
     * Demonstrates HKMA collateral reporting with Tr ISO-20022 projection.
     * The test projects the report to Tr ISO-20022 format.
     *
     * @throws IOException If there is an error during file reading or processing.
     */
    @Test
    void HKMATrCollateralWithISOProjectionExampleTest() throws IOException {
        //Get a validated HKMA margin report
        HKMAMarginReport report = HKMAMarginReport();

        // Project the report to Tr ISO-20022 format
        hktrISOProjection(report);
    }

    /**
     * Generates an HKMA margin report from a reportableCollateral and validates it
     * The test performs the following steps:
     * - Loads a `ReportableCollateral` from JSON.
     * - Generates a `HKMAMarginReport`.
     * - Validates the `HKMAMarginReport`
     *
     * @return The generated HKMAMarginReport after validation.
     * @throws IOException If there is an error during file reading or processing.
     */
    private HKMAMarginReport HKMAMarginReport() throws IOException {
        // Load a ReportableCollateral from the input test data
        ReportableCollateral reportableCollateral = ResourcesUtils.getObjectAndResolveReferences(
                ReportableCollateral.class,
                "regulatory-reporting/input/collateral/Collateral-ex01.json"
        );
        assertNotNull(reportableCollateral, "No reportable collateral was found");

        // Generate the HKMA margin report
        HKMAMarginReport report = runReport(reportableCollateral);
        assertNotNull(report, "The report is null");

        // Validate the report and print validation results
        ValidationReport validationReport = validator.runProcessStep(HKMAMarginReport.class, report.toBuilder());
        ReportingTestUtils.printValidation(validationReport);

        return report;
    }

    /**
     * Generates an HKMA margin report from a `ReportableCollateral`.
     *
     * @param reportableCollateral The reportable collateral input.
     * @return The generated HKMAMarginReport.
     * @throws IOException If there is an error during processing.
     */
    HKMAMarginReport runReport(ReportableCollateral reportableCollateral) throws IOException {
        // Create the reporting side (e.g., reporting party and counterparty)
        final ReportingSide reportingSide = ReportingTestUtils.createDefaultReportingSideFromReportableCollateral(reportableCollateral);

        // Create a collateral report instruction from the reportable collateral
        final CollateralReportInstruction reportInstruction = createCollateralReportInstructionFunc.evaluate(reportableCollateral, reportingSide);

        // Generate the HKMA margin report
        final HKMAMarginReport report = reportFunc.evaluate(reportInstruction);

        // Print the generated report in JSON format for debugging
        logger.debug(mapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(report));

        return report;
    }

    /**
     * Projects an `HKMAMarginReport` to Dtcc ISO-20022 format.
     *
     * @param report The HKMA margin report to project.
     * @throws IOException If there is an error during the projection process.
     */
    void dtccISOProjection(HKMAMarginReport report) throws IOException {
        // Project the HKMA margin report to an ISO-20022 document
        iso20022.auth108.hkma.dtcc.Document iso20022Document = hkmaDtccMarginReportToIso20022.evaluate(report);

        // Load the ISO-20022 configuration path
        URL iso20022Auth108XmlConfig = Resources.getResource(Auth108HkmaDtccModelConfig.XML_CONFIG_PATH);

        // Print the ISO-20022 document using the provided configuration
        ReportingTestUtils.logXMLProjection(iso20022Document, iso20022Auth108XmlConfig);
    }

    /**
     * Projects an `HKMAMarginReport` to Tr ISO-20022 format.
     *
     * @param report The HKMA margin report to project.
     * @throws IOException If there is an error during the projection process.
     */
    void hktrISOProjection(HKMAMarginReport report) throws IOException {
        // Project the HKMA margin report to an ISO-20022 document
        iso20022.auth108.hkma.tr.Document iso20022Document = hkmaTrMarginReportToIso20022.evaluate(report);

        // Load the ISO-20022 configuration path
        URL iso20022Auth108XmlConfig = Resources.getResource(Auth108HkmaTrModelConfig.XML_CONFIG_PATH);

        // Print the ISO-20022 document using the provided configuration
        ReportingTestUtils.logXMLProjection(iso20022Document, iso20022Auth108XmlConfig);
    }
}
