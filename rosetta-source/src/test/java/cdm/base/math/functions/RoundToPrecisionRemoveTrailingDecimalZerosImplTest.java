package cdm.base.math.functions;

import cdm.base.math.RoundingDirectionEnum;
import drr.functions.AbstractFunctionTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoundToPrecisionRemoveTrailingDecimalZerosImplTest extends AbstractFunctionTest {

    @Inject
    private RoundToPrecisionRemoveTrailingZeros func;
    
    @Test
    void shouldRoundToNearest5DecimalPlaces() {
        BigDecimal result = func.doEvaluate(BigDecimal.valueOf(1023.123456789), 5, RoundingDirectionEnum.NEAREST);
        assertEquals("1023.12346", result.toString());
    }

    @Test
    void shouldRoundToNearest5DecimalPlacesStripTrailingZeros() {
        BigDecimal result = func.doEvaluate(BigDecimal.valueOf(1023.12300), 5, RoundingDirectionEnum.NEAREST);
        assertEquals("1023.123", result.toString());
    }

    @Test
    void shouldRoundUpTo5DecimalPlaces() {
        BigDecimal result = func.doEvaluate(BigDecimal.valueOf(1023.123456789), 5, RoundingDirectionEnum.UP);
        assertEquals("1023.12346", result.toString());
    }

    @Test
    void shouldRoundDownTo5DecimalPlaces() {
        BigDecimal result = func.doEvaluate(BigDecimal.valueOf(1023.123456789), 5, RoundingDirectionEnum.DOWN);
        assertEquals("1023.12345", result.toString());
    }

    @Test
    void shouldRoundToNearest0DecimalPlaces() {
        BigDecimal result = func.doEvaluate(BigDecimal.valueOf(1023.123456789), 0, RoundingDirectionEnum.NEAREST);
        assertEquals("1023", result.toString());
    }

    @Test
    void shouldRoundToNearest7DecimalPlaces() {
        BigDecimal result = func.doEvaluate(BigDecimal.valueOf(1023.1), 7, RoundingDirectionEnum.NEAREST);
        assertEquals("1023.1", result.toString());
    }

    @Test
    void shouldRoundDownTo5DecimalPlacesRemoveTrailingZero() {
        BigDecimal result = func.doEvaluate(BigDecimal.valueOf(1023), 5, RoundingDirectionEnum.NEAREST);
        assertEquals("1023", result.toString());
    }

    @Test
    void shouldRoundDownTo4DecimalPlacesRemovingTrailingZeros() {
        BigDecimal result = func.doEvaluate(BigDecimal.valueOf(999999999), 4, RoundingDirectionEnum.NEAREST);
        assertEquals("999999999", result.toString());
    }

    @Test
    void shouldRoundIntegerToNearest5DecimalPlacesStripTrailingZeros() {
        BigDecimal result = func.doEvaluate(BigDecimal.valueOf(1000.0000000), 5, RoundingDirectionEnum.NEAREST);
        // previously rounded to 1E+3
        assertEquals("1000", result.toString());
    }
}