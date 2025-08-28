package com.regnosys.drr.analytics;

import java.util.Comparator;

public class ValidationData implements Comparable<ValidationData> {

    private final String functionName;
    private final String testPack;
    private final String sampleName;
    private final TypeValidationData inputData;
    private final TypeValidationData outputData;
    private final String xsdSchemaValidationErrors;

    public ValidationData(String functionName,
                          String testPack,
                          String sampleName,
                          TypeValidationData inputData,
                          TypeValidationData outputData,
                          String xsdSchemaValidationErrors) {
        this.functionName = functionName;
        this.testPack = testPack;
        this.sampleName = sampleName;
        this.inputData = inputData;
        this.outputData = outputData;
        this.xsdSchemaValidationErrors = xsdSchemaValidationErrors;
    }

    public String getFunctionName() {
        return functionName;
    }

    public String getTestPack() {
        return testPack;
    }

    public String getSampleName() {
        return sampleName;
    }

    public TypeValidationData getInputData() {
        return inputData;
    }

    public TypeValidationData getOutputData() {
        return outputData;
    }

    public String getXsdSchemaValidationErrors() {
        return xsdSchemaValidationErrors;
    }

    @Override
    public int compareTo(ValidationData o) {
        return Comparator
                .comparing(ValidationData::getFunctionName)
                .thenComparing(ValidationData::getTestPack)
                .thenComparing(ValidationData::getSampleName)
                .compare(this, o);
    }
}
