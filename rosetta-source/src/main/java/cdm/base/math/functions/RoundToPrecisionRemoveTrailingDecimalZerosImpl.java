package cdm.base.math.functions;

import cdm.base.math.RoundingDirectionEnum;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Optional;

public class RoundToPrecisionRemoveTrailingDecimalZerosImpl extends RoundToPrecisionRemoveTrailingZeros {

    @Inject
    private RoundToPrecision roundToPrecision;
    
    // round a supplied value to the specified precision (in decimal places).
    @Override
    protected BigDecimal doEvaluate(BigDecimal value, Integer precision, RoundingDirectionEnum roundingMode) {
        return Optional.ofNullable(roundToPrecision.evaluate(value, precision, roundingMode))
                .map(this::stripTrailingZeros)
                .orElse(null);
    }

    private BigDecimal stripTrailingZeros(BigDecimal roundedValue) {
        String numberStr = roundedValue.toPlainString();  // Avoid scientific notation
        if (numberStr.contains(".")) {
            // Remove trailing zeros from the decimal part
            numberStr = numberStr.replaceAll("0+$", "");
            // If there's a decimal point left, ensure there's at least one digit after it
            if (numberStr.endsWith(".")) {
                numberStr = numberStr.substring(0, numberStr.length() - 1);
            }
        }
        return new BigDecimal(numberStr);
    }
}
