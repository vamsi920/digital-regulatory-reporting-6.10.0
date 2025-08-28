package drr.enrichment.eic.functions;

import drr.functions.AbstractFunctionTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GetAcceptedEicCodesImplTest  extends AbstractFunctionTest {

    @Inject
    GetAcceptedEicCodes acceptedEicCodesFunc;
    
    @Test
    void shouldPreloadAcceptedEicCodes() {
        List<String> acceptedEicCodes = acceptedEicCodesFunc.evaluate();
        assertEquals(518, acceptedEicCodes.size());
        assertTrue(acceptedEicCodes.contains("10YFI-1--------U"));
        assertTrue(acceptedEicCodes.contains("59WFSRUGOLARTUNH"));
    }

    @Test
    void shouldNotThrowExceptionIfPathNotFound() {
        GetAcceptedEicCodes func = new GetAcceptedEicCodesImpl("/path/not/on/classpath");
        List<String> acceptedEicCodes = func.evaluate();
        assertEquals(0, acceptedEicCodes.size());
    }
}