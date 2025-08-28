package com.regnosys.drr.utils.gleif;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import drr.enrichment.lei.LeiCategoryEnum;
import drr.enrichment.lei.LeiData;
import drr.enrichment.lei.LeiRegistrationStatusEnum;
import drr.enrichment.lei.LeiStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.Optional;

public class GleifLeiAdaptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(GleifLeiAdaptor.class);

    public LeiData adapt(String responseJson) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode node = objectMapper.readTree(responseJson);

            return LeiData.builder()
                    .setLei(extractField(node, "/data/attributes/lei").orElse(null))
                    .setEntityName(extractField(node, "/data/attributes/entity/legalName/name").orElse(null))
                    .setEntityCategory(extractField(node, "/data/attributes/entity/category").map(GleifLeiAdaptor::toLeiCategoryEnum).orElse(null))
                    .setEntityStatus(extractField(node, "/data/attributes/entity/status").map(GleifLeiAdaptor::toLeiStatusEnum).orElse(LeiStatusEnum.NULL))
                    .setBranchEntityStatus(extractField(node, "/data/attributes/branches/entityStatus").map(GleifLeiAdaptor::toLeiStatusEnum).orElse(LeiStatusEnum.NULL))
                    .setRegistrationStatus(extractField(node, "/data/attributes/registration/status").map(GleifLeiAdaptor::toLeiRegistrationStatusEnum).orElse(null))
                    .setRegistrationDate(extractField(node, "/data/attributes/registration/initialRegistrationDate").map(GleifLeiAdaptor::parseZonedDateTime).orElse(null))
                    .setPublished(isPublished(node))
                    .build();
        } catch (JsonProcessingException e) {
            LOGGER.error("Error occurred parsing JSON response: {}", responseJson, e);
            return null;
        }
    }

    private static Optional<String> extractField(JsonNode node, String path) {
        return Optional.ofNullable(node.at(path))
                .map(JsonNode::asText)
                .filter(str -> !str.isEmpty());
    }

    private static LeiCategoryEnum toLeiCategoryEnum(String entityCategory) {
        try {
            return LeiCategoryEnum.valueOf(entityCategory);
        } catch (Exception e) {
            LOGGER.warn("Unknown LEI entity category received from GLEIF {}", entityCategory);
            return null;
        }
    }

    private static LeiStatusEnum toLeiStatusEnum(String leiStatus) {
        try {
            return LeiStatusEnum.valueOf(leiStatus);
        } catch (Exception e) {
            LOGGER.warn("Unknown LEI status received from GLEIF {}", leiStatus);
            return null;
        }
    }

    private static LeiRegistrationStatusEnum toLeiRegistrationStatusEnum(String registrationStatus) {
        try {
            return LeiRegistrationStatusEnum.valueOf(registrationStatus);
        } catch (Exception e) {
            LOGGER.warn("Unknown LEI registration status received from GLEIF {}", registrationStatus);
            return null;
        }
    }

    private static ZonedDateTime parseZonedDateTime(String zonedDateTime) {
        return ZonedDateTime.parse(zonedDateTime);
    }

    private static boolean isPublished(JsonNode node) {
        return extractField(node, "/data/type").map("lei-records"::equals).orElse(false)
                && extractField(node, "/data/id").isPresent()
                && extractField(node, "/data/links/self").isPresent();
    }

}
