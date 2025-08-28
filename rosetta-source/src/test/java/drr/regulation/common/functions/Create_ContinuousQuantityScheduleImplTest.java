package drr.regulation.common.functions;

import com.rosetta.model.lib.records.Date;
import drr.functions.AbstractFunctionTest;
import drr.standards.iosco.cde.base.quantity.NotionalPeriod;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Create_ContinuousQuantityScheduleImplTest extends AbstractFunctionTest {

    @Inject
    Create_ContinuousQuantitySchedule func;

    @Test
    void shouldReturnContinuousScheduleFromPartiallyPopulatedSchedule() {
        List<NotionalPeriod> scheduleWithStartDatesOnly = List.of(
                createNotionalPeriod(Date.of(2023, 1, 1), null),
                createNotionalPeriod(Date.of(2023, 2, 1), null),
                createNotionalPeriod(Date.of(2023, 3, 1), null));
        Date terminationDate = Date.of(2023, 4, 1);

        List<? extends NotionalPeriod> continuousSchedule = func.evaluate(scheduleWithStartDatesOnly, terminationDate);

        List<NotionalPeriod> expected = List.of(
                createNotionalPeriod(Date.of(2023, 1, 1), Date.of(2023, 1, 31)),
                createNotionalPeriod(Date.of(2023, 2, 1), Date.of(2023, 2, 28)),
                createNotionalPeriod(Date.of(2023, 3, 1), Date.of(2023, 4, 1)));


        assertEquals(continuousSchedule, expected);
    }



    @Test
    void shouldReturnContinuousScheduleFromDiscontinuousSchedule() {
        List<NotionalPeriod> scheduleWithStartDatesOnly = List.of(
                createNotionalPeriod(Date.of(2023, 1, 1), null),
                createNotionalPeriod(Date.of(2023, 2, 1), Date.of(2023, 2, 15)),
                createNotionalPeriod(Date.of(2023, 3, 1), null),
                createNotionalPeriod(Date.of(2023, 4, 1), Date.of(2023, 4, 15)),
                createNotionalPeriod(Date.of(2023, 5, 17), null));
        Date terminationDate = Date.of(2023, 6, 17);

        List<? extends NotionalPeriod> discontinuousSchedule = func.evaluate(scheduleWithStartDatesOnly, terminationDate);

        List<NotionalPeriod> expected = List.of(
                createNotionalPeriod(Date.of(2023, 1, 1), Date.of(2023, 1, 31)),
                createNotionalPeriod(Date.of(2023, 2, 1), Date.of(2023, 2, 28)),
                createNotionalPeriod(Date.of(2023, 3, 1), Date.of(2023, 3, 31)),
                createNotionalPeriod(Date.of(2023, 4, 1), Date.of(2023, 5, 16)),
                createNotionalPeriod(Date.of(2023, 5, 17), Date.of(2023, 6, 17)));


        assertEquals(discontinuousSchedule, expected);
    }

    @Test
    void shouldReturnContinuousScheduleFromFullyPopulatedSchedule() {
        List<NotionalPeriod> scheduleWithStartDatesOnly = List.of(
                createNotionalPeriod(Date.of(2023, 1, 1), Date.of(2023, 1, 31)),
                createNotionalPeriod(Date.of(2023, 2, 1), Date.of(2023, 2, 28)),
                createNotionalPeriod(Date.of(2023, 3, 1), Date.of(2023, 4, 1)));
        Date terminationDate = Date.of(2023, 4, 1);

        List<? extends NotionalPeriod> continuousSchedule = func.evaluate(scheduleWithStartDatesOnly, terminationDate);

        List<NotionalPeriod> expected = List.of(
                createNotionalPeriod(Date.of(2023, 1, 1), Date.of(2023, 1, 31)),
                createNotionalPeriod(Date.of(2023, 2, 1), Date.of(2023, 2, 28)),
                createNotionalPeriod(Date.of(2023, 3, 1), Date.of(2023, 4, 1)));


        assertEquals(continuousSchedule, expected);
    }

    private NotionalPeriod.NotionalPeriodBuilder createNotionalPeriod(Date effectiveDate, Date endDate){
        return NotionalPeriod.builder()
                .setEffectiveDate(effectiveDate)
                .setEndDate(endDate);
    }

}