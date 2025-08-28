package org.isda.drr.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import com.regnosys.rosetta.common.hashing.ReferenceResolverProcessStep;
import com.regnosys.rosetta.common.serialisation.RosettaObjectMapper;
import com.regnosys.rosetta.common.validation.RosettaTypeValidator;
import com.regnosys.rosetta.common.validation.ValidationReport;
import com.rosetta.model.lib.RosettaModelObject;
import drr.enrichment.common.margin.functions.Create_CollateralReportInstruction;
import drr.enrichment.common.trade.functions.Create_TransactionReportInstruction;
import drr.enrichment.common.valuation.functions.Create_ValuationReportInstruction;
import org.isda.cdm.processor.CdmReferenceConfig;

/**
 * Abstract base class for reporting-related tests.
 * This class extends {@link AbstractExampleTest} and provides shared dependencies for
 * creating transaction, collateral, and valuation report instructions.
 * Subclasses can use these injected functions for their test implementations.
 */
public abstract class AbstractReportingTest extends AbstractExampleTest {

    @Inject
    protected RosettaTypeValidator validator;

    protected static final ObjectMapper mapper = RosettaObjectMapper.getNewRosettaObjectMapper();

    // Function to create transaction report instructions
    @Inject
    protected Create_TransactionReportInstruction createTransactionReportInstructionFunc;

    // Function to create collateral report instructions
    @Inject
    protected Create_CollateralReportInstruction createCollateralReportInstructionFunc;

    // Function to create valuation report instructions
    @Inject
    protected Create_ValuationReportInstruction createValuationReportInstruction;

    /**
     * Validates a Rosetta model object by applying CDM validation rules.
     *
     * This method runs validation processes defined in the CDM framework to check
     * the correctness and consistency of the given object against its schema.
     *
     * @param input The Rosetta model object to be validated.
     * @param <T>   The type of the Rosetta model object.
     * @return A ValidationReport containing validation results, including errors
     *         and warnings, if any.
     */
    protected <T extends RosettaModelObject> ValidationReport validate(T input) {
        // Run validation on the input object using its type.
        return validator.runProcessStep(input.getClass(), input);
    }

    /**
     * Resolves references within a Rosetta model object to ensure consistency and completeness.
     *
     * The resolution process ensures that all references within the object are valid and
     * points to the correct entities or objects in the model. This step is necessary
     * before validation or qualification in cases where objects have unresolved references.
     *
     * @param object The Rosetta model object with references to be resolved.
     * @param <T>    The type of the Rosetta model object.
     * @return A new instance of the Rosetta model object with resolved references.
     */
    protected static <T extends RosettaModelObject> T resolveReferences(T object) {
        // Convert the object to its builder representation for modification.
        RosettaModelObject builder = object.toBuilder();
        // Resolve references using the CDM reference resolution configuration.
        new ReferenceResolverProcessStep(CdmReferenceConfig.get()).runProcessStep(builder.getType(), builder);
        // Build and return the updated object with resolved references.
        return (T) builder.build();
    }
}
