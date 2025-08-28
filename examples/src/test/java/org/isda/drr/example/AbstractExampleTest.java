package org.isda.drr.example;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.regnosys.drr.DrrRuntimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

/**
 * Abstract base class for setting up test environments using dependency injection.
 * This class initializes a shared Guice injector with a runtime module and
 * ensures that dependencies are injected into subclasses before each test.
 */
public abstract class AbstractExampleTest {

    // Shared Guice injector for all tests in the hierarchy
    private static Injector injector;

    /**
     * Runs once before all tests to initialize the Guice injector.
     * This ensures that a single injector instance is reused across all tests.
     */
    @BeforeAll
    public static void setUpOnce() {
        injector = Guice.createInjector(new DrrRuntimeModule());
    }

    /**
     * Runs before each test to inject dependencies into the test class instance.
     * Subclasses of AbstractExampleTest will have their dependencies injected automatically.
     */
    @BeforeEach
    public void setUp() {
        injector.injectMembers(this);
    }
}
