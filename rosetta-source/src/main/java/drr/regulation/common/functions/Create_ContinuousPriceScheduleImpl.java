package  drr.regulation.common.functions;

import com.rosetta.model.lib.records.Date;
import drr.standards.iosco.cde.base.price.PricePeriod;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Create_ContinuousPriceScheduleImpl extends Create_ContinuousPriceSchedule {
    @Override
    protected List<PricePeriod.PricePeriodBuilder> doEvaluate(List<? extends PricePeriod> pricePeriods, Date endDate) {
        if (pricePeriods == null || endDate == null) {
            return null;
        }
        LocalDate localEndDate = endDate.toLocalDate();

        // First sort based on start date
        List<? extends PricePeriod> sortedPeriods = pricePeriods.stream().sorted(Comparator.comparing(PricePeriod::getEffectiveDate)).collect(Collectors.toList());

        return IntStream.range(0, sortedPeriods.size()).mapToObj(i -> {
            PricePeriod pricePeriod = sortedPeriods.get(i);
            PricePeriod.PricePeriodBuilder pricePeriodBuilder = toBuilder(pricePeriod);

            LocalDate computedEndDate;
            //This is the last reportable Period
            if (i == sortedPeriods.size() - 1) {
                computedEndDate = localEndDate;
            } else {
                computedEndDate = sortedPeriods.get(i + 1).getEffectiveDate().toLocalDate().minusDays(1);
            }

            if (pricePeriod.getEndDate() == null || pricePeriod.getEndDate().toLocalDate().isBefore(localEndDate)) {
                pricePeriodBuilder.setEndDate(Date.of(computedEndDate));
            }
            return pricePeriodBuilder;
        }).collect(Collectors.toList());
    }
}
