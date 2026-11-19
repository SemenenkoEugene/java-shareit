package ru.practicum.shareit.user;

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
import ru.practicum.shareit.generated.user.dto.CreateUserRequest;
import ru.practicum.shareit.generated.user.dto.UpdateUserRequest;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class UserClientTest {
    private static final String SERVER_URL = "http://localhost:9090";
    private static final String USERS_URL = SERVER_URL + "/users";
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private static final long USER_ID = 42L;

    private MockRestServiceServer server;
    private UserClient client;

    @BeforeEach
    void setUp() {
        final ExposedUserClient testClient = new ExposedUserClient(SERVER_URL, new RestTemplateBuilder());
        server = MockRestServiceServer.bindTo(testClient.restTemplate()).build();
        client = testClient;
    }

    @Test
    void createShouldSendUserAsJsonWithoutUserHeader() {
        final CreateUserRequest user = CreateUserRequest.builder()
                .name("Anna")
                .email("anna@example.com")
                .build();
        expectRequest("", HttpMethod.POST)
                .andExpect(headerDoesNotExist(USER_ID_HEADER))
                .andExpect(content().json("""
                        \s{"name":"Anna","email":"anna@example.com"}
                        """))
                .andRespond(withSuccess());

        client.create(user);

        server.verify();
    }

    @Test
    void updateShouldPreserveDtoValuesAndSendUserHeader() {
        final UpdateUserRequest user = UpdateUserRequest.builder()
                .name("Updated name")
                .email("updated@example.com")
                .build();
        expectRequest("/42", HttpMethod.PATCH)
                .andExpect(header(USER_ID_HEADER, String.valueOf(USER_ID)))
                .andExpect(content().json("""
                        \s{"name":"Updated name","email":"updated@example.com"}
                        """))
                .andRespond(withSuccess());

        client.update(USER_ID, user);

        server.verify();
    }

    @Test
    void getByIdShouldSendUserIdWithoutUserHeader() {
        expectRequest("/42", HttpMethod.GET)
                .andExpect(headerDoesNotExist(USER_ID_HEADER))
                .andRespond(withSuccess());

        client.getById(USER_ID);

        server.verify();
    }

    @Test
    void getAllShouldSendUsersUriWithoutUserHeader() {
        expectRequest("", HttpMethod.GET)
                .andExpect(headerDoesNotExist(USER_ID_HEADER))
                .andRespond(withSuccess());

        client.getAll();

        server.verify();
    }

    @Test
    void deleteShouldSendUserIdWithoutUserHeader() {
        expectRequest("/42", HttpMethod.DELETE)
                .andExpect(headerDoesNotExist(USER_ID_HEADER))
                .andRespond(withSuccess());

        client.delete(USER_ID);

        server.verify();
    }

    private ResponseActions expectRequest(final String path, final HttpMethod httpMethod) {
        return server.expect(MockRestRequestMatchers.requestTo(USERS_URL + path))
                .andExpect(MockRestRequestMatchers.method(httpMethod))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
    }

    private static class ExposedUserClient extends UserClient {
        private ExposedUserClient(final String serverUrl, final RestTemplateBuilder builder) {
            super(serverUrl, builder);
        }

        private RestTemplate restTemplate() {
            return rest;
        }
    }

}
