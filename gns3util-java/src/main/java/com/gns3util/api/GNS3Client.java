package com.gns3util.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import okhttp3.Request.Builder;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * GNS3 API client for making HTTP requests to GNS3 servers
 */
public class GNS3Client {

    private final OkHttpClient httpClient;
    private final String baseUrl;
    private final String accessToken;
    private final boolean insecure;
    private final ObjectMapper objectMapper;

    public GNS3Client(String baseUrl, String accessToken, boolean insecure) {
        this.baseUrl = baseUrl;
        this.accessToken = accessToken;
        this.insecure = insecure;
        this.objectMapper = new ObjectMapper();

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS);

        if (insecure) {
            // Create a trust manager that trusts all certificates
            builder.hostnameVerifier((hostname, session) -> true);
        }

        this.httpClient = builder.build();
    }

    /**
     * Make a GET request to the specified endpoint
     */
    public <T> T get(String endpoint, Class<T> responseType) throws IOException {
        return get(endpoint, responseType, null);
    }

    /**
     * Make a GET request to the specified endpoint with query parameters
     */
    public <T> T get(String endpoint, Class<T> responseType, Map<String, String> queryParams) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl + endpoint).newBuilder();

        if (queryParams != null) {
            for (Map.Entry<String, String> param : queryParams.entrySet()) {
                urlBuilder.addQueryParameter(param.getKey(), param.getValue());
            }
        }

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        return executeRequest(request, responseType);
    }

    /**
     * Make a POST request to the specified endpoint
     */
    public <T> T post(String endpoint, Object requestBody, Class<T> responseType) throws IOException {
        String jsonBody = objectMapper.writeValueAsString(requestBody);

        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(baseUrl + endpoint)
                .post(body)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .build();

        return executeRequest(request, responseType);
    }

    /**
     * Make a PUT request to the specified endpoint
     */
    public <T> T put(String endpoint, Object requestBody, Class<T> responseType) throws IOException {
        String jsonBody = objectMapper.writeValueAsString(requestBody);

        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(baseUrl + endpoint)
                .put(body)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .build();

        return executeRequest(request, responseType);
    }

    /**
     * Make a DELETE request to the specified endpoint
     */
    public <T> T delete(String endpoint, Class<T> responseType) throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + endpoint)
                .delete()
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        return executeRequest(request, responseType);
    }

    /**
     * Execute HTTP request and parse response
     */
    private <T> T executeRequest(Request request, Class<T> responseType) throws IOException {
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP request failed: " + response.code() + " " + response.message());
            }

            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IOException("Empty response body");
            }

            String responseString = responseBody.string();

            if (responseType == String.class) {
                return (T) responseString;
            }

            return objectMapper.readValue(responseString, responseType);
        }
    }

    /**
     * Get the raw response as string (for debugging or raw JSON handling)
     */
    public String getRaw(String endpoint) throws IOException {
        return get(endpoint, String.class);
    }

    /**
     * Make a POST request and return raw response
     */
    public String postRaw(String endpoint, Object requestBody) throws IOException {
        return post(endpoint, requestBody, String.class);
    }
}
