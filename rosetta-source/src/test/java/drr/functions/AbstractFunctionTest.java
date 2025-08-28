package drr.functions;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.regnosys.drr.DrrRuntimeModule;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractFunctionTest {
    private static Injector injector;

    @BeforeEach
    public void setUp() {
        injector = Guice.createInjector(new DrrRuntimeModule());
        injector.injectMembers(this);
    }
}
