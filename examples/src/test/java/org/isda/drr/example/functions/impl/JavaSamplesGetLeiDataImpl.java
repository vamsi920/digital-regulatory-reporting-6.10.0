package org.isda.drr.example.functions.impl;

import drr.enrichment.lei.LeiData;
import drr.enrichment.lei.functions.API_GetLeiData;

public class JavaSamplesGetLeiDataImpl extends API_GetLeiData {

    protected LeiData.LeiDataBuilder doEvaluate(String s) {
        return LeiData.builder().setEntityName("Java samples custom resolution");
    }
}