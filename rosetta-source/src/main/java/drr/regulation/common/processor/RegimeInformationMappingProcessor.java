package drr.regulation.common.processor;

import cdm.base.staticdata.party.metafields.ReferenceWithMetaParty;
import com.regnosys.rosetta.common.translation.*;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.metafields.FieldWithMetaString;
import drr.regulation.common.*;
import drr.regulation.common.ReportingRegime.ReportingRegimeBuilder;
import drr.regulation.common.metafields.FieldWithMetaRegimeNameEnum;
import drr.regulation.common.metafields.FieldWithMetaSupervisoryBodyEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static com.regnosys.rosetta.common.translation.MappingProcessorUtils.getNonNullMappedValue;
import static com.regnosys.rosetta.common.translation.MappingProcessorUtils.setValueAndOptionallyUpdateMappings;
import static drr.regulation.common.PartyInformation.PartyInformationBuilder;

/**
 * FpML mapping processor.
 * Specs for the mapper:
 * - Access to partyTradeInformation list and extract the partyReference
 * - Relate the partyReference "href" with the party "id" bloc and extract the classification within it
 * - Map it int CDM in ReportableEvent->reportableInformation->partyInformation->regimeInformation->esmaPartyInformation->corporateSector->financialSector/nonFinancialSector->nonFinancialSectorIndicator
 * - Take into account that partyInformation in CDM is a list (1..*), financialSector is type FinancialSectorEnum and nonFinancialSector is a list 0..* of type NonFinancialSector and nonFinancialSectorIndicator is type NonFinancialSectorEnum
 * - For populating into the enums, use the new scheme Jan provided (fpml-schemeDefinitions) in order to decide how to populate the elements.
 */
