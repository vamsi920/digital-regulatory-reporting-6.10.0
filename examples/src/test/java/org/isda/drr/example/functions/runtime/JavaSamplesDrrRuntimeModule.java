package org.isda.drr.example.functions.runtime;

import com.regnosys.drr.DrrRuntimeModule;
import drr.enrichment.lei.functions.API_GetLeiData;
import org.isda.drr.example.functions.impl.JavaSamplesGetLeiDataImpl;

public class JavaSamplesDrrRuntimeModule extends DrrRuntimeModule {
    @Override
    protected void configure() {
        super.configure();
        bind(API_GetLeiData.class).to(bindGetLeiData());
    }

    protected Class<? extends API_GetLeiData> bindGetLeiData() { return JavaSamplesGetLeiDataImpl.class;}
}
