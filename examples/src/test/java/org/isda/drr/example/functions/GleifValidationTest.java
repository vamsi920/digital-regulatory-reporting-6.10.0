package org.isda.drr.example.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.inject.Guice;
import jakarta.inject.Inject;
import com.google.inject.Injector;
import com.regnosys.drr.DrrRuntimeModule;
import com.regnosys.rosetta.common.serialisation.RosettaObjectMapper;
import drr.enrichment.lei.LeiData;
import drr.enrichment.lei.functions.API_GetLeiData;
import org.isda.drr.example.functions.runtime.JavaSamplesDrrRuntimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for validating the behavior of the LEI (Legal Entity Identifier) data retrieval functionality.
 *
 * This class tests the `API_GetLeiData` function using two different runtime modules:
 * 1. The default runtime module (`DrrRuntimeModule`)
 * 2. A custom runtime module (`JavaSamplesDrrRuntimeModule`)
 *
 * Each test case ensures that the LEI data retrieval behaves as expected in the specified runtime context.
 */
final class GleifValidationTest {

    // Logger for outputting test information and results
    private static final Logger logger = LoggerFactory.getLogger(GleifValidationTest.class);

    // JSON writer used for pretty-printing test results
    private static ObjectWriter writer;

    // The API function being tested for retrieving LEI data
    @Inject
    private API_GetLeiData getLeiDataFunc;

    /**
     * One-time setup method to initialize the JSON writer.
     * This method is executed before any test runs.
     */
    @BeforeAll
    public static void setUpOnce() {
        writer = RosettaObjectMapper.getNewRosettaObjectMapper().writerWithDefaultPrettyPrinter();
    }

    /**
     * Test the LEI data retrieval functionality using the default runtime module (`DrrRuntimeModule`).
     *
     * @throws JsonProcessingException if there is an error during JSON serialization
     */
    @Test
    void mustGetLEIDataWithDefaultRuntime() throws JsonProcessingException {
        // Initialize the default runtime module and inject dependencies
        Injector injector = Guice.createInjector(new DrrRuntimeModule());
        injector.injectMembers(this);

        // Call the function to retrieve LEI data for a sample LEI code
        LeiData data = getLeiDataFunc.evaluate("549300OL8KL0WCQ34V31");

        // Verify the output matches the expected empty JSON object
        assertEquals("{ }", writer.writeValueAsString(data), "Should retrieve empty LeiData");

        // Log the result for debugging and verification
        logger.info(writer.writeValueAsString(data));
    }

    /**
     * Test the LEI data retrieval functionality using a custom runtime module (`JavaSamplesDrrRuntimeModule`).
     *
     * @throws JsonProcessingException if there is an error during JSON serialization
     */
    @Test
    void mustGetLEIDataWithCustomRuntime() throws JsonProcessingException {
        // Initialize the custom runtime module and inject dependencies
        Injector injector = Guice.createInjector(new JavaSamplesDrrRuntimeModule());
        injector.injectMembers(this);

        // Call the function to retrieve LEI data for a sample LEI code
        LeiData data = getLeiDataFunc.evaluate("549300OL8KL0WCQ34V31");

        // Verify the output matches the expected entity name defined in the custom runtime
        assertEquals("Java samples custom resolution", data.getEntityName(), "Should match test scenario runtime binding response");

        // Log the result for debugging and verification
        logger.info(writer.writeValueAsString(data));
    }
}
