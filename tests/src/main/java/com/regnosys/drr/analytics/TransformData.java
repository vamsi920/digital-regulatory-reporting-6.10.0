package com.regnosys.drr.analytics;

import java.util.Objects;

public class TransformData {

    private final Class<?> functionClass;
    private final String modelId;
    private final String schemaPath;

    public TransformData(Class<?> functionClass, String schemaPath) {
        this(functionClass, null, schemaPath);
    }
    
    public TransformData(Class<?> functionClass, String modelId, String schemaPath) {
        this.functionClass = functionClass;
        this.modelId = modelId;
        this.schemaPath = schemaPath;
    }

    public Class<?> getFunctionClass() {
        return functionClass;
    }

    public String getModelId() {
        return modelId;
    }

    public String getSchemaPath() {
        return schemaPath;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TransformData that = (TransformData) o;
        return Objects.equals(functionClass, that.functionClass) && Objects.equals(modelId, that.modelId) && Objects.equals(schemaPath, that.schemaPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(functionClass, modelId, schemaPath);
    }
}
