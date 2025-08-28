package com.regnosys.drr.dataquality.modifiers;

import com.regnosys.drr.dataquality.util.XmlDom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathExpressionException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static com.regnosys.drr.dataquality.modifiers.TestPackModifierHelper.*;

@SuppressWarnings("unused") // instantiated reflectively
public class PartyTradeInformationModifier extends BaseModifier {

    public static final String PARTY_TRADE_INFORMATION_XPATH = TRADE_HEADER_XPATH + "/partyTradeInformation";
    private static final Logger LOGGER = LoggerFactory.getLogger(PartyTradeInformationModifier.class);

    public PartyTradeInformationModifier(ModifierContext context) {
        super(context);
    }

    private static void addReportingRegime(XmlDom xml, Node partyTradeInformationNode, Node reportingRegimeNode) {
        NodeList childNodes = partyTradeInformationNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (List.of("endUserException", "nonStandardTerms", "largeSizeTrade", "executionType", "executionVenueType", "confirmationMethod").contains(item.getNodeName())) {
                partyTradeInformationNode.insertBefore(reportingRegimeNode, item);
                return;
            }
        }
        xml.addLast(partyTradeInformationNode, reportingRegimeNode);
    }
    private static void addRelatedParty(XmlDom xml, Node partyTradeInformationNode, Node reportingRegimeNode) {
        NodeList childNodes = partyTradeInformationNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (List.of("relatedParty", "category", "collateralizationType", "relatedBusinessUnit"," relatedPerson","intentToAllocate", "executionDateTime", "allocationStatus", "intentToClear", "clearingStatus", "endUserException", "nonStandardTerms", "largeSizeTrade", "executionType", "reportingRegime").contains(item.getNodeName())) {
                partyTradeInformationNode.insertBefore(reportingRegimeNode, item);
                return;
            }
        }
        xml.addLast(partyTradeInformationNode, reportingRegimeNode);
    }

    private static Node createPartyTradeInformation(XmlDom xml, String id) {
        Node partyTradeInformationNode = xml.createNode("partyTradeInformation");
        partyTradeInformationNode.appendChild(xml.createNode("partyReference", Map.of("href", id)));
        partyTradeInformationNode.appendChild(createEmirReportingRegime(xml));
        partyTradeInformationNode.appendChild(createUkEmirReportingRegime(xml));
        partyTradeInformationNode.appendChild(createJfsaReportingRegime(xml));
        return partyTradeInformationNode;

    }

    private static Node createRelatedPartyRole(XmlDom xml, String role, String id) {
        Node relatedParty = xml.createNode("relatedParty");
        relatedParty.appendChild(xml.createNode("partyReference", Map.of("href", id)));
        relatedParty.appendChild(xml.createNode("role", role));
        return relatedParty;
    }

    private static Node createEmirReportingRegime(XmlDom xml) {
        Node reportingRegime = xml.createNode("reportingRegime");
        reportingRegime.appendChild(xml.createNode("name", "EMIR"));
        reportingRegime.appendChild(createSupervisorRegistration(xml, "ESMA"));
        reportingRegime.appendChild(xml.createNode("reportingRole", "ReportingParty"));
        reportingRegime.appendChild(xml.createNode("reportingPurpose", "PrimaryEconomicTerms"));
        reportingRegime.appendChild(xml.createNode("mandatorilyClearable", "false"));
        reportingRegime.appendChild(xml.createNode("exceedsClearingThreshold", "true"));
        reportingRegime.appendChild(xml.createNode("entityClassification", Map.of("entityClassificationScheme", "http://www.fpml.org/coding-scheme/esma-entity-classification"), "Financial"));
        return reportingRegime;
    }

    private static Node createUkEmirReportingRegime(XmlDom xml) {
        Node reportingRegime = xml.createNode("reportingRegime");
        reportingRegime.appendChild(xml.createNode("name", "UKEMIR"));
        reportingRegime.appendChild(createSupervisorRegistration(xml, "FCA"));
        reportingRegime.appendChild(xml.createNode("reportingRole", "ReportingParty"));
        reportingRegime.appendChild(xml.createNode("reportingPurpose", "PrimaryEconomicTerms"));
        reportingRegime.appendChild(xml.createNode("mandatorilyClearable", "false"));
        reportingRegime.appendChild(xml.createNode("exceedsClearingThreshold", "true"));
        reportingRegime.appendChild(xml.createNode("entityClassification", Map.of("entityClassificationScheme", "http://www.fpml.org/coding-scheme/esma-entity-classification"), "Financial"));
        return reportingRegime;
    }

    private static Node createJfsaReportingRegime(XmlDom xml) {
        Node reportingRegime = xml.createNode("reportingRegime");
        reportingRegime.appendChild(xml.createNode("name", "JFSA"));
        reportingRegime.appendChild(createSupervisorRegistration(xml, "JFSA"));
        reportingRegime.appendChild(xml.createNode("reportingRole", "ReportingParty"));
        return reportingRegime;
    }

    private static Node createCsaReportingRegime(XmlDom xml) {
        Node reportingRegime = xml.createNode("reportingRegime");
        reportingRegime.appendChild(xml.createNode("name", "CSA"));
        reportingRegime.appendChild(createSupervisorRegistration(xml, "CA.AB.ASC"));
        reportingRegime.appendChild(xml.createNode("reportingRole", "ReportingParty"));
        reportingRegime.appendChild(xml.createNode("mandatorilyClearable", "false"));
        reportingRegime.appendChild(xml.createNode("entityClassification", Map.of("entityClassificationScheme", "http://www.fpml.org/coding-scheme/cftc-entity-classification"), "SBSD"));
        return reportingRegime;
    }

    private static Node createHKMAReportingRegime(XmlDom xml) {
        Node reportingRegime = xml.createNode("reportingRegime");
        reportingRegime.appendChild(xml.createNode("name", "HKTR"));
        reportingRegime.appendChild(createSupervisorRegistration(xml, "HKMA"));
        reportingRegime.appendChild(xml.createNode("reportingRole", "ReportingParty"));
        reportingRegime.appendChild(xml.createNode("mandatorilyClearable", "false"));
        reportingRegime.appendChild(xml.createNode("entityClassification", "Financial"));
        return reportingRegime;
    }
    
    private static Node createSupervisorRegistration(XmlDom xml, String supervisoryBody) {
        Node supervisorRegistration = xml.createNode("supervisorRegistration");
        supervisorRegistration.appendChild(xml.createNode("supervisoryBody", supervisoryBody));
        return supervisorRegistration;
    }

    private static Node getReportingRegimeNode(XmlDom xml, Node partyTradeInformationNode, String name) throws XPathExpressionException {
        List<Node> reportingRegimeNodes = xml.getList(partyTradeInformationNode, "reportingRegime");
        for (Node reportingRegimeNode : reportingRegimeNodes) {
            if (textContentEquals(xml, reportingRegimeNode, "name", name)) {
                return reportingRegimeNode;
            }
        }
        return null;
    }
    private static Node getRelatedPartyNode(XmlDom xml, Node partyTradeInformationNode, String name) throws XPathExpressionException {
        List<Node> relatedPartyNodes = xml.getList(partyTradeInformationNode, "relatedParty");
        for (Node relatedPartyNode : relatedPartyNodes) {

            if (null != relatedPartyNode && textContentEquals(xml, relatedPartyNode, "role", name)) {
                return relatedPartyNode;
            }
        }
        return null;
    }

    @Override
    public boolean isApplicable(Path xmlFile, String xmlContent, XmlDom xml) throws XPathExpressionException {
        List<Node> partyNodes = xml.getList(PARTY_XPATH);
        final Path fileName = xmlFile.getFileName();
        if (fileName.getFileName().toString().startsWith("mockup-irs-new-trade-notional-amount-schedule.xml"))
            return false;
        else
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
        List<Node> partyTradeInformationNodes = xml.getList(PARTY_TRADE_INFORMATION_XPATH);
        modifyPartyTradeInformation(xml, partyTradeInformationNodes, partyReferenceHrefs.get(0));
        modifyPartyTradeInformation(xml, partyTradeInformationNodes, partyReferenceHrefs.get(1));
    }

    private void modifyPartyTradeInformation(XmlDom xml, List<Node> partyTradeInformationNodes, String id) throws XPathExpressionException {
        Node partyTradeInformationNode = getPartyTradeInformation(xml, partyTradeInformationNodes, id);
        if (partyTradeInformationNode == null) {
            LOGGER.info("Missing partyTradeInformation.partyReference.href {}", id);
            Node tradeHeaderNode = xml.get(TRADE_HEADER_XPATH);
            xml.addAfter(tradeHeaderNode, "partyTradeInformation", createPartyTradeInformation(xml, id));
        } else {
            /*Node executionAgentRelatedPartyNode = getRelatedPartyNode(xml, partyTradeInformationNode, "ExecutionAgent");
            if (executionAgentRelatedPartyNode == null) {
                LOGGER.info("Missing partyTradeInformation.relatedParty.role=ExecutionAgent for {}", id);
                addRelatedParty(xml, partyTradeInformationNode, createRelatedPartyRole(xml,"ExecutionAgent", id));
            }
            Node tradeRepositoryRelatedPartyNode = getRelatedPartyNode(xml, partyTradeInformationNode, "TradeRepository");
            if (tradeRepositoryRelatedPartyNode == null) {
                LOGGER.info("Missing partyTradeInformation.relatedParty.role=TradeRepository for {}", id);
                addRelatedParty(xml, partyTradeInformationNode, createRelatedPartyRole(xml,"TradeRepository", id));
            }*/
            Node emirReportingRegimeNode = getReportingRegimeNode(xml, partyTradeInformationNode, "EMIR");
            if (emirReportingRegimeNode == null) {
                LOGGER.info("Missing partyTradeInformation.reportingRegime.name=EMIR for {}", id);
                addReportingRegime(xml, partyTradeInformationNode, createEmirReportingRegime(xml));
            }
            Node ukEmirReportingRegimeNode = getReportingRegimeNode(xml, partyTradeInformationNode, "UKEMIR");
            if (ukEmirReportingRegimeNode == null) {
                LOGGER.info("Missing partyTradeInformation.reportingRegime.name=UKEMIR for {}", id);
                addReportingRegime(xml, partyTradeInformationNode, createUkEmirReportingRegime(xml));
            }
            Node jfsaReportingRegimeNode = getReportingRegimeNode(xml, partyTradeInformationNode, "JFSA");
            if (jfsaReportingRegimeNode == null) {
                LOGGER.info("Missing partyTradeInformation.reportingRegime.name=JFSA for {}", id);
                addReportingRegime(xml, partyTradeInformationNode, createJfsaReportingRegime(xml));
            }
            Node csaReportingRegimeNode = getReportingRegimeNode(xml, partyTradeInformationNode, "CSA");
            if (csaReportingRegimeNode == null) {
                LOGGER.info("Missing partyTradeInformation.reportingRegime.name=CSA for {}", id);
                addReportingRegime(xml, partyTradeInformationNode, createCsaReportingRegime(xml));
            }
            Node hkmaReportingRegimeNode = getReportingRegimeNode(xml, partyTradeInformationNode, "HKTR");
            if (hkmaReportingRegimeNode == null) {
                LOGGER.info("Missing partyTradeInformation.reportingRegime.name=HKMA for {}", id);
                addReportingRegime(xml, partyTradeInformationNode, createHKMAReportingRegime(xml));
            }
        }
    }

    private Node getPartyTradeInformation(XmlDom xml, List<Node> partyTradeInformationNodes, String id) throws XPathExpressionException {
        for (Node partyTradeInformationNode : partyTradeInformationNodes) {
            Node partyReferenceNode = xml.get(partyTradeInformationNode, "partyReference");
            if (attributeEquals(partyReferenceNode, "href", id)) {
                return partyTradeInformationNode;
            }
        }
        return null;
    }
}
