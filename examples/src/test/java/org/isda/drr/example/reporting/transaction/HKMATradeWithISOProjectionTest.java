package org.isda.drr.example.reporting.transaction;

import com.google.common.io.Resources;
import jakarta.inject.Inject;
import com.regnosys.drr.examples.util.ResourcesUtils;
import com.regnosys.rosetta.common.validation.ValidationReport;
import drr.projection.iso20022.hkma.rewrite.trade.dtcc.functions.Project_HKMADtccTradeReportToIso20022;
import drr.projection.iso20022.hkma.rewrite.trade.tr.functions.Project_HKMATrTradeReportToIso20022;
import drr.regulation.common.ReportableEvent;
import drr.regulation.common.ReportingSide;
import drr.regulation.common.TransactionReportInstruction;
import drr.regulation.hkma.rewrite.trade.HKMATransactionReport;
import drr.regulation.hkma.rewrite.trade.reports.HKMATradeReportFunction;
import iso20022.Auth030HkmaDtccModelConfig;
import iso20022.Auth030HkmaTrModelConfig;
import iso20022.auth030.hkma.dtcc.Document;
import org.isda.drr.example.AbstractReportingTest;
import org.isda.drr.example.util.ReportingTestUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;

final class HKMATradeWithISOProjectionTest extends AbstractReportingTest {

    private static final Logger logger = LoggerFactory.getLogger(HKMATradeWithISOProjectionTest.class);

    // Function to generate an HKMA Trade Report from a TransactionReportInstruction
    @Inject
    HKMATradeReportFunction reportFunc;

    // Function to project an HKMA Trade Report to Dtcc ISO-20022 format
    @Inject
    Project_HKMADtccTradeReportToIso20022 hkmaDtccTradeReportToIso20022;

    // Function to project an HKMA Trade Report to Tr ISO-20022 format
    @Inject
    Project_HKMATrTradeReportToIso20022 hkmaTrTradeReportToIso20022;

    /**
     * Demonstrates HKMA trade reporting with Dtcc ISO-20022 projection.
     * The test projects the report to Dtcc ISO-20022 format.
     *
     * @throws IOException If there is an error during file reading or processing.
     */
    @Test
    void HKMADtccTradeWithISOProjectionExampleTest() throws IOException {
        //Get a validated HKMA transaction report
        HKMATransactionReport report = HKMATradeReport();

        // Project the report to Dtcc ISO-20022 format
        dtccISOProjection(report);
    }

    /**
     * Demonstrates HKMA trade reporting with Tr ISO-20022 projection.
     * The test projects the report to Tr ISO-20022 format.
     *
     * @throws IOException If there is an error during file reading or processing.
     */
    @Test
    void HKMATrTradeWithISOProjectionExampleTest() throws IOException {
        //Get a validated HKMA transaction report
        HKMATransactionReport report = HKMATradeReport();

        // Project the report to Tr ISO-20022 format
        hktrISOProjection(report);
    }

    /**
     * Generates an HKMA transaction report from a reportableEvent and validates it
     * The test performs the following steps:
     * - Loads a `ReportableEvent` from JSON.
     * - Generates a `HKMATransactionReport`.
     * - Validates the `HKMATransactionReport`
     *
     * @return The generated HKMATransactionReport after validation.
     * @throws IOException If there is an error during file reading or processing.
     */
    private HKMATransactionReport HKMATradeReport() throws IOException {
        // Load a ReportableEvent from the input test data
        ReportableEvent reportableEvent = ResourcesUtils.getObjectAndResolveReferences(
                ReportableEvent.class,
                "regulatory-reporting/input/rates/IR-IRS-Fixed-Float-ex01.json"
        );
        assertNotNull(reportableEvent, "No reportable event was found");

        // Generate the HKMA transaction report
        HKMATransactionReport report = runReport(reportableEvent);
        assertNotNull(report, "The report is null");

        // Validate the report and print validation results
        ValidationReport validationReport = validator.runProcessStep(HKMATransactionReport.class, report.toBuilder());
        ReportingTestUtils.printValidation(validationReport);

        return report;
    }

    /**
     * Generates an HKMA transaction report from a `ReportableEvent`.
     *
     * @param reportableEvent The reportable event input.
     * @return The generated HKMATransactionReport.
     * @throws IOException If there is an error during processing.
     */
    private HKMATransactionReport runReport(ReportableEvent reportableEvent) throws IOException {
        // Create the reporting side (e.g., reporting party and counterparty)
        final ReportingSide reportingSide = ReportingTestUtils.createDefaultReportingSideFromReportableEvent(reportableEvent);

        // Create a transaction report instruction from the reportable event
        final TransactionReportInstruction reportInstruction = createTransactionReportInstructionFunc.evaluate(reportableEvent, reportingSide);

        // Generate the HKMA transaction report
        final HKMATransactionReport report = reportFunc.evaluate(reportInstruction);

        // Print the generated report in JSON format for debugging
        logger.debug(mapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(report));

        return report;
    }

    /**
     * Projects an `HKMATransactionReport` to Dtcc ISO-20022 format.
     *
     * @param report The HKMA transaction report to project.
     * @throws IOException If there is an error during the projection process.
     */
    void dtccISOProjection(HKMATransactionReport report) throws IOException {
        // Project the HKMA transaction report to an ISO-20022 document
        Document iso20022Document = hkmaDtccTradeReportToIso20022.evaluate(report);

        // Load the ISO-20022 configuration path
        URL iso20022Auth030XmlConfig = Resources.getResource(Auth030HkmaDtccModelConfig.XML_CONFIG_PATH);

        // Print the ISO-20022 document using the provided configuration
        ReportingTestUtils.logXMLProjection(iso20022Document, iso20022Auth030XmlConfig);
    }

    /**
     * Projects an `HKMATransactionReport` to Tr ISO-20022 format.
     *
     * @param report The HKMA transaction report to project.
     * @throws IOException If there is an error during the projection process.
     */
    void hktrISOProjection(HKMATransactionReport report) throws IOException {
        // Project the HKMA transaction report to an ISO-20022 document
        iso20022.auth030.hkma.tr.Document iso20022Document = hkmaTrTradeReportToIso20022.evaluate(report);

        // Load the ISO-20022 configuration path
        URL iso20022Auth030XmlConfig = Resources.getResource(Auth030HkmaTrModelConfig.XML_CONFIG_PATH);

        // Print the ISO-20022 document using the provided configuration
        ReportingTestUtils.logXMLProjection(iso20022Document, iso20022Auth030XmlConfig);
    }
}