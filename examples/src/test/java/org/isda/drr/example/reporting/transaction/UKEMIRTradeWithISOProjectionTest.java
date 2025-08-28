package org.isda.drr.example.reporting.transaction;

import com.google.common.io.Resources;
import jakarta.inject.Inject;
import com.regnosys.drr.examples.util.ResourcesUtils;
import com.regnosys.rosetta.common.validation.ValidationReport;
import drr.projection.iso20022.fca.ukemir.refit.trade.functions.Project_FcaUkEmirTradeReportToIso20022;
import drr.regulation.common.ReportableEvent;
import drr.regulation.common.ReportingSide;
import drr.regulation.common.TransactionReportInstruction;
import drr.regulation.fca.ukemir.refit.trade.FCAUKEMIRTransactionReport;
import drr.regulation.fca.ukemir.refit.trade.reports.FCAUKEMIRTradeReportFunction;
import iso20022.Auth030FcaModelConfig;
import iso20022.auth030.fca.Document;
import org.isda.drr.example.AbstractReportingTest;
import org.isda.drr.example.util.ReportingTestUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test class to demonstrate UK EMIR trade reporting with ISO-20022 projection.
 * This class validates the transformation of a `ReportableEvent` to a `FCAUKEMIRTransactionReport`,
 * followed by validation and ISO-20022 projection.
 */
final class UKEMIRTradeWithISOProjectionTest extends AbstractReportingTest {

    private static final Logger logger = LoggerFactory.getLogger(UKEMIRTradeWithISOProjectionTest.class);

    // Function to generate a UK EMIR Trade Report from a TransactionReportInstruction
    @Inject
    FCAUKEMIRTradeReportFunction reportFunc;

    // Function to project a UK EMIR Trade Report to ISO-20022 format
    @Inject
    Project_FcaUkEmirTradeReportToIso20022 ukEmirTradeReportToIso20022;

    /**
     * Demonstrates UK EMIR trade reporting with ISO-20022 projection.
     * The test performs the following steps:
     * - Loads a `ReportableEvent` from JSON.
     * - Generates a `FCAUKEMIRTransactionReport` and validates it.
     * - Projects the report to ISO-20022 format.
     *
     * @throws IOException If there is an error during file reading or processing.
     */
    @Test
    void UKEMIRTradeWithISOProjectionExampleTest() throws IOException {
        // Load a ReportableEvent from the input test data
        ReportableEvent reportableEvent = ResourcesUtils.getObjectAndResolveReferences(
                ReportableEvent.class,
                "regulatory-reporting/input/rates/IR-IRS-Fixed-Float-ex01.json"
        );
        assertNotNull(reportableEvent, "No reportable event was found");

        // Generate the UK EMIR transaction report
        FCAUKEMIRTransactionReport report = runReport(reportableEvent);
        assertNotNull(report, "The report is null");

        // Validate the report and print validation results
        ValidationReport validationReport = validator.runProcessStep(FCAUKEMIRTransactionReport.class, report.toBuilder());
        ReportingTestUtils.printValidation(validationReport);

        // Project the report to ISO-20022 format
        ISOProjection(report);
    }

    /**
     * Generates a UK EMIR transaction report from a `ReportableEvent`.
     *
     * @param reportableEvent The reportable event input.
     * @return The generated FCAUKEMIRTransactionReport.
     * @throws IOException If there is an error during processing.
     */
    private FCAUKEMIRTransactionReport runReport(ReportableEvent reportableEvent) throws IOException {
        // Create the reporting side (e.g., reporting party and counterparty)
        final ReportingSide reportingSide = ReportingTestUtils.createDefaultReportingSideFromReportableEvent(reportableEvent);

        // Create a transaction report instruction from the reportable event
        final TransactionReportInstruction reportInstruction = createTransactionReportInstructionFunc.evaluate(reportableEvent, reportingSide);

        // Generate the UK EMIR transaction report
        final FCAUKEMIRTransactionReport report = reportFunc.evaluate(reportInstruction);

        // Print the generated report in JSON format for debugging
        logger.debug(mapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(report));

        return report;
    }

    /**
     * Projects a `FCAUKEMIRTransactionReport` to ISO-20022 format.
     *
     * @param report The UK EMIR transaction report to project.
     * @throws IOException If there is an error during the projection process.
     */
    private void ISOProjection(FCAUKEMIRTransactionReport report) throws IOException {
        // Project the UK EMIR transaction report to an ISO-20022 document
        Document iso20022Document = ukEmirTradeReportToIso20022.evaluate(report);

        // Load the ISO-20022 configuration path
        URL iso20022Auth030XmlConfig = Resources.getResource(Auth030FcaModelConfig.XML_CONFIG_PATH);

        // Print the ISO-20022 document using the provided configuration
        ReportingTestUtils.logXMLProjection(iso20022Document, iso20022Auth030XmlConfig);
    }
}
