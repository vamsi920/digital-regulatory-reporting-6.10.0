package com.regnosys.drr;

import cdm.base.math.functions.RoundToPrecisionRemoveTrailingZeros;
import cdm.base.math.functions.RoundToPrecisionRemoveTrailingDecimalZerosImpl;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.regnosys.model.functions.NoOpConditionValidator;
import com.rosetta.model.lib.functions.ConditionValidator;
import drr.enrichment.eic.functions.GetAcceptedEicCodes;
import drr.enrichment.eic.functions.GetAcceptedEicCodesImpl;
import drr.enrichment.upi.functions.FilterEntityIdByScheme;
import drr.enrichment.upi.functions.FilterEntityIdBySchemeImpl;
import drr.regulation.common.functions.*;
import drr.regulation.common.util.functions.*;
import org.finos.cdm.CdmRuntimeModule;

public class DrrRuntimeModule extends CdmRuntimeModule {

    @Override
    protected void configure() {
        super.configure();
        bind(ConditionValidator.class).to(bindConditionValidator());

        bind(GetCommodityKey.class).to(bindGetCommodityKey());
        bind(GetQuantityKeys.class).to(bindGetQuantityKeys());
        bind(GetQuantityReference.class).to(bindGetQuantityReference());
        bind(Create_ContinuousPriceSchedule.class).to(bindCreateContinuousPriceSchedule());
        bind(Create_ContinuousQuantitySchedule.class).to(bindCreateContinuousQuantitySchedule());

        bind(StringLength.class).to(bindStringLength());
        bind(SubString.class).to(bindSubString());
        bind(StringContains.class).to(bindStringContains());
        bind(FilterEntityIdByScheme.class).to(bindFilterEntityIdByScheme());

        bind(GetAcceptedEicCodes.class).to(bindGetAcceptedEicCodes()).asEagerSingleton();
        bind(Key.get(String.class, Names.named(GetAcceptedEicCodesImpl.ACCEPTED_EIC_CODES_PATH)))
                .toInstance(GetAcceptedEicCodesImpl.ACCEPTED_EIC_CODES_CSV);
    }

    protected Class<? extends GetCommodityKey> bindGetCommodityKey() {
        return GetCommodityKeyImpl.class;
    }

    protected Class<? extends GetQuantityKeys> bindGetQuantityKeys() {
        return GetQuantityKeysImpl.class;
    }

    protected Class<? extends GetQuantityReference> bindGetQuantityReference() {
        return GetQuantityReferenceImpl.class;
    }

    protected Class<? extends Create_ContinuousPriceSchedule> bindCreateContinuousPriceSchedule() {
        return Create_ContinuousPriceScheduleImpl.class;
    }

    protected Class<? extends Create_ContinuousQuantitySchedule> bindCreateContinuousQuantitySchedule() {
        return Create_ContinuousQuantityScheduleImpl.class;
    }
    
    protected Class<? extends ConditionValidator> bindConditionValidator() {
        return NoOpConditionValidator.class;
    }

    protected Class<? extends StringLength> bindStringLength() {
        return StringLengthImpl.class;
    }

    protected Class<? extends SubString> bindSubString() {
        return SubStringImpl.class;
    }

    protected Class<? extends StringContains> bindStringContains() {
        return StringContainsImpl.class;
    }

    protected Class<? extends FilterEntityIdByScheme> bindFilterEntityIdByScheme() {
        return FilterEntityIdBySchemeImpl.class;
    }
    
    protected Class<? extends GetAcceptedEicCodes> bindGetAcceptedEicCodes() {
        return GetAcceptedEicCodesImpl.class;
    }

    @Override
    protected Class<? extends RoundToPrecisionRemoveTrailingZeros> bindRoundToPrecisionRemoveTrailingZeros() {
        return RoundToPrecisionRemoveTrailingDecimalZerosImpl.class;
    }
}
