package drr.enrichment.upi.functions;

import cdm.base.staticdata.party.LegalEntity;
import com.rosetta.model.metafields.FieldWithMetaString;
import com.rosetta.model.metafields.MetaFields;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class FilterEntityIdBySchemeImpl extends FilterEntityIdByScheme {

    @Override
    protected String doEvaluate(LegalEntity legalEntity, String scheme) {
        if (scheme == null) {
            return null;
        }
        List<? extends FieldWithMetaString> entityIds = Optional.ofNullable(legalEntity)
                .map(LegalEntity::getEntityId)
                .orElse(Collections.emptyList());
        return entityIds.stream()
                .filter(e -> schemeMatches(e, scheme))
                .findFirst()
                .map(FieldWithMetaString::getValue)
                .orElse(null);
    }

    private boolean schemeMatches(FieldWithMetaString e, String scheme) {
        return Optional.ofNullable(e.getMeta())
                .map(MetaFields::getScheme)
                .map(s -> s.equals(scheme))
                .orElse(false);
    }
}
