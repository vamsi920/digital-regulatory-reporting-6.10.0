package drr.regulation.common.functions;

import com.rosetta.model.lib.records.Date;
import drr.standards.iosco.cde.base.quantity.NotionalPeriod;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class Create_ContinuousQuantityScheduleImpl extends Create_ContinuousQuantitySchedule {
    @Override
    protected List<NotionalPeriod.NotionalPeriodBuilder> doEvaluate(List<? extends NotionalPeriod> notionalPeriods, Date endDate) {
        if (notionalPeriods == null || endDate == null) {
            return null;
        }
        LocalDate localEndDate = endDate.toLocalDate();

        // First sort based on start date
        List<? extends NotionalPeriod> sortedPeriods = notionalPeriods.stream()
                .sorted(Comparator.comparing(NotionalPeriod::getEffectiveDate))
                .collect(Collectors.toList());

        return IntStream.range(0, sortedPeriods.size()).mapToObj(i -> {
            NotionalPeriod notionalPeriod = sortedPeriods.get(i);
            NotionalPeriod.NotionalPeriodBuilder notionalPeriodBuilder = toBuilder(notionalPeriod);

            LocalDate computedEndDate;
            //This is the last reportable Period
            if (i == sortedPeriods.size() - 1) {
                computedEndDate = localEndDate;
            } else {
                computedEndDate = sortedPeriods.get(i + 1).getEffectiveDate().toLocalDate().minusDays(1);
            }

            if (notionalPeriod.getEndDate() == null || notionalPeriod.getEndDate().toLocalDate().isBefore(localEndDate)) {
                notionalPeriodBuilder.setEndDate(Date.of(computedEndDate));
            }
            return notionalPeriodBuilder;
        }).collect(Collectors.toList());
    }

}
