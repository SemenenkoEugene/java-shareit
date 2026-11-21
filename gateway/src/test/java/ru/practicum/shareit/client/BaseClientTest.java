package ru.practicum.shareit.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;

class BaseClientTest {
    private static final String SERVER_URL = "http://localhost:9090";
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private static final long USER_ID = 42L;

    private MockRestServiceServer server;
    private ExposedClient client;

    @BeforeEach
    void setUp() {
        final RestTemplate restTemplate = new RestTemplateBuilder()
                .rootUri(SERVER_URL)
                .build();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        client = new ExposedClient(restTemplate);
    }

    @Test
    void getShouldSendHeadersAndQueryParametersAndReturnSuccessfulResponse() {
        server.expect(requestTo(SERVER_URL + "/items/search?text=drill&from=0&size=20"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(header(USER_ID_HEADER, String.valueOf(USER_ID)))
                .andRespond(MockRestResponseCreators.withSuccess(
                        "{\"id\":7,\"name\":\"Drill\"}", MediaType.APPLICATION_JSON));

        final ResponseEntity<Object> response = client.get(
                "/items/search?text={text}&from={from}&size={size}",
                USER_ID,
                Map.of("text", "drill", "from", 0, "size", 20));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(Map.of("id", 7, "name", "Drill"));
        server.verify();
    }

    @Test
    void postShouldSendJsonBodyAndReturnCreatedResponse() {
        server.expect(requestTo(SERVER_URL + "/items"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(header(USER_ID_HEADER, String.valueOf(USER_ID)))
                .andExpect(MockRestRequestMatchers.content().json("{\"name\":\"Drill\",\"available\":true}"))
                .andRespond(MockRestResponseCreators.withCreatedEntity(URI.create(SERVER_URL + "/items/7")));

        final ResponseEntity<Object> response = client.post(
                "/items", USER_ID, Map.of("name", "Drill", "available", true));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).hasToString(SERVER_URL + "/items/7");
        assertThat(response.getBody()).isNull();
        server.verify();
    }

    @Test
    void patchShouldPreserveBadRequestStatusAndBody() {
        final String errorBody = "{\"error\":\"invalid approval\"}";
        server.expect(requestTo(SERVER_URL + "/bookings/9?approved=false"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(header(USER_ID_HEADER, String.valueOf(USER_ID)))
                .andExpect(MockRestRequestMatchers.content().string(""))
                .andRespond(MockRestResponseCreators.withBadRequest()
                        .contentType(MediaType.APPLICATION_JSON).body(errorBody));

        final ResponseEntity<Object> response = client.patch(
                "/bookings/9?approved={approved}", USER_ID, Map.of("approved", false), null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(byte[].class);
        assertThat(new String((byte[]) response.getBody(), StandardCharsets.UTF_8)).isEqualTo(errorBody);
        server.verify();
    }

    @Test
    void deleteShouldPreserveServerErrorStatusAndBody() {
        final String errorBody = "{\"error\":\"server unavailable\"}";
        server.expect(requestTo(SERVER_URL + "/users/42"))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(header(USER_ID_HEADER, String.valueOf(USER_ID)))
                .andRespond(MockRestResponseCreators.withServerError()
                        .contentType(MediaType.APPLICATION_JSON).body(errorBody));

        final ResponseEntity<Object> response = client.delete("/users/42", USER_ID, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isInstanceOf(byte[].class);
        assertThat(new String((byte[]) response.getBody(), StandardCharsets.UTF_8)).isEqualTo(errorBody);
        server.verify();
    }

    private static class ExposedClient extends BaseClient {
        private ExposedClient(final RestTemplate restTemplate) {
            super(restTemplate);
        }
    }
}
