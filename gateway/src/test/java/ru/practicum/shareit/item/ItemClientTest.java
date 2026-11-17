package ru.practicum.shareit.item;

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
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ItemClientTest {
    private static final String SERVER_URL = "http://localhost:9090";
    private static final String ITEMS_URL = SERVER_URL + "/items";
    private static final long USER_ID = 42L;

    private MockRestServiceServer server;
    private ItemClient client;

    @BeforeEach
    void setUp() {
        final ExposedItemClient testClient = new ExposedItemClient(SERVER_URL, new RestTemplateBuilder());
        server = MockRestServiceServer.bindTo(testClient.restTemplate()).build();
        client = testClient;
    }

    @Test
    void createShouldSendItemAsJson() {
        final ItemDto item = ItemDto.builder()
                .name("Drill")
                .description("Cordless drill")
                .available(true)
                .build();
        expectUserRequest("", HttpMethod.POST)
                .andExpect(content().json("""
                        \s{"name":"Drill","description":"Cordless drill","available":true}
                        """))
                .andRespond(withSuccess());

        client.create(USER_ID, item);

        server.verify();
    }

    @Test
    void updateShouldSendItemIdAndJsonBody() {
        final ItemDto item = ItemDto.builder().name("Updated drill").build();
        expectUserRequest("/7", HttpMethod.PATCH)
                .andExpect(content().json("{\"name\":\"Updated drill\"}"))
                .andRespond(withSuccess());

        client.update(USER_ID, 7L, item);

        server.verify();
    }

    @Test
    void getByIdShouldSendItemId() {
        expectUserRequest("/7", HttpMethod.GET).andRespond(withSuccess());

        client.getById(USER_ID, 7L);

        server.verify();
    }

    @Test
    void getAllByOwnerIdShouldPreservePaginationValues() {
        expectUserRequest("?from=1&size=2", HttpMethod.GET).andRespond(withSuccess());

        client.getAllByOwnerId(USER_ID, 1, 2);

        server.verify();
    }

    @Test
    void getAllBySearchTextShouldEncodeTextAndPreservePaginationValues() {
        expectUserRequest("/search?text=cordless%20drill%20%26%20saw&from=1&size=2", HttpMethod.GET)
                .andRespond(withSuccess());

        client.getAllBySearchText(USER_ID, "cordless drill & saw", 1, 2);

        server.verify();
    }

    @Test
    void createCommentShouldSendItemIdAndJsonBody() {
        final CommentDto comment = CommentDto.builder().text("Works well").build();
        expectUserRequest("/7/comment", HttpMethod.POST)
                .andExpect(content().json("{\"text\":\"Works well\"}"))
                .andRespond(withSuccess());

        client.createComment(USER_ID, 7L, comment);

        server.verify();
    }

    @Test
    void deleteShouldSendItemIdWithoutUserHeader() {
        expectRequest("/7", HttpMethod.DELETE)
                .andExpect(headerDoesNotExist(ItemController.X_SHARER_USER_ID))
                .andRespond(withSuccess());

        client.delete(7L);

        server.verify();
    }

    private ResponseActions expectUserRequest(final String path, final HttpMethod httpMethod) {
        return expectRequest(path, httpMethod)
                .andExpect(header(ItemController.X_SHARER_USER_ID, String.valueOf(USER_ID)));
    }

    private ResponseActions expectRequest(final String path, final HttpMethod httpMethod) {
        return server.expect(MockRestRequestMatchers.requestTo(ITEMS_URL + path))
                .andExpect(MockRestRequestMatchers.method(httpMethod))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
    }

    private static class ExposedItemClient extends ItemClient {
        private ExposedItemClient(final String serverUrl, final RestTemplateBuilder builder) {
            super(serverUrl, builder);
        }

        private RestTemplate restTemplate() {
            return rest;
        }
    }
}
