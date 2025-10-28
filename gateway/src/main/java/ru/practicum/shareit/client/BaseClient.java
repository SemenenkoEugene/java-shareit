package ru.practicum.shareit.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static ru.practicum.shareit.booking.BookingController.X_SHARER_USER_ID;

public class BaseClient {
    protected final RestTemplate rest;

    public BaseClient(final RestTemplate rest) {
        this.rest = rest;
    }

    private static ResponseEntity<Object> prepareGatewayResponse(final ResponseEntity<Object> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        final ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(response.getStatusCode());

        if (response.hasBody()) {
            return responseBuilder.body(response.getBody());
        }

        return responseBuilder.build();
    }

    protected ResponseEntity<Object> get(final String path) {
        return get(path, null, null);
    }

    protected ResponseEntity<Object> get(final String path, final Long userId) {
        return get(path, userId, null);
    }

    protected ResponseEntity<Object> get(final String path, final Long userId, @Nullable final Map<String, Object> parameters) {
        return makeAndSendRequest(HttpMethod.GET, path, userId, parameters, null);
    }

    protected <T> ResponseEntity<Object> post(final String path, final T body) {
        return post(path, null, null, body);
    }

    protected <T> ResponseEntity<Object> post(final String path, final Long userId, final T body) {
        return post(path, userId, null, body);
    }

    protected <T> ResponseEntity<Object> post(final String path, final Long userId, @Nullable final Map<String, Object> parameters, final T body) {
        return makeAndSendRequest(HttpMethod.POST, path, userId, parameters, body);
    }

    protected <T> ResponseEntity<Object> put(final String path, final Long userId, final T body) {
        return put(path, userId, null, body);
    }

    protected <T> ResponseEntity<Object> put(final String path, final Long userId, @Nullable final Map<String, Object> parameters, final T body) {
        return makeAndSendRequest(HttpMethod.PUT, path, userId, parameters, body);
    }

    protected <T> ResponseEntity<Object> patch(final String path, final T body) {
        return patch(path, null, null, body);
    }

    protected <T> ResponseEntity<Object> patch(final String path, final Long userId) {
        return patch(path, userId, null, null);
    }

    protected <T> ResponseEntity<Object> patch(final String path, final Long userId, final T body) {
        return patch(path, userId, null, body);
    }

    protected <T> ResponseEntity<Object> patch(final String path, final Long userId, @Nullable final Map<String, Object> parameters, final T body) {
        return makeAndSendRequest(HttpMethod.PATCH, path, userId, parameters, body);
    }

    protected ResponseEntity<Object> delete(final String path) {
        return delete(path, null, null);
    }

    protected ResponseEntity<Object> delete(final String path, final Long userId) {
        return delete(path, userId, null);
    }

    protected ResponseEntity<Object> delete(final String path, final Long userId, @Nullable final Map<String, Object> parameters) {
        return makeAndSendRequest(HttpMethod.DELETE, path, userId, parameters, null);
    }

    private <T> ResponseEntity<Object> makeAndSendRequest(final HttpMethod method, final String path, final Long userId, @Nullable final Map<String, Object> parameters, @Nullable final T body) {
        final HttpEntity<T> requestEntity = new HttpEntity<>(body, defaultHeaders(userId));

        final ResponseEntity<Object> shareitServerResponse;
        try {
            if (parameters != null) {
                shareitServerResponse = rest.exchange(path, method, requestEntity, Object.class, parameters);
            } else {
                shareitServerResponse = rest.exchange(path, method, requestEntity, Object.class);
            }
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }
        return prepareGatewayResponse(shareitServerResponse);
    }

    private MultiValueMap<String, String> defaultHeaders(final Long userId) {
        final MultiValueMap<String, String> headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        if (userId != null) {
            headers.set(X_SHARER_USER_ID, String.valueOf(userId));
        }
        return headers;
    }
}
