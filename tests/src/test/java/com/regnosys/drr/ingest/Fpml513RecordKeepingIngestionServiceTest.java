package com.regnosys.drr.ingest;

import com.regnosys.drr.DrrRuntimeModuleExternalApi;
import com.regnosys.ingest.test.framework.ingestor.ExpectationUtil;
import com.regnosys.ingest.test.framework.ingestor.IngestionTest;
import com.regnosys.ingest.test.framework.ingestor.IngestionTestUtil;
import com.regnosys.ingest.test.framework.ingestor.service.IngestionFactory;
import com.regnosys.ingest.test.framework.ingestor.service.IngestionService;
import drr.regulation.common.ReportableEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.provider.Arguments;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

class Fpml513RecordKeepingIngestionServiceTest extends IngestionTest<ReportableEvent> {

    public static final String INSTANCE = "target/FpML_5_13";

    private static final String SAMPLE_FILES_DIR = "cdm-sample-files/fpml-5-13/record-keeping/";

    private static IngestionService ingestionService;

    @BeforeAll
    static void setup() {
        writeActualExpectations = ExpectationUtil.WRITE_EXPECTATIONS;
        ClassLoader classLoader = Fpml513RecordKeepingIngestionServiceTest.class.getClassLoader();
        Collection<URL> ingestURLs = List.of(classLoader.getResource("ingestions/ingestions.json"), classLoader.getResource("ingestions/drr-ingestions.json"));
        DrrRuntimeModuleExternalApi runtimeModule = new DrrRuntimeModuleExternalApi();
        initialiseIngestionFactory(INSTANCE, ingestURLs, runtimeModule, IngestionTestUtil.getPostProcessors(runtimeModule));
        IngestionFactory ingestionFactory = IngestionFactory.getInstance(INSTANCE);
        ingestionService = ingestionFactory.getService("FpML_5_RecordKeeping_To_ReportableEvent");
        //ingestionService = getIngestionService(ingestionFactory.getTranslateOptions("FpML_5_RecordKeeping_To_ReportableEvent"), Fpml513RecordKeepingIngestionServiceTest.class, true);
    }

    @Override
    protected Class<ReportableEvent> getClazz() {
        return ReportableEvent.class;
    }

    @Override
    protected IngestionService ingestionService() {
        return ingestionService;
    }

    @SuppressWarnings("unused")//used by the junit parameterized test
    private static Stream<Arguments> fpMLFiles() {
        return readExpectationsFromPath(SAMPLE_FILES_DIR);
    }
}