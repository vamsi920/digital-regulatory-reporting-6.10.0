package com.regnosys.drr.dataquality.modifiers;

import com.regnosys.drr.dataquality.util.XmlDom;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;
import java.nio.file.Path;
import java.util.List;

public class MessageIdModifier extends BaseModifier {

    private static final String TARGET_NAMESPACE = "http://www.fpml.org/FpML-5/recordkeeping";
    private static final String MESSAGE_ID_XPATH = "//*/messageId";
    private static final String TARGET_ATTRIBUTE = "messageIdScheme";
    private static final String NEW_ATTRIBUTE_VALUE = "http://www.fpml.org/coding-scheme/external/technical-record-id";

    public MessageIdModifier(ModifierContext context) {
        super(context);
    }

    @Override
    public boolean isApplicable(Path xmlFile, String xmlContent, XmlDom xml) throws XPathExpressionException {
        List<Node> tradeHeaderNodes = xml.getList(MESSAGE_ID_XPATH);
        if (tradeHeaderNodes.isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    public void modify(Path xmlFile, XmlDom xml) throws XPathExpressionException {
        Node messageIdNode = xml.get(MESSAGE_ID_XPATH);
        NamedNodeMap attributes = messageIdNode.getAttributes();
        Node namedItem = attributes.getNamedItem(TARGET_ATTRIBUTE);
        if(null != namedItem) {
            if(namedItem.getNodeValue() != NEW_ATTRIBUTE_VALUE) {
                namedItem.setNodeValue(NEW_ATTRIBUTE_VALUE);
            }
        }
    }

}
