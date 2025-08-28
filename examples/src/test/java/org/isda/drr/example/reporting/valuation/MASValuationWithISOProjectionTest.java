package org.isda.drr.example.reporting.valuation;

import com.google.common.io.Resources;
import jakarta.inject.Inject;
import com.regnosys.drr.examples.util.ResourcesUtils;
import com.regnosys.rosetta.common.validation.ValidationReport;
import drr.projection.iso20022.mas.rewrite.valuation.functions.Project_MASValuationReportToIso20022;
import drr.regulation.common.ReportableValuation;
import drr.regulation.common.ReportingSide;
import drr.regulation.common.ValuationReportInstruction;
import drr.regulation.mas.rewrite.valuation.MASValuationReport;
import drr.regulation.mas.rewrite.valuation.reports.MASValuationReportFunction;
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
 * Test class to demonstrate MAS valuation reporting with ISO-20022 projection.
 * This class validates the transformation of a `ReportableValuation` to a `MASValuationReport`,
 * followed by validation and ISO-20022 projection.
 */
final class MASValuationWithISOProjectionTest extends AbstractReportingTest {

    private static final Logger logger = LoggerFactory.getLogger(MASValuationWithISOProjectionTest.class);

    // Function to project a MAS valuation report to ISO-20022 format
    @Inject
    Project_MASValuationReportToIso20022 masValuationReportToIso20022;

    // Function to generate a MAS valuation report from a ValuationReportInstruction
    @Inject
    MASValuationReportFunction reportFunc;

    /**
     * Demonstrates MAS valuation reporting with ISO-20022 projection.
     * The test performs the following steps:
     * - Loads a `ReportableValuation` from JSON.
     * - Generates a `MASValuationReport` and validates it.
     * - Projects the report to ISO-20022 format.
     *
     * @throws IOException If there is an error during file reading or writing.
     */
    @Test
    void MasValuationWithISOProjectionExampleTest() throws IOException {
        // Load a ReportableValuation from the input test data
        ReportableValuation reportableValuation = ResourcesUtils.getObjectAndResolveReferences(
                ReportableValuation.class,
                "regulatory-reporting/input/valuation/Valuation-ex01.json"
        );
        assertNotNull(reportableValuation, "No reportable valuation was found");

        // Generate the MAS valuation report
        MASValuationReport report = runReport(reportableValuation);
        assertNotNull(report, "The report is null");

        // Validate the report and print validation results
        ValidationReport validationReport = validator.runProcessStep(MASValuationReport.class, report.toBuilder());
        ReportingTestUtils.printValidation(validationReport);

        // Project the report to ISO-20022 format
        ISOProjection(report);
    }

    /**
     * Generates a MAS valuation report from a `ReportableValuation`.
     *
     * @param reportableValuation The reportable valuation input.
     * @return The generated MASValuationReport.
     * @throws IOException If there is an error during processing.
     */
    MASValuationReport runReport(ReportableValuation reportableValuation) throws IOException {
        // Create the reporting side (e.g., reporting party and counterparty)
        final ReportingSide reportingSide = ReportingTestUtils.createDefaultReportingSideFromReportableValuation(reportableValuation);

        // Create a valuation report instruction from the reportable valuation
        final ValuationReportInstruction reportInstruction = createValuationReportInstruction.evaluate(reportableValuation, reportingSide);

        // Generate the MAS valuation report
        final MASValuationReport report = reportFunc.evaluate(reportInstruction);

        // Print the generated report in JSON format for debugging
        logger.debug(mapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(report));

        return report;
    }

    /**
     * Projects a `MASValuationReport` to ISO-20022 format.
     *
     * @param report The MAS valuation report to project.
     * @throws IOException If there is an error during the projection process.
     */
    void ISOProjection(MASValuationReport report) throws IOException {
        // Project the MAS valuation report to an ISO-20022 document
        Document iso20022Document = masValuationReportToIso20022.evaluate(report);

        // Load the ISO-20022 configuration path
        URL iso20022Auth030XmlConfig = Resources.getResource(Auth030MasModelConfig.XML_CONFIG_PATH);

        // Print the ISO-20022 document using the provided configuration
        ReportingTestUtils.logXMLProjection(iso20022Document, iso20022Auth030XmlConfig);
    }
}
