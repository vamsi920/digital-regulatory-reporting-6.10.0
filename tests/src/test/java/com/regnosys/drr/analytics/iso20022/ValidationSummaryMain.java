package com.regnosys.drr.analytics.iso20022;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.regnosys.drr.analytics.TransformData;
import com.regnosys.drr.report.ReportTestRuntimeModule;
import com.regnosys.rosetta.common.transform.TestPackUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

import static com.regnosys.drr.analytics.iso20022.Iso20022ValidationSummaryCreator.*;
import static com.regnosys.rosetta.common.transform.TestPackUtils.PROJECTION_PATH;


public class ValidationSummaryMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationSummaryMain.class);

    private static final Path ANALYTICS_PATH = PROJECTION_PATH.resolve("analytics");

    private static final List<TransformData> PROJECTIONS =
            List.of(ASIC_TRADE, ASIC_MARGIN,
                    CSA_TRADE, CSA_PPD_TRADE, CSA_VALUATION,
                    EMIR_TRADE, EMIR_MARGIN,
                    FCA_UKEMIR_TRADE, FCA_UKEMIR_MARGIN,
                    HKMA_DTCC_TRADE, HKMA_DTCC_MARGIN, HKMA_DTCC_VALUATION,
                    HKMA_TR_TRADE, HKMA_TR_MARGIN, HKMA_TR_VALUATION,
                    JFSA_TRADE, JFSA_MARGIN,
                    MAS_TRADE, MAS_MARGIN);

    public static void main(String[] args) {
        try {
            Injector injector = Guice.createInjector(new ReportTestRuntimeModule());
            Iso20022ValidationSummaryCreator creator = injector.getInstance(Iso20022ValidationSummaryCreator.class);
            creator.generateValidationSummaryAndWriteCsv(PROJECTIONS, TestPackUtils.PROJECTION_CONFIG_PATH_WITHOUT_ISO20022, ANALYTICS_PATH);
            System.exit(0);
        } catch (Exception e) {
            LOGGER.error("Error executing {}.main()", ValidationSummaryMain.class.getName(), e);
            System.exit(1);
        }
    }
}
