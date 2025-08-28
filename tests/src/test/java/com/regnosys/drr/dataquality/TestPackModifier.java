package com.regnosys.drr.dataquality;

import com.regnosys.drr.dataquality.util.XmlDom;

import java.nio.file.Path;

public interface TestPackModifier {

    boolean isApplicable(Path xmlFile, String xmlContent, XmlDom xml) throws Exception;

    String modify(Path xmlFile, String xmlContent) throws Exception;

    void modify(Path xmlFile, XmlDom xml) throws Exception;
}
