package com.regnosys.drr.analytics;

import com.regnosys.rosetta.common.transform.TestPackModel;
import com.rosetta.model.lib.RosettaModelObject;

import java.util.List;

public interface ValidationSummaryProcessor<IN extends RosettaModelObject> {
    List<ValidationData> processValidation(Class<?> functionType, Class<IN> inputType, List<TestPackModel> pipelineTestPackModels);
}

