package com.regnosys.drr.projection.hkma;

import com.google.common.io.Resources;
import com.regnosys.drr.report.ReportTestRuntimeModule;
import com.regnosys.rosetta.common.transform.TestPackModel;
import com.regnosys.testing.transform.TransformTestExtension;
import drr.projection.iso20022.hkma.rewrite.trade.tr.functions.Project_HKMATrTradeReportToIso20022;
import iso20022.Auth030HkmaDtccModelConfig;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.regnosys.rosetta.common.transform.TestPackUtils.PROJECTION_CONFIG_PATH_WITHOUT_ISO20022;

public class HKMATrTradeIso20022ReportTest {
    @RegisterExtension
    static TransformTestExtension<Project_HKMATrTradeReportToIso20022> testExtension =
            new TransformTestExtension<>(
                    new ReportTestRuntimeModule(),
                    PROJECTION_CONFIG_PATH_WITHOUT_ISO20022,
                    Project_HKMATrTradeReportToIso20022.class)
                    .withSchemaValidation(Resources.getResource(Auth030HkmaDtccModelConfig.SCHEMA_PATH));

    @ParameterizedTest(name = "{0}")
    @MethodSource("inputFiles")
    void runReport(String testName,
                   String testPackId,
                   TestPackModel.SampleModel sampleModel) {
        testExtension.runTransformAndAssert(testPackId, sampleModel);
    }

    private static Stream<Arguments> inputFiles() {
        return testExtension.getArguments();
    }
}
