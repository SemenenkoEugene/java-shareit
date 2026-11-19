package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseActions;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.generated.request.dto.CreateItemRequestRequest;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ItemRequestClientTest {
    private static final String SERVER_URL = "http://localhost:9090";
    private static final String REQUESTS_URL = SERVER_URL + "/requests";
    private static final long USER_ID = 42L;

    private MockRestServiceServer server;
    private ItemRequestClient client;

    @BeforeEach
    void setUp() {
        final ExposedItemRequestClient testClient = new ExposedItemRequestClient(
                SERVER_URL, new RestTemplateBuilder());
        server = MockRestServiceServer.bindTo(testClient.restTemplate()).build();
        client = testClient;
    }

    @Test
    void createShouldSendRequestAsJson() {
        final CreateItemRequestRequest request = CreateItemRequestRequest.builder()
                .description("Need a cordless drill")
                .build();
        expectRequest("", HttpMethod.POST)
                .andExpect(content().json("{\"description\":\"Need a cordless drill\"}"))
                .andRespond(withSuccess());

        client.create(USER_ID, request);

        server.verify();
    }

    @Test
    void getAllByOwnerIdShouldPreservePaginationValues() {
        expectRequest("?from=1&size=2", HttpMethod.GET).andRespond(withSuccess());

        client.getAllByOwnerId(USER_ID, 1, 2);

        server.verify();
    }

    @Test
    void getAllShouldPreservePaginationValues() {
        expectRequest("/all?from=1&size=2", HttpMethod.GET).andRespond(withSuccess());

        client.getAll(USER_ID, 1, 2);

        server.verify();
    }

    @Test
    void getByIdShouldSendRequestId() {
        expectRequest("/7", HttpMethod.GET).andRespond(withSuccess());

        client.getById(USER_ID, 7L);

        server.verify();
    }

    private ResponseActions expectRequest(final String path, final HttpMethod httpMethod) {
        return server.expect(MockRestRequestMatchers.requestTo(REQUESTS_URL + path))
                .andExpect(method(httpMethod))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(header(ItemRequestController.X_SHARER_USER_ID, String.valueOf(USER_ID)));
    }

    private static class ExposedItemRequestClient extends ItemRequestClient {
        private ExposedItemRequestClient(final String serverUrl, final RestTemplateBuilder builder) {
            super(serverUrl, builder);
        }

        private RestTemplate restTemplate() {
            return rest;
        }
    }
}
