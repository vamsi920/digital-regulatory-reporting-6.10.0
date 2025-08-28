package drr.enrichment.lei.functions;

import com.rosetta.model.lib.functions.ModelObjectValidator;
import drr.enrichment.lei.LeiData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

public class GetOrFetchLeiDataTest {
    private static final String LEI_1 = "529900W18LQJJN6SJ336";
    private static final String LEI_2 = "529900W18LQJJN6SJ339";
    private static final String LEI_3 = "529900W18LQJJN6SJ340";
    private GetOrFetchLeiData func;
    private API_GetLeiData apiGetLeiData;

    @BeforeEach
    void setup() {
        apiGetLeiData = mock(API_GetLeiData.class);
        func = new GetOrFetchLeiDataForTesting(apiGetLeiData, mock(ModelObjectValidator.class));
    }

    @Test
    void shouldCallAPIWithLEIDataNotContainingLEIAndEnabledFlag() {
        List<LeiData> preEnrichedLeiData = List.of(LeiData.builder().setLei(LEI_1).setLeiValidation(true).build());

        func.evaluate(preEnrichedLeiData, LEI_2);
        verify(apiGetLeiData, times(1)).evaluate(LEI_2);
    }

    @Test
    void shouldCallAPIWithLEIDataContainingOnlyEnabledFlag() {
        List<LeiData> preEnrichedLeiData = List.of(LeiData.builder().setLeiValidation(true).build());

        func.evaluate(preEnrichedLeiData, LEI_1);
        verify(apiGetLeiData, times(1)).evaluate(LEI_1);
    }

    @Test
    void shouldCallAPIWithLEIData() {
        List<LeiData> preEnrichedLeiData = List.of();

        func.evaluate(preEnrichedLeiData, LEI_1);
        verify(apiGetLeiData, times(1)).evaluate(LEI_1);
    }

    @Test
    void shouldCallAPIWithMultipleElementsInLEIDataContainingEnabledFlag() {
        LeiData expected_leiData = LeiData.builder().setLei(LEI_3).build();
        when(apiGetLeiData.evaluate(LEI_3)).thenReturn(expected_leiData);
        List<LeiData> preEnrichedLeiData = List.of(
                LeiData.builder().setLei(LEI_1).setLeiValidation(true).build(),
                LeiData.builder().setLei(LEI_2).setLeiValidation(true).build());

        LeiData actual_leiData = func.evaluate(preEnrichedLeiData, LEI_3);

        assertEquals(expected_leiData, actual_leiData);
    }

    @Test
    void shouldNotCallAPIWithLEIDataContainingEmptyListAndEnabledFlag() {
        List<LeiData> preEnrichedLeiData = List.of(LeiData.builder().setLeiValidation(true).build());

        final LeiData actual_leiData = func.evaluate(preEnrichedLeiData, null);
        assertNull(actual_leiData);

        verify(apiGetLeiData, never()).evaluate(null);
    }

    @Test
    void shouldNotCallAPIWithLEIDataContainingEmptyListAndEmptyLEI() {
        List<LeiData> preEnrichedLeiData = List.of();

        final LeiData actual_leiData = func.evaluate(preEnrichedLeiData, null);
        assertNull(actual_leiData);
        verify(apiGetLeiData, never()).evaluate(null);
    }

    @Test
    void shouldNotCallAPIWithLEIDataContainingDisabledFlag() {
        LeiData leiData = LeiData.builder().setLei(LEI_1).setLeiValidation(false).build();

        List<LeiData> preEnrichedLeiData = List.of(leiData);

        final LeiData actual_leiData = func.evaluate(preEnrichedLeiData, LEI_1);
        assertNull(actual_leiData);
        verify(apiGetLeiData, never()).evaluate(LEI_1);
    }

    @Test
    void shouldNotCallAPIWithLEIDataContainingOnlyDisabledFlag() {
        List<LeiData> preEnrichedLeiData = List.of(LeiData.builder().setLeiValidation(false).build());

        final LeiData actual_leiData = func.evaluate(preEnrichedLeiData, LEI_1);
        assertNull(actual_leiData);
        verify(apiGetLeiData, never()).evaluate(LEI_1);
    }

    @Test
    void shouldNotCallAPIWithLEIDataContainingEnabledFlag() {
        LeiData leiData = LeiData.builder().setLei(LEI_1).setLeiValidation(true).build();

        List<LeiData> preEnrichedLeiData = List.of(leiData);

        final LeiData actual_leiData = func.evaluate(preEnrichedLeiData, LEI_1);
        assertEquals(leiData, actual_leiData);
        verify(apiGetLeiData, never()).evaluate(LEI_1);
    }


    @Test
    void shouldNotCallAPIWithLEIDataContainingEnabledFlagAndEmptyLEI() {
        List<LeiData> preEnrichedLeiData = List.of(LeiData.builder().setLei(LEI_1).setLeiValidation(true).build());

        final LeiData actual_leiData = func.evaluate(preEnrichedLeiData, null);
        assertNull(actual_leiData);
        verify(apiGetLeiData, never()).evaluate(null);
    }

    @Test
    void shouldNotCallAPIWithLEIDataContainingDisabledFlagAndEmptyLEI() {
        List<LeiData> preEnrichedLeiData = List.of(LeiData.builder().setLei(LEI_1).setLeiValidation(false).build());

        final LeiData actual_leiData = func.evaluate(preEnrichedLeiData, null);
        assertNull(actual_leiData);
        verify(apiGetLeiData, never()).evaluate(null);
    }

    @Test
    void shouldNotCallAPIWithMultipleElementsInLEIDataContainingEnabledAndDisabledFlag() {
        final LeiData.LeiDataBuilder expected_leiData = LeiData.builder().setLei(LEI_2).setLeiValidation(true);
        List<LeiData> preEnrichedLeiData = List.of(
                LeiData.builder().setLei(LEI_1).setLeiValidation(false).build(),
                expected_leiData.build());

        LeiData actual_leiData = func.evaluate(preEnrichedLeiData, LEI_2);

        verify(apiGetLeiData, never()).evaluate(LEI_2);
        assertEquals(expected_leiData, actual_leiData);
    }

    static class GetOrFetchLeiDataForTesting extends GetOrFetchLeiData.GetOrFetchLeiDataDefault {
        GetOrFetchLeiDataForTesting(API_GetLeiData apiGetLeiData, ModelObjectValidator objectValidator) {
            this.aPI_GetLeiData = apiGetLeiData;
            this.objectValidator = objectValidator;
        }
    }
}
