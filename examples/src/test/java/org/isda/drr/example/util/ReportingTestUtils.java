package org.isda.drr.example.util;

import cdm.base.staticdata.party.metafields.ReferenceWithMetaParty;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import com.google.inject.Injector;
import com.regnosys.drr.DrrRuntimeModule;
import com.regnosys.rosetta.common.serialisation.RosettaObjectMapperCreator;
import com.regnosys.rosetta.common.validation.ValidationReport;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.validation.ValidationResult;
import drr.enrichment.common.margin.functions.Create_CollateralReportInstruction;
import drr.enrichment.common.trade.functions.Create_TransactionReportInstruction;
import drr.enrichment.common.valuation.functions.Create_ValuationReportInstruction;
import drr.regulation.common.*;
import com.regnosys.rosetta.common.validation.RosettaTypeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for testing reporting processes in the DRR (Data Reporting Repository).
 * Provides helper methods for validation, reporting side creation, and ISO-20022 XML generation.
 */
public class ReportingTestUtils {

    private static final Logger logger = LoggerFactory.getLogger(ReportingTestUtils.class);

    // Injected functions for creating transaction, collateral, and valuation report instructions
    @Inject
    protected Create_TransactionReportInstruction createTransactionReportInstructionFunc;

    @Inject
    protected Create_CollateralReportInstruction createCollateralReportInstructionFunc;

    @Inject
    protected Create_ValuationReportInstruction createValuationReportInstruction;

    // Validator for running validation processes
    @Inject
    protected RosettaTypeValidator validator;

    // Static injector and runtime module
    protected static Injector injector;
    protected static DrrRuntimeModule drrRuntimeModule = new DrrRuntimeModule();

    /**
     * Prints a validation report to the console.
     * Errors are displayed first, followed by successful results.
     *
     * @param validationReport The validation report to be printed.
     * @param <T>              Type of the Rosetta model object being validated.
     */
    public static <T extends RosettaModelObject> void printValidation(ValidationReport validationReport) {
        logger.debug(String.format("\n\n+-------------------+\n| %s |\n+-------------------+%n", "Report Validation"));

        // Sort and print validation results: errors first, then successes.
        validationReport.getValidationResults().stream()
                .sorted(Comparator.comparing(ValidationResult::isSuccess, Boolean::compareTo))
                .map(ValidationResult::toString)
                .forEach(logger::debug);
    }

    /**
     * Creates a default reporting side from a reportable event by extracting distinct party references.
     *
     * @param reportableEvent The reportable event.
     * @param <T>             Type of the Rosetta model object.
     * @return A ReportingSide object representing the reporting party and counterparty.
     */
    public static <T extends RosettaModelObject> ReportingSide createDefaultReportingSideFromReportableEvent(ReportableEvent reportableEvent) {
        // Extract distinct party references
        List<ReferenceWithMetaParty> parties = reportableEvent.getReportableInformation()
                .getPartyInformation()
                .stream()
                .map(PartyInformation::getPartyReference)
                .distinct()
                .collect(Collectors.toList());

        // Assume the first two parties are reporting party and counterparty
        ReferenceWithMetaParty party1 = parties.get(0);
        ReferenceWithMetaParty party2 = parties.get(1);

        // Build and return the ReportingSide
        return ReportingSide.builder()
                .setReportingParty(party1)
                .setReportingCounterparty(party2)
                .build();
    }

    /**
     * Creates a default reporting side from a reportable collateral by extracting distinct party references.
     *
     * @param reportableCollateral The reportable collateral.
     * @param <T>                  Type of the Rosetta model object.
     * @return A ReportingSide object representing the reporting party and counterparty.
     */
    public static <T extends RosettaModelObject> ReportingSide createDefaultReportingSideFromReportableCollateral(ReportableCollateral reportableCollateral) {
        List<ReferenceWithMetaParty> parties = reportableCollateral.getReportableInformation()
                .getPartyInformation()
                .stream()
                .map(PartyInformation::getPartyReference)
                .distinct()
                .collect(Collectors.toList());

        ReferenceWithMetaParty party1 = parties.get(0);
        ReferenceWithMetaParty party2 = parties.get(1);

        return ReportingSide.builder()
                .setReportingParty(party1)
                .setReportingCounterparty(party2)
                .build();
    }

    /**
     * Creates a default reporting side from a reportable valuation by extracting distinct party references.
     *
     * @param reportableValuation The reportable valuation.
     * @param <T>                 Type of the Rosetta model object.
     * @return A ReportingSide object representing the reporting party and counterparty.
     */
    public static <T extends RosettaModelObject> ReportingSide createDefaultReportingSideFromReportableValuation(ReportableValuation reportableValuation) {
        List<ReferenceWithMetaParty> parties = reportableValuation.getReportableInformation()
                .getPartyInformation()
                .stream()
                .map(PartyInformation::getPartyReference)
                .distinct()
                .collect(Collectors.toList());

        ReferenceWithMetaParty party1 = parties.get(0);
        ReferenceWithMetaParty party2 = parties.get(1);

        return ReportingSide.builder()
                .setReportingParty(party1)
                .setReportingCounterparty(party2)
                .build();
    }

    /**
     * Generates XML output for a given document using a specified configuration.
     *
     * @param document  The Rosetta model object to be serialized.
     * @param xmlConfig The URL to the XML configuration.
     * @throws IOException If an error occurs while reading the configuration or writing the output.
     */
    public static void logXMLProjection(RosettaModelObject document, URL xmlConfig) throws IOException {
        logger.debug("XML mapper config path: {}", xmlConfig);

        // Create an XML object mapper using the provided configuration
        ObjectMapper objectMapper = RosettaObjectMapperCreator.forXML(xmlConfig.openStream()).create();

        // Serialize the document to XML and print it with pretty formatting
        logger.info("\n\n+-------------------+\n|     {}     |\n+-------------------+\n{}",
                "XML Report",
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(document)
        );
    }
}
