package drr.enrichment.lei.functions;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.regnosys.drr.utils.gleif.GleifLeiAdaptor;
import com.regnosys.drr.utils.gleif.GleifLeiClient;
import drr.enrichment.lei.LeiData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class API_GetLeiDataImpl extends API_GetLeiData {

    private static final Logger LOGGER = LoggerFactory.getLogger(API_GetLeiDataImpl.class);

    private final GleifLeiClient gleifLeiClient;
    private final GleifLeiAdaptor gleifLeiAdaptor;

    @VisibleForTesting
    protected final Cache<String, Optional<LeiData>> leiDataCache =
            CacheBuilder.newBuilder()
                    .maximumSize(500)
                    .build();

    public API_GetLeiDataImpl(HttpClient httpClient) {
        this(new GleifLeiClient(httpClient), new GleifLeiAdaptor());
    }

    public API_GetLeiDataImpl() {
        this(new GleifLeiClient(), new GleifLeiAdaptor());
    }

    public API_GetLeiDataImpl(GleifLeiClient gleifLeiClient, GleifLeiAdaptor gleifLeiAdaptor) {
        this.gleifLeiClient = gleifLeiClient;
        this.gleifLeiAdaptor = gleifLeiAdaptor;
    }

    public API_GetLeiDataImpl(Map<String, String> preloadLeiData) {
        this();
        preloadCache(preloadLeiData);
    }

    private void preloadCache(Map<String, String> preloadLeiData) {
        preloadLeiData.forEach((lei, value) -> {
            Optional<LeiData> leiData =
                    Optional.ofNullable(value)
                            .flatMap(jsonResponse ->
                                    Optional.ofNullable(gleifLeiAdaptor.adapt(jsonResponse)));
            leiDataCache.put(lei, leiData);
        });
    }

    public Optional<LeiData> getResultForLei(String lei) {
        GleifLeiClient.GleifLeiResult leiDataFromGleif = gleifLeiClient.getLeiDataFromGleif(lei);
        return leiDataFromGleif.getData()
                .map(gleifLeiAdaptor::adapt);
    }

    @Override
    protected LeiData.LeiDataBuilder doEvaluate(String lei) {
        return Optional.ofNullable(lei)
                .filter(gleifLeiClient::isValidLei)
                .flatMap(x -> {
                    try {
                        Optional<LeiData> leiData;
                        if (leiDataCache.asMap().containsKey(x)) {
                            //get from cache if exists
                            leiData = leiDataCache.getIfPresent(x);
                        } else {
                            //else get from GLEIF
                            leiData = leiDataCache.get(x, () -> getResultForLei(x));
                        }
                        if (leiData.isEmpty()) {
                            LOGGER.debug("LEI data not found for {}", lei);
                        }
                        return leiData;
                    } catch (ExecutionException e) {
                        LOGGER.error("LEI record cache exception", e);
                        return Optional.empty();
                    }
                })
                .map(LeiData::toBuilder)
                .orElse(null);
    }

}

