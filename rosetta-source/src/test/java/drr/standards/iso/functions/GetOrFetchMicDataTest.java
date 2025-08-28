package drr.standards.iso.functions;

import com.rosetta.model.lib.functions.ModelObjectValidator;
import drr.standards.iso.MicData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

public class GetOrFetchMicDataTest {
    private static final String MIC_1 = "DRSP_1";
    private static final String MIC_2 = "DRSP_2";
    private static final String MIC_3 = "DRSP_3";
    private GetOrFetchMicData func;
    private API_GetMicData apiGetMicData;

    @BeforeEach
    void setup() {
        apiGetMicData = mock(API_GetMicData.class);
        func = new GetOrFetchMicDataForTesting(apiGetMicData, mock(ModelObjectValidator.class));
    }

    @Test
    void shouldCallAPIWithMICDataNotContainingMICAndEnabledFlag() {
        List<MicData> preEnrichedMicData = List.of(MicData.builder().setMic(MIC_1).setMicValidation(true).build());

        func.evaluate(preEnrichedMicData, MIC_2);
        verify(apiGetMicData, times(1)).evaluate(MIC_2);
    }

    @Test
    void shouldCallAPIWithMICDataContainingOnlyEnabledFlag() {
        List<MicData> preEnrichedMicData = List.of(MicData.builder().setMicValidation(true).build());

        func.evaluate(preEnrichedMicData, MIC_1);
        verify(apiGetMicData, times(1)).evaluate(MIC_1);
    }

    @Test
    void shouldCallAPIWithMICData() {
        List<MicData> preEnrichedMicData = List.of();

        func.evaluate(preEnrichedMicData, MIC_1);
        verify(apiGetMicData, times(1)).evaluate(MIC_1);
    }

    @Test
    void shouldCallAPIWithMultipleElementsInMICDataContainingEnabledFlag() {
        MicData expected_MicData = MicData.builder().setMic(MIC_3).build();
        when(apiGetMicData.evaluate(MIC_3)).thenReturn(expected_MicData);
        List<MicData> preEnrichedMicData = List.of(
                MicData.builder().setMic(MIC_1).setMicValidation(true).build(),
                MicData.builder().setMic(MIC_2).setMicValidation(true).build());

        MicData actual_MicData = func.evaluate(preEnrichedMicData, MIC_3);

        assertEquals(expected_MicData, actual_MicData);
    }

    @Test
    void shouldNotCallAPIWithMICDataContainingEmptyListAndEnabledFlag() {
        List<MicData> preEnrichedMicData = List.of(MicData.builder().setMicValidation(true).build());

        final MicData actual_MicData = func.evaluate(preEnrichedMicData, null);
        assertNull(actual_MicData);

        verify(apiGetMicData, never()).doEvaluate(null);
    }

    @Test
    void shouldNotCallAPIWithMICDataContainingEmptyListAndEmptyMIC() {
        List<MicData> preEnrichedMicData = List.of();

        final MicData actual_MicData = func.evaluate(preEnrichedMicData, null);
        assertNull(actual_MicData);
        verify(apiGetMicData, never()).evaluate(null);
    }

    @Test
    void shouldNotCallAPIWithMICDataContainingDisabledFlag() {
        MicData micData = MicData.builder().setMic(MIC_1).setMicValidation(false).build();

        List<MicData> preEnrichedMicData = List.of(micData);

        final MicData actual_MicData = func.evaluate(preEnrichedMicData, MIC_1);
        assertNull(actual_MicData);
        verify(apiGetMicData, never()).evaluate(MIC_1);
    }

    @Test
    void shouldNotCallAPIWithMICDataContainingOnlyDisabledFlag() {
        List<MicData> preEnrichedMicData = List.of(MicData.builder().setMicValidation(false).build());

        final MicData actual_MicData = func.evaluate(preEnrichedMicData, MIC_1);
        assertNull(actual_MicData);
        verify(apiGetMicData, never()).evaluate(MIC_1);
    }

    @Test
    void shouldNotCallAPIWithMICDataContainingEnabledFlag() {
        MicData micData = MicData.builder().setMic(MIC_1).setMicValidation(true).build();

        List<MicData> preEnrichedMicData = List.of(micData);

        final MicData actual_MicData = func.evaluate(preEnrichedMicData, MIC_1);
        assertEquals(micData, actual_MicData);
        verify(apiGetMicData, never()).evaluate(MIC_1);
    }


    @Test
    void shouldNotCallAPIWithMICDataContainingEnabledFlagAndEmptyMIC() {
        List<MicData> preEnrichedMicData = List.of(MicData.builder().setMic(MIC_1).setMicValidation(true).build());

        final MicData actual_MicData = func.evaluate(preEnrichedMicData, null);
        assertNull(actual_MicData);
        verify(apiGetMicData, never()).evaluate(null);
    }

    @Test
    void shouldNotCallAPIWithMICDataContainingDisabledFlagAndEmptyMIC() {
        List<MicData> preEnrichedMicData = List.of(MicData.builder().setMic(MIC_1).setMicValidation(false).build());

        final MicData actual_MicData = func.evaluate(preEnrichedMicData, null);
        assertNull(actual_MicData);
        verify(apiGetMicData, never()).evaluate(null);
    }

    @Test
    void shouldNotCallAPIWithMultipleElementsInMICDataContainingEnabledAndDisabledFlag() {
        MicData.MicDataBuilder expected_MicData = MicData.builder().setMic(MIC_2).setMicValidation(true);
        List<MicData> preEnrichedMicData = List.of(
                MicData.builder().setMic(MIC_1).setMicValidation(false).build(),
                expected_MicData.build());

        MicData actual_MicData = func.evaluate(preEnrichedMicData, MIC_2);

        verify(apiGetMicData, never()).evaluate(MIC_2);
        assertEquals(expected_MicData, actual_MicData);
    }

    static class GetOrFetchMicDataForTesting extends GetOrFetchMicData.GetOrFetchMicDataDefault {
        GetOrFetchMicDataForTesting(API_GetMicData aPI_GetMicData, ModelObjectValidator objectValidator) {
            this.aPI_GetMicData = aPI_GetMicData;
            this.objectValidator = objectValidator;
        }
    }
}
