package com.regnosys.drr.testpack.gleif;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.regnosys.drr.utils.gleif.GleifLeiClient;
import com.regnosys.rosetta.common.serialisation.RosettaObjectMapper;
import com.regnosys.rosetta.common.transform.PipelineModel;
import com.regnosys.rosetta.common.transform.TestPackModel;
import com.regnosys.rosetta.common.util.SimpleProcessor;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.process.AttributeMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class CreateTestPackGleifDataFile {

    public static final ObjectMapper MAPPER = RosettaObjectMapper.getNewRosettaObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateTestPackGleifDataFile.class);

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        Path resourcesPath = Path.of("rosetta-source/src/main/resources");
        Path configRootPath = resourcesPath.resolve("regulatory-reporting/config");
        Multimap<String, TestPackModel> testPacks = Multimaps.index(getConfig(configRootPath, "test-pack", TestPackModel.class), TestPackModel::getPipelineId);
        Map<String, PipelineModel> pipelines = Maps.uniqueIndex(getConfig(configRootPath, "pipeline", PipelineModel.class), PipelineModel::getId);
        Set<String> leiFromSamples = new LinkedHashSet<>();
        for (Map.Entry<String, TestPackModel> entry : testPacks.entries()) {
            PipelineModel pipelineModel = pipelines.get(entry.getKey());
            List<TestPackModel.SampleModel> samples = entry.getValue().getSamples();
            List<Path> allRegInputSamples = samples.stream().map(TestPackModel.SampleModel::getInputPath)
                    .map(x -> resourcesPath.resolve(Path.of(x))).collect(Collectors.toList());
            @SuppressWarnings("unchecked")
            Class<RosettaModelObject> type = (Class<RosettaModelObject>) Class.forName(pipelineModel.getTransform().getInputType());
            Set<String> leis = findLEIsFromSamples(allRegInputSamples, type);
            LOGGER.info("Found LEIs in test pack {} - {}", entry.getValue().getName(), leis);
            leiFromSamples.addAll(leis);
        }

        LOGGER.info("LEIs: {}", leiFromSamples);
        ObjectMapper objectMapper = new ObjectMapper();

        String output = "regulatory-reporting/lookup/test-pack-gleif-data.json";
        Path testPackGleifData = resourcesPath.resolve(output);
        TypeReference<HashMap<String,String>> typeRef
                = new TypeReference<HashMap<String,String>>() {};
        HashMap<String, String> readFromFile = objectMapper.readValue(testPackGleifData.toFile(), typeRef);
        Map<String, String> leiCacheMap = new LinkedHashMap<>(readFromFile);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        GleifLeiClient gleifLeiClient = new GleifLeiClient(HttpClient.newBuilder().executor(executor).build());
        for (String lei : leiFromSamples) {
            if (leiCacheMap.containsKey(lei)) {
                LOGGER.info("Already have data for LEI: {}", lei);
            } else {
                GleifLeiClient.GleifLeiResult leiDataFromGleif = gleifLeiClient.getLeiDataFromGleif(lei);
                if (leiDataFromGleif.getStatus() != GleifLeiClient.Status.API_ERROR) {
                    leiDataFromGleif.getData().ifPresent(s -> leiCacheMap.put(lei, s));
                    leiCacheMap.put(lei, leiDataFromGleif.getData().orElse(null));
                }
            }
        }
        
        LOGGER.info("Writing cache file: {}", output);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(testPackGleifData.toFile(), leiCacheMap);
        executor.shutdown();
        LOGGER.info("Done");

    }

    private static <T> List<T> getConfig(Path configRootPath, String prefix, Class<T> type) throws IOException {
        List<T> models = new ArrayList<>();
        List<Path> allTestPackConfigs = Files.walk(configRootPath)
                .filter(x -> x.getFileName().toString().startsWith(prefix))
                .collect(Collectors.toList());

        for (Path allTestPackConfig : allTestPackConfigs) {
            models.add(MAPPER.readValue(allTestPackConfig.toUri().toURL(), type));
        }
        return models;
    }

    private static <T extends RosettaModelObject> Set<String> findLEIsFromSamples(List<Path> allRegInputSamples, Class<T> type) throws IOException {
        Set<String> linkedHashSet = new LinkedHashSet<>();
        for (Path inputSample : allRegInputSamples) {
            T transactionReportInstruction = MAPPER.readValue(inputSample.toUri().toURL(), type);
            LeiFinder leiFinder = new LeiFinder(linkedHashSet, inputSample.getFileName().toString());
            transactionReportInstruction.process(RosettaPath.valueOf("report"), leiFinder);
        }
        return linkedHashSet;
    }

    private static class LeiFinder extends SimpleProcessor {
        private final String sample;

        private Set<String> leiSet;

        public LeiFinder(Set<String> leiSet, String sample) {
            this.leiSet = leiSet;
            this.sample = sample;
        }

        public <T> void processBasic(RosettaPath path, Class<? extends T> rosettaType, T instance, RosettaModelObject parent, AttributeMeta... metas) {
            if (instance == null) {
                return;
            }
            String maybeLEI = instance.toString();
            if (GleifLeiClient.LEI_PATTERN.matcher(maybeLEI).matches()) {
                LOGGER.info("Found LEI: {} in sample {}", leiSet, sample);

                leiSet.add(maybeLEI);
            }
        }

        @Override
        public <R extends RosettaModelObject> boolean processRosetta(RosettaPath rosettaPath, Class<? extends R> aClass, R r, RosettaModelObject rosettaModelObject, AttributeMeta... attributeMetas) {
            return true;
        }

        @Override
        public Report report() {
            return null;
        }
    }
}
