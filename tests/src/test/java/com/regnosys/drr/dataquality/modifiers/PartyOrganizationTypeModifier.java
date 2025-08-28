package com.regnosys.drr.dataquality.modifiers;

import com.regnosys.drr.dataquality.util.XmlDom;
import drr.regulation.common.CFTCEntityClassificationEnum;
import drr.regulation.common.CFTCFederalEntityClassificationEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static com.regnosys.drr.dataquality.modifiers.TestPackModifierHelper.*;

@SuppressWarnings("unused") // instantiated reflectively
public class PartyOrganizationTypeModifier extends BaseModifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyOrganizationTypeModifier.class);

    private static final String SCHEME_NAME = "organizationTypeScheme";
    // organizationType - CFTCEntityClassificationEnum - http://www.fpml.org/coding-scheme/organization-type
    private static final String ENTITY_CLASSIFICATION_SCHEME_VALUE = "http://www.fpml.org/coding-scheme/organization-type";
    // federalEntity - CFTCFederalEntityClassificationEnum - http://www.fpml.org/coding-scheme/cftc-organization-type
    private static final String FEDERAL_ENTITY_CLASSIFICATION_SCHEME_VALUE = "http://www.fpml.org/coding-scheme/cftc-organization-type";

    public PartyOrganizationTypeModifier(ModifierContext context) {
        super(context);
    }

    private static void modifyOrganizationType(Path xmlFile, XmlDom xml, List<Node> partyNodes, String id) throws XPathExpressionException {
        Node partyNode = getPartyNode(partyNodes, id);
        List<Node> organizationTypeNodes = xml.getList(partyNode, "organizationType");

        boolean supportsMultipleOrganizationTypes = isFpml513(xml);

        if (!organizationTypeNodes.isEmpty() && !supportsMultipleOrganizationTypes) {
            return; // FpML 5.10 does not support multiple organization types
        }

        if (organizationTypeNodes.stream().noneMatch(cn -> attributeEquals(cn, SCHEME_NAME, ENTITY_CLASSIFICATION_SCHEME_VALUE) || !cn.hasAttributes())) {
            boolean isClassificationSpecified = xml.getList(partyNode, "classification").stream().anyMatch(n -> !n.hasAttributes());
            boolean isFinancial = xml.getList(partyNode, "classification").stream().anyMatch(n -> !n.hasAttributes() && "Financial".equals(n.getTextContent()));
            CFTCEntityClassificationEnum organizationType = !isClassificationSpecified || isFinancial ? CFTCEntityClassificationEnum.SD : CFTCEntityClassificationEnum.NON_SD_MSP;
            LOGGER.info("Adding missing organizationType (scheme: {}) {} for party {}", ENTITY_CLASSIFICATION_SCHEME_VALUE, organizationType, id);
            Node newOrganizationTypeNode = xml.createNode("organizationType", Map.of(SCHEME_NAME, ENTITY_CLASSIFICATION_SCHEME_VALUE), organizationType.toString());
            addOrganizationTypeNode(xml, partyNode, newOrganizationTypeNode);

            if (!supportsMultipleOrganizationTypes) {
                return; // FpML 5.10 does not support multiple organization types
            }
        }

        if (organizationTypeNodes.stream().noneMatch(cn -> attributeEquals(cn, SCHEME_NAME, FEDERAL_ENTITY_CLASSIFICATION_SCHEME_VALUE))) {
            CFTCFederalEntityClassificationEnum federalEntity = CFTCFederalEntityClassificationEnum.CHARTERED_PURSUANT_TO_FEDERAL_LAW;
            LOGGER.info("Adding missing organizationType (scheme: {}) {} for party {}", FEDERAL_ENTITY_CLASSIFICATION_SCHEME_VALUE, federalEntity, id);
            Node newOrganizationTypeNode = xml.createNode("organizationType", Map.of(SCHEME_NAME, FEDERAL_ENTITY_CLASSIFICATION_SCHEME_VALUE), federalEntity.toString());
            addOrganizationTypeNode(xml, partyNode, newOrganizationTypeNode);
        }
    }

    private static boolean isFpml513(XmlDom xml) throws XPathExpressionException {
        Node node = xml.get("nonpublicExecutionReport");
        return attributeEquals(node, "fpmlVersion", "5-13");
    }

    private static void addOrganizationTypeNode(XmlDom xml, Node partyNode, Node newOrganizationTypeNode) throws XPathExpressionException {
        Node nodeToAddBefore = getNodeToAddBefore(xml, partyNode);
        if (nodeToAddBefore != null) {
            partyNode.insertBefore(newOrganizationTypeNode, nodeToAddBefore);
        } else {
            xml.addLast(partyNode, newOrganizationTypeNode);
        }
    }

    private static Node getNodeToAddBefore(XmlDom xml, Node partyNode) throws XPathExpressionException {
        Node contactInfoNode = xml.get(partyNode, "contactInfo");
        if (contactInfoNode != null) {
            return contactInfoNode;
        }
        Node businessUnitNode = xml.get(partyNode, "businessUnit");
        if (businessUnitNode != null) {
            return businessUnitNode;
        }
        return xml.get(partyNode, "person");
    }

    @Override
    public boolean isApplicable(Path xmlFile, String xmlContent, XmlDom xml) throws XPathExpressionException {
        final Path fileName = xmlFile.getFileName();
        if (fileName.getFileName().toString().startsWith("mockup-irs-new-trade-notional-amount-schedule.xml"))
            return false;
        List<Node> partyNodes = xml.getList(PARTY_XPATH);
        if (partyNodes.isEmpty()) {
            LOGGER.warn("Sample {} has no parties", fileName);
            return false;
        }
        List<String> partyReferenceHrefs = getCounterpartyHrefs(xml);
        if (partyReferenceHrefs.size() != 2) {
            LOGGER.warn("Sample {} does not have 2 counterparties, but has {}", fileName, partyReferenceHrefs.size());
            return false;
        }
        return true;
    }

    @Override
    public void modify(Path xmlFile, XmlDom xml) throws XPathExpressionException {
        List<String> partyReferenceHrefs = getCounterpartyHrefs(xml);
        List<Node> partyNodes = xml.getList(PARTY_XPATH);
        modifyOrganizationType(xmlFile, xml, partyNodes, partyReferenceHrefs.get(0));
        modifyOrganizationType(xmlFile, xml, partyNodes, partyReferenceHrefs.get(1));
    }
}
