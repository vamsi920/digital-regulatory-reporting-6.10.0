package com.regnosys.drr.projection.csa;

import com.google.common.io.Resources;
import com.regnosys.drr.report.ReportTestRuntimeModule;
import com.regnosys.rosetta.common.transform.TestPackModel;
import com.regnosys.testing.transform.TransformTestExtension;
import drr.projection.dtcc.rds.harmonized.csa.rewrite.valuation.functions.Project_CSAValuationReportToDtccRdsHarmonized;
import iso20022.DtccRdsHarmonizedModelConfig;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.regnosys.rosetta.common.transform.TestPackUtils.PROJECTION_CONFIG_PATH_WITHOUT_ISO20022;

public class CsaValuationDtccRdsHarmonizedReportTest {
    @RegisterExtension
    static TransformTestExtension<Project_CSAValuationReportToDtccRdsHarmonized> testExtension =
            new TransformTestExtension<>(
                    new ReportTestRuntimeModule(),
                    PROJECTION_CONFIG_PATH_WITHOUT_ISO20022,
                    Project_CSAValuationReportToDtccRdsHarmonized.class)
                    .withSchemaValidation(Resources.getResource(DtccRdsHarmonizedModelConfig.SCHEMA_PATH));

    private static Stream<Arguments> inputFiles() {
        return testExtension.getArguments();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("inputFiles")
    void runReport(String testName,
                   String testPackId,
                   TestPackModel.SampleModel sampleModel) {
        testExtension.runTransformAndAssert(testPackId, sampleModel);
    }
}
