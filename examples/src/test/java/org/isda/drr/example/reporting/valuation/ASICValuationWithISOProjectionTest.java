package org.isda.drr.example.reporting.valuation;

import com.google.common.io.Resources;
import jakarta.inject.Inject;
import com.regnosys.drr.examples.util.ResourcesUtils;
import com.regnosys.rosetta.common.validation.ValidationReport;
import drr.projection.iso20022.asic.rewrite.valuation.functions.Project_ASICValuationReportToIso20022;
import drr.regulation.asic.rewrite.valuation.ASICValuationReport;
import drr.regulation.asic.rewrite.valuation.reports.ASICValuationReportFunction;
import drr.regulation.common.ReportableValuation;
import drr.regulation.common.ReportingSide;
import drr.regulation.common.ValuationReportInstruction;
import iso20022.Auth030AsicModelConfig;
import iso20022.auth030.asic.Document;
import org.isda.drr.example.AbstractReportingTest;
import org.isda.drr.example.util.ReportingTestUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test class to demonstrate ASIC valuation reporting with ISO-20022 projection.
 * The test validates the transformation of a `ReportableValuation` to an `ASICValuationReport`,
 * followed by validation and ISO-20022 projection.
 */
final class ASICValuationWithISOProjectionTest extends AbstractReportingTest {

    private static final Logger logger = LoggerFactory.getLogger(ASICValuationWithISOProjectionTest.class);

    // Function to generate an ASICValuationReport from a ValuationReportInstruction.
    @Inject
    ASICValuationReportFunction reportFunc;

    @Inject
    Project_ASICValuationReportToIso20022 asicValuationReportToIso20022;

    /**
     * Demonstrates ASIC valuation reporting with ISO-20022 projection.
     * The test performs the following steps:
     * - Loads a `ReportableValuation` from JSON.
     * - Generates an `ASICValuationReport` and validates it.
     * - Projects the report to ISO-20022 format.
     *
     * @throws IOException If there is an error during file reading or writing.
     */
    @Test
    void AsicValuationWithISOProjectionExampleTest() throws IOException {
        // Load a ReportableValuation from the input test data.
        ReportableValuation reportableValuation = ResourcesUtils.getObjectAndResolveReferences(
                ReportableValuation.class,
                "regulatory-reporting/input/valuation/Valuation-ex01.json"
        );
        assertNotNull(reportableValuation, "No reportable event was found");

        // Generate the ASIC valuation report.
        ASICValuationReport report = runReport(reportableValuation);
        assertNotNull(report, "The report is null");

        // Validate the report and print validation results.
        ValidationReport validationReport = validator.runProcessStep(ASICValuationReport.class, report.toBuilder());
        ReportingTestUtils.printValidation(validationReport);

        // Project the report to ISO-20022 format.
        ISOProjection(report);
    }

    /**
     * Generates an ASIC valuation report from a `ReportableValuation`.
     *
     * @param reportableValuation The reportable valuation input.
     * @return The generated ASICValuationReport.
     * @throws IOException If there is an error during processing.
     */
    ASICValuationReport runReport(ReportableValuation reportableValuation) throws IOException {
        // Create the reporting side (e.g., reporting party and counterparty).
        final ReportingSide reportingSide = ReportingTestUtils.createDefaultReportingSideFromReportableValuation(reportableValuation);

        // Create a valuation report instruction from the reportable valuation.
        final ValuationReportInstruction reportInstruction = createValuationReportInstruction.evaluate(reportableValuation, reportingSide);

        // Generate the ASIC valuation report.
        final ASICValuationReport report = reportFunc.evaluate(reportInstruction);

        // Print the generated report in JSON format for debugging.
        logger.debug(mapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(report));

        return report;
    }

    /**
     * Projects an `ASICValuationReport` to ISO-20022 format.
     *
     * @param report The ASIC valuation report to project.
     * @throws IOException If there is an error during the projection process.
     */
    void ISOProjection(ASICValuationReport report) throws IOException {

        // Project the ASIC valuation report to an ISO-20022 document.
        Document iso20022Document = asicValuationReportToIso20022.evaluate(report);

        // Load the ISO-20022 configuration path.
        URL iso20022Auth030XmlConfig = Resources.getResource(Auth030AsicModelConfig.XML_CONFIG_PATH);

        // Print the ISO-20022 document using the provided configuration.
        ReportingTestUtils.logXMLProjection(iso20022Document, iso20022Auth030XmlConfig);
    }
}
