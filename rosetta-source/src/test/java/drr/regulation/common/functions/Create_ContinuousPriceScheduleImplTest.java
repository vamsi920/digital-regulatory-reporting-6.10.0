package drr.regulation.common.functions;

import com.rosetta.model.lib.records.Date;
import drr.functions.AbstractFunctionTest;
import drr.standards.iosco.cde.base.price.PricePeriod;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Create_ContinuousPriceScheduleImplTest extends AbstractFunctionTest {

    @Inject
    Create_ContinuousPriceSchedule func;

    @Test
    void shouldReturnContinuousScheduleFromPartiallyPopulatedSchedule() {
        List<PricePeriod> scheduleWithStartDatesOnly = List.of(
                createPricePeriod(Date.of(2023, 1, 1), null),
                createPricePeriod(Date.of(2023, 2, 1), null),
                createPricePeriod(Date.of(2023, 3, 1), null));
        Date terminationDate = Date.of(2023, 4, 1);

        List<? extends PricePeriod> continuousSchedule = func.evaluate(scheduleWithStartDatesOnly, terminationDate);

        List<PricePeriod> expected = List.of(
                createPricePeriod(Date.of(2023, 1, 1), Date.of(2023, 1, 31)),
                createPricePeriod(Date.of(2023, 2, 1), Date.of(2023, 2, 28)),
                createPricePeriod(Date.of(2023, 3, 1), Date.of(2023, 4, 1)));


        assertEquals(continuousSchedule, expected);
    }



    @Test
    void shouldReturnContinuousScheduleFromDiscontinuousSchedule() {
        List<PricePeriod> scheduleWithStartDatesOnly = List.of(
                createPricePeriod(Date.of(2023, 1, 1), null),
                createPricePeriod(Date.of(2023, 2, 1), Date.of(2023, 2, 15)),
                createPricePeriod(Date.of(2023, 3, 1), null),
                createPricePeriod(Date.of(2023, 4, 1), Date.of(2023, 4, 15)),
                createPricePeriod(Date.of(2023, 5, 17), null));
        Date terminationDate = Date.of(2023, 6, 17);

        List<? extends PricePeriod> discontinuousSchedule = func.evaluate(scheduleWithStartDatesOnly, terminationDate);

        List<PricePeriod> expected = List.of(
                createPricePeriod(Date.of(2023, 1, 1), Date.of(2023, 1, 31)),
                createPricePeriod(Date.of(2023, 2, 1), Date.of(2023, 2, 28)),
                createPricePeriod(Date.of(2023, 3, 1), Date.of(2023, 3, 31)),
                createPricePeriod(Date.of(2023, 4, 1), Date.of(2023, 5, 16)),
                createPricePeriod(Date.of(2023, 5, 17), Date.of(2023, 6, 17)));


        assertEquals(discontinuousSchedule, expected);
    }

    @Test
    void shouldReturnContinuousScheduleFromFullyPopulatedSchedule() {
        List<PricePeriod> scheduleWithStartDatesOnly = List.of(
                createPricePeriod(Date.of(2023, 1, 1), Date.of(2023, 1, 31)),
                createPricePeriod(Date.of(2023, 2, 1), Date.of(2023, 2, 28)),
                createPricePeriod(Date.of(2023, 3, 1), Date.of(2023, 4, 1)));
        Date terminationDate = Date.of(2023, 4, 1);

        List<? extends PricePeriod> continuousSchedule = func.evaluate(scheduleWithStartDatesOnly, terminationDate);

        List<PricePeriod> expected = List.of(
                createPricePeriod(Date.of(2023, 1, 1), Date.of(2023, 1, 31)),
                createPricePeriod(Date.of(2023, 2, 1), Date.of(2023, 2, 28)),
                createPricePeriod(Date.of(2023, 3, 1), Date.of(2023, 4, 1)));


        assertEquals(continuousSchedule, expected);
    }

    private PricePeriod.PricePeriodBuilder createPricePeriod(Date effectiveDate, Date endDate){
        return PricePeriod.builder()
                .setEffectiveDate(effectiveDate)
                .setEndDate(endDate);
    }


}