@SuppressWarnings("unused")
public class RegimeInformationMappingProcessor extends MappingProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegimeInformationMappingProcessor.class);
    private static final String ESMA_CORPORATE_SECTOR_SCHEME = "http://www.fpml.org/coding-scheme/esma-emir-refit-regulatory-corporate-sector";
    private static final String HKMA_CORPORATE_SECTOR_SCHEME = "http://www.fpml.org/coding-scheme/hkma-rewrite-regulatory-corporate-sector";
    private static final String CFTC_ORGANIZATION_TYPE_SCHEME = "http://www.fpml.org/coding-scheme/cftc-organization-type";
    private final SynonymToEnumMap synonymToEnumMap;

    public RegimeInformationMappingProcessor(RosettaPath modelPath, List<Path> synonymPaths, MappingContext context) {
        super(modelPath, synonymPaths, context);
        this.synonymToEnumMap = getContext().getSynonymToEnumMap();
    }

    @Override
    public void map(Path synonymPath, List<? extends RosettaModelObjectBuilder> builders, RosettaModelObjectBuilder parent) {
        PartyInformationBuilder partyInformationBuilder = (PartyInformationBuilder) parent;
        List<ReportingRegimeBuilder> reportingRegimeBuilders = (List<ReportingRegimeBuilder>) builders;

        List<Path> classificationPaths = getPartyPaths(partyInformationBuilder, "classification");
        setCorporateSector(reportingRegimeBuilders, classificationPaths, SupervisoryBodyEnum.ESMA, ESMA_CORPORATE_SECTOR_SCHEME);
        setCorporateSector(reportingRegimeBuilders, classificationPaths, SupervisoryBodyEnum.FCA, ESMA_CORPORATE_SECTOR_SCHEME);
        setCorporateSector(reportingRegimeBuilders, classificationPaths, SupervisoryBodyEnum.HKMA, HKMA_CORPORATE_SECTOR_SCHEME);

        List<Path> organizationTypePaths = getPartyPaths(partyInformationBuilder, "organizationType");

        setTechnicalRecordId(reportingRegimeBuilders);
    }


    private void setCorporateSector(List<ReportingRegimeBuilder> reportingRegimeBuilders, List<Path> classificationPaths, SupervisoryBodyEnum supervisoryBody, String corporateSectorScheme) {
        reportingRegimeBuilders.stream()
                .filter(b -> isSupervisoryBody(b, supervisoryBody))
                .findFirst()
                .ifPresent(reportingRegimeBuilder -> {
                    for (Path classificationPath : classificationPaths) {
                        getNonNullMappedValue(classificationPath.addElement("industryClassificationScheme"), getMappings())
                                .filter(corporateSectorScheme::equals)
                                .ifPresent(industryClassificationScheme ->
                                        setValueAndOptionallyUpdateMappings(classificationPath,
                                                classificationValue -> {
                                                    CorporateSector.CorporateSectorBuilder corporateSectorBuilder = CorporateSector.builder();

                                                    // add financial or non-financial sector
                                                    getFinancialSectorEnum(classificationValue)
                                                            .ifPresent(corporateSectorBuilder::addFinancialSector);
                                                    getNonFinancialSector(classificationValue)
                                                            .ifPresent(corporateSectorBuilder::addNonFinancialSector);

                                                    // add corporate sector
                                                    if (corporateSectorBuilder.hasData()) {
                                                        updateCorporateSectorForRegime(reportingRegimeBuilder, corporateSectorBuilder);
                                                        return true;
                                                    }
                                                    return false;
                                                },
                                                getMappings(),
                                                getModelPath()));
                    }
                });
    }

    private Boolean isFederalEntityScheme(Path organizationTypePath) {
        return getNonNullMappedValue(organizationTypePath.addElement("organizationTypeScheme"), getMappings())
                .map(CFTC_ORGANIZATION_TYPE_SCHEME::equals).orElse(false);
    }

    private void setTechnicalRecordId(List<ReportingRegimeBuilder> reportingRegimeBuilders) {
        List<Path> messageIdSchemePaths = getMappings().stream()
                .map(Mapping::getXmlPath)
                .filter(xmlPath -> xmlPath.endsWith("messageIdScheme"))
                .collect(Collectors.toList());

        for (Path messageIdSchemePath : messageIdSchemePaths) {
            Optional<String> mappedValue = getNonNullMappedValue(messageIdSchemePath, getMappings());
            if (mappedValue.isPresent() && mappedValue.get().equals("http://www.fpml.org/coding-scheme/external/technical-record-id")) {
                Path messageIdPath = messageIdSchemePath.getParent();
                Optional<String> messageId = getNonNullMappedValue(messageIdPath, getMappings());

                messageId.ifPresent(id -> {
                    FieldWithMetaString fieldWithMetaString = FieldWithMetaString.builder().setValue(id).build();
                    reportingRegimeBuilders.stream()
                            .filter(this::isRelevantSupervisoryBody)
                            .forEach(reportingRegimeBuilder -> {
                                if (fieldWithMetaString != null) {
                                    reportingRegimeBuilder.setTechnicalRecordId(fieldWithMetaString);
                                }
                            });
                });
            }
        }
    }

    private boolean isRelevantSupervisoryBody(ReportingRegimeBuilder reportingRegimeBuilder) {
        FieldWithMetaSupervisoryBodyEnum supervisoryBodyField = reportingRegimeBuilder.getSupervisoryBody();
        if (supervisoryBodyField != null) {
            SupervisoryBodyEnum supervisoryBodyEnum = supervisoryBodyField.getValue();
            return supervisoryBodyEnum == SupervisoryBodyEnum.ASIC
                    || supervisoryBodyEnum == SupervisoryBodyEnum.JFSA
                    || supervisoryBodyEnum == SupervisoryBodyEnum.MAS;
        }
        return false;
    }

    private boolean isSupervisoryBody(ReportingRegimeBuilder reportingRegime, SupervisoryBodyEnum supervisoryBody) {
        return Optional.ofNullable(reportingRegime.getSupervisoryBody())
                .map(FieldWithMetaSupervisoryBodyEnum::getValue)
                .map(supervisoryBody::equals)
                .orElse(false);
    }

    private boolean isRegimeName(ReportingRegimeBuilder reportingRegime, RegimeNameEnum regimeName) {
        return Optional.ofNullable(reportingRegime.getRegimeName())
                .map(FieldWithMetaRegimeNameEnum::getValue)
                .map(regimeName::equals)
                .orElse(false);
    }

    private List<Path> getPartyPaths(PartyInformationBuilder partyInformationBuilder, String partyAttribute) {
        return Optional.ofNullable(partyInformationBuilder.getPartyReference())
                .map(ReferenceWithMetaParty::getExternalReference)
                .map(partyReference -> getPathsForParty(partyReference, partyAttribute))
                .orElse(Collections.emptyList());
    }

    private List<Path> getPathsForParty(String partyReference, String partyAttribute) {
        List<Path> paths = new ArrayList<>();

        Optional<Path> partyPath = getPartyPath(partyReference, "party", "id");
        partyPath.ifPresent(path -> {
            String partyPathStr = path.toString();
            for (Mapping mapping : getMappings()) {
                Path xmlPath = mapping.getXmlPath();
                if (mapping.getXmlValue() != null
                        && xmlPath != null
                        && xmlPath.toString().startsWith(partyPathStr)
                        && xmlPath.endsWith(partyAttribute)) {
                    paths.add(xmlPath);
                }
            }
        });

        return paths;
    }

    private Optional<Path> getPartyPath(String xmlValue, String... xmlPathEndsWith) {
        return getMappings().stream()
                .filter(m -> m.getXmlPath().endsWith(xmlPathEndsWith))
                .filter(m -> {
                    Object xmlValueObj = m.getXmlValue();
                    String xmlValueStr = (xmlValueObj != null) ? xmlValueObj.toString() : null;
                    return Objects.equals(xmlValueStr, xmlValue);
                })
                .map(Mapping::getXmlPath) // e.g. nonpublicExecutionReport.party(0).id
                .map(Path::getParent) // e.g. nonpublicExecutionReport.party(0)
                .findFirst();
    }

    private void updateCorporateSectorForRegime(ReportingRegimeBuilder reportingRegimeBuilder, CorporateSector.CorporateSectorBuilder corporateSectorBuilder) {
        SupervisoryBodyEnum supervisoryBody = reportingRegimeBuilder.getSupervisoryBody().getValue();
        switch (supervisoryBody) {
            case ESMA:
                reportingRegimeBuilder.getOrCreateEsmaPartyInformation().setCorporateSector(corporateSectorBuilder);
                break;
            case FCA:
                reportingRegimeBuilder.getOrCreateFcaPartyInformation().setCorporateSector(corporateSectorBuilder);
                break;
            case HKMA:
                reportingRegimeBuilder.getOrCreateHkmaPartyInformation().setCorporateSector(corporateSectorBuilder);
                break;
            default:
                throw new RuntimeException("Cannot updated corporate sector, unexpected supervisoryBody " + supervisoryBody);
        }
    }

    private Optional<FinancialSectorEnum> getFinancialSectorEnum(String value) {
        try {
            return Optional.of(FinancialSectorEnum.valueOf(value));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private Optional<NonFinancialSector> getNonFinancialSector(String value) {
        try {
            return Optional.of(NonFinancialSectorEnum.valueOf(value))
                    .map(e ->
                            NonFinancialSector.builder()
                                    .setNonFinancialSectorIndicator(e)
                                    .setOrdinal(e.ordinal() + 1));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
