package com.regnosys.drr.utils.gleif;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import static java.time.temporal.ChronoUnit.SECONDS;

public class GleifLeiClient {
    public static final Pattern LEI_PATTERN = Pattern.compile("^[A-Z0-9]{18,18}[0-9]{2,2}$");
    private static final Logger LOGGER = LoggerFactory.getLogger(GleifLeiClient.class);

    private static final String DATA_SOURCE_URL = "https://api.gleif.org/api/v1/lei-records/";
    private final HttpClient httpClient;

    public GleifLeiClient() {
        this(HttpClient.newBuilder()
                .executor(Executors.newFixedThreadPool(3))
                .build());
    }

    public GleifLeiClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public boolean isValidLei(String lei) {
        return LEI_PATTERN.matcher(lei).matches();
    }

    public GleifLeiResult getLeiDataFromGleif(String lei) {
        LOGGER.info("Looking up LEI {} in GLEIF", lei);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(DATA_SOURCE_URL + lei))
                    .timeout(Duration.of(10, SECONDS))
                    .GET()
                    .build();

            CompletableFuture<HttpResponse<String>> httpResponse =
                    httpClient
                            .sendAsync(request, HttpResponse.BodyHandlers.ofString());

            LOGGER.debug("Waiting for response");
            HttpResponse<String> response = httpResponse.join();
            LOGGER.debug("Got response");
            int statusCode = response.statusCode();
            if (statusCode == HttpURLConnection.HTTP_NOT_FOUND) {
                LOGGER.error("Got not found from GLEIF: lei {}, status code {}", lei, statusCode);
                return new GleifLeiResult(null, Status.NOT_FOUND, null);
            }

            if (statusCode != HttpURLConnection.HTTP_OK) {
                LOGGER.error("Got error code from GLEIF: lei {}, status code {}", lei, statusCode);
                return new GleifLeiResult(null, Status.API_ERROR, null);
            }

            String body = response.body();
            return new GleifLeiResult(body, Status.OK, null);
        } catch (Exception e) {
            LOGGER.error("Exception occurred getting LEI record from GLEIF", e);
            return new GleifLeiResult(null, Status.EXCEPTION, e);
        }
    }

    public enum Status {
        OK, API_ERROR, NOT_FOUND, EXCEPTION
    }

    public static class GleifLeiResult {
        private final String data;
        private final Status status;
        private final Exception exception;

        public GleifLeiResult(String data, Status status, Exception exception) {
            this.data = data;
            this.status = status;
            this.exception = exception;
        }

        public Status getStatus() {
            return status;
        }

        public Optional<String> getData() {
            return Optional.ofNullable(data);
        }

        public Optional<Exception> getError() {
            return Optional.of(exception);
        }
    }
}
