package drr.enrichment.eic.functions;

import com.google.common.base.Stopwatch;
import com.regnosys.rosetta.common.util.ClassPathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GetAcceptedEicCodesImpl extends GetAcceptedEicCodes {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetAcceptedEicCodesImpl.class);

    public static final String ACCEPTED_EIC_CODES_PATH = "accepted-eic-codes-path";

    // Downloaded from https://www.acer.europa.eu/remit-documents/remit-reporting-guidance > List of accepted EICs (05/07/2024)
    public static final String ACCEPTED_EIC_CODES_CSV = "regulatory-reporting/lookup/List-of-Accepted-EICs-2024-07-05.csv";

    private final Set<String> acceptedEicCodesCsv = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final String resourceName;

    @Inject
    public GetAcceptedEicCodesImpl(@Named(ACCEPTED_EIC_CODES_PATH) String resourceName) {
        this.resourceName = resourceName;
    }

    @Override
    protected List<String> doEvaluate() {
        if (acceptedEicCodesCsv.isEmpty()) {
            acceptedEicCodesCsv.addAll(loadAcceptedEicCodes(resourceName));
        }
        return new ArrayList<>(acceptedEicCodesCsv);
    }

    private List<String> loadAcceptedEicCodes(String resourceName) {
        Stopwatch t = Stopwatch.createStarted();
        List<Path> paths = ClassPathUtils
                .loadFromClasspath(resourceName, this.getClass().getClassLoader())
                .collect(Collectors.toList());
        if (paths.isEmpty()) {
            LOGGER.warn("Failed to find Accepted EIC codes CSV path for resource: {}, took {}", resourceName, t);
            return Collections.emptyList();
        } else {
            LOGGER.info("Found {} Accepted EIC codes CSV paths for resource: {}: paths: {}, took {}", paths.size(), resourceName, paths, t);
        }
        Path acceptedEicCodesPath = paths.get(0);
        LOGGER.debug("Accepted EIC codes CSV path: {}", acceptedEicCodesPath);
        try {
            List<String> acceptedEicCodesCsv = Files.readAllLines(acceptedEicCodesPath, StandardCharsets.UTF_8);
            LOGGER.info("Loaded {} Accepted EIC codes from path {}, took {}", acceptedEicCodesCsv.size(), acceptedEicCodesPath, t);
            return acceptedEicCodesCsv;
        } catch (IOException e) {
            LOGGER.error("Error occurred while reading Accepted EIC codes CSV path {} found from resource {}, took {}", acceptedEicCodesPath, resourceName, t, e);
            throw new UncheckedIOException(e);
        }
    }
}
