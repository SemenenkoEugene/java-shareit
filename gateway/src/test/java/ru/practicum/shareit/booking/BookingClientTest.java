package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseActions;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.RequestBookingStatus;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BookingClientTest {
    private static final String SERVER_URL = "http://localhost:9090";
    private static final String BOOKINGS_URL = SERVER_URL + "/bookings";
    private static final long USER_ID = 42L;

    private MockRestServiceServer server;
    private BookingClient client;

    @BeforeEach
    void setUp() {
        final ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        final RestTemplateBuilder builder = new RestTemplateBuilder()
                .messageConverters(new MappingJackson2HttpMessageConverter(objectMapper));
        final ExposedBookingClient testClient = new ExposedBookingClient(SERVER_URL, builder);
        server = MockRestServiceServer.bindTo(testClient.restTemplate()).build();
        client = testClient;
    }

    @Test
    void createShouldSendBookingAsJson() {
        final BookingRequestDto request = BookingRequestDto.builder()
                .start(LocalDateTime.of(2030, 1, 2, 10, 15, 30))
                .end(LocalDateTime.of(2030, 1, 2, 11, 45, 30))
                .itemId(7L)
                .build();
        expectRequest("", HttpMethod.POST)
                .andExpect(content().json("""
                        \s{"start":"2030-01-02T10:15:30","end":"2030-01-02T11:45:30","itemId":7}
                        """))
                .andRespond(withSuccess("{\"id\":9}", MediaType.APPLICATION_JSON));

        final ResponseEntity<Object> response = client.create(USER_ID, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(java.util.Map.of("id", 9));
        server.verify();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void approveShouldSendApprovedQueryParameter(final boolean approved) {
        expectRequest("/9?approved=" + approved, HttpMethod.PATCH)
                .andExpect(content().string(""))
                .andRespond(withSuccess());

        final ResponseEntity<Object> response = client.approve(USER_ID, 9L, approved);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        server.verify();
    }

    @Test
    void getByIdShouldSendBookingId() {
        expectRequest("/9", HttpMethod.GET)
                .andRespond(withSuccess("{\"id\":9}", MediaType.APPLICATION_JSON));

        final ResponseEntity<Object> response = client.getById(USER_ID, 9L);

        assertThat(response.getBody()).isEqualTo(java.util.Map.of("id", 9));
        server.verify();
    }

    @Test
    void getAllByStateShouldPreservePaginationValues() {
        expectRequest("?state=CURRENT&from=1&size=2", HttpMethod.GET)
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        final ResponseEntity<Object> response = client.getAllByState(
                USER_ID, RequestBookingStatus.CURRENT, 1, 2);

        assertThat(response.getBody()).isEqualTo(java.util.List.of());
        server.verify();
    }

    @Test
    void getAllByStateForOwnerShouldPreservePaginationValues() {
        expectRequest("/owner?state=WAITING&from=1&size=2", HttpMethod.GET)
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        final ResponseEntity<Object> response = client.getAllByStateForOwner(
                USER_ID, RequestBookingStatus.WAITING, 1, 2);

        assertThat(response.getBody()).isEqualTo(java.util.List.of());
        server.verify();
    }

    private ResponseActions expectRequest(final String path, final HttpMethod httpMethod) {
        return server.expect(MockRestRequestMatchers.requestTo(BOOKINGS_URL + path))
                .andExpect(MockRestRequestMatchers.method(httpMethod))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(header(BookingController.X_SHARER_USER_ID, String.valueOf(USER_ID)));
    }

    private static class ExposedBookingClient extends BookingClient {
        private ExposedBookingClient(final String serverUrl, final RestTemplateBuilder builder) {
            super(serverUrl, builder);
        }

        private RestTemplate restTemplate() {
            return rest;
        }
    }
}
