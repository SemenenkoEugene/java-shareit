package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.RequestBookingStatus;

import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {
    private static final long USER_ID = 2L;
    private static final long BOOKING_ID = 3L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingClient bookingClient;

    @Test
    void createShouldForwardValidRequestAndReturnClientResponse() throws Exception {
        Mockito.when(bookingClient.create(Mockito.eq(USER_ID), Mockito.any(BookingRequestDto.class)))
                .thenReturn(ResponseEntity.status(201).body(Map.of("id", BOOKING_ID, "status", "WAITING")));

        mockMvc.perform(MockMvcRequestBuilders.post("/bookings")
                        .header(BookingController.X_SHARER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                \s{"itemId":1,"start":"2099-01-01T10:00:00","end":"2099-01-01T11:00:00"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(BOOKING_ID))
                .andExpect(jsonPath("$.status").value("WAITING"));

        Mockito.verify(bookingClient).create(Mockito.eq(USER_ID), Mockito.argThat(request ->
                request.getItemId().equals(1L)
                        && request.getStart().equals(LocalDateTime.parse("2099-01-01T10:00:00"))
                        && request.getEnd().equals(LocalDateTime.parse("2099-01-01T11:00:00"))));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"start\":\"2099-01-01T10:00:00\",\"end\":\"2099-01-01T11:00:00\"}",
            "{\"itemId\":1,\"end\":\"2099-01-01T11:00:00\"}",
            "{\"itemId\":1,\"start\":\"2099-01-01T10:00:00\"}",
            "{\"itemId\":1,\"start\":\"2000-01-01T10:00:00\",\"end\":\"2099-01-01T11:00:00\"}",
            "{\"itemId\":1,\"start\":\"2099-01-01T10:00:00\",\"end\":\"2000-01-01T11:00:00\"}"
    })
    void createShouldRejectMissingFieldsAndNonFutureDates(final String body) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/bookings")
                        .header(BookingController.X_SHARER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(bookingClient);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void updateShouldForwardApprovalAndReturnClientResponse(final boolean approved) throws Exception {
        Mockito.when(bookingClient.approve(USER_ID, BOOKING_ID, approved))
                .thenReturn(ResponseEntity.ok(Map.of("id", BOOKING_ID, "approved", approved)));

        mockMvc.perform(patch("/bookings/{id}", BOOKING_ID)
                        .header(BookingController.X_SHARER_USER_ID, USER_ID)
                        .param("approved", Boolean.toString(approved)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(BOOKING_ID))
                .andExpect(jsonPath("$.approved").value(approved));

        Mockito.verify(bookingClient).approve(USER_ID, BOOKING_ID, approved);
    }

    @Test
    void updateShouldRejectMissingApproved() throws Exception {
        mockMvc.perform(patch("/bookings/{id}", BOOKING_ID)
                        .header(BookingController.X_SHARER_USER_ID, USER_ID))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(bookingClient);
    }

    @Test
    void getByIdShouldForwardIdsAndReturnClientResponse() throws Exception {
        Mockito.when(bookingClient.getById(USER_ID, BOOKING_ID))
                .thenReturn(ResponseEntity.ok(Map.of("id", BOOKING_ID)));

        mockMvc.perform(get("/bookings/{id}", BOOKING_ID)
                        .header(BookingController.X_SHARER_USER_ID, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(BOOKING_ID));

        Mockito.verify(bookingClient).getById(USER_ID, BOOKING_ID);
    }

    @Test
    void getAllShouldPassStateAndOffsetWithoutConversion() throws Exception {
        Mockito.when(bookingClient.getAllByState(USER_ID, RequestBookingStatus.FUTURE, 1, 2))
                .thenReturn(ResponseEntity.ok(Map.of("result", "booker")));

        mockMvc.perform(get("/bookings")
                        .header(BookingController.X_SHARER_USER_ID, USER_ID)
                        .param("state", "future")
                        .param("from", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("booker"));

        Mockito.verify(bookingClient).getAllByState(USER_ID, RequestBookingStatus.FUTURE, 1, 2);
    }

    @Test
    void getOwnerBookingsShouldPassStateAndOffsetWithoutConversion() throws Exception {
        Mockito.when(bookingClient.getAllByStateForOwner(USER_ID, RequestBookingStatus.PAST, 1, 2))
                .thenReturn(ResponseEntity.ok(Map.of("result", "owner")));

        mockMvc.perform(get("/bookings/owner")
                        .header(BookingController.X_SHARER_USER_ID, USER_ID)
                        .param("state", "PAST")
                        .param("from", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("owner"));

        Mockito.verify(bookingClient).getAllByStateForOwner(USER_ID, RequestBookingStatus.PAST, 1, 2);
    }

    @Test
    void getAllShouldUseExistingDefaults() throws Exception {
        Mockito.when(bookingClient.getAllByState(USER_ID, RequestBookingStatus.ALL, 0, 20))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings")
                        .header(BookingController.X_SHARER_USER_ID, USER_ID))
                .andExpect(status().isOk());

        Mockito.verify(bookingClient).getAllByState(USER_ID, RequestBookingStatus.ALL, 0, 20);
    }

    @Test
    void getOwnerBookingsShouldUseExistingDefaults() throws Exception {
        Mockito.when(bookingClient.getAllByStateForOwner(USER_ID, RequestBookingStatus.ALL, 0, 20))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings/owner")
                        .header(BookingController.X_SHARER_USER_ID, USER_ID))
                .andExpect(status().isOk());

        Mockito.verify(bookingClient).getAllByStateForOwner(USER_ID, RequestBookingStatus.ALL, 0, 20);
    }

    @ParameterizedTest
    @CsvSource({
            "/bookings,-1,20",
            "/bookings,0,0",
            "/bookings/owner,-1,20",
            "/bookings/owner,0,0"
    })
    void getBookingsShouldRejectInvalidPagination(final String path, final int from, final int size)
            throws Exception {
        try {
            mockMvc.perform(get(path)
                            .header(BookingController.X_SHARER_USER_ID, USER_ID)
                            .param("from", Integer.toString(from))
                            .param("size", Integer.toString(size)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").isNotEmpty());
        } finally {
            Mockito.verifyNoInteractions(bookingClient);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"/bookings", "/bookings/owner"})
    void getBookingsShouldRejectUnknownState(final String path) throws Exception {
        mockMvc.perform(get(path)
                        .header(BookingController.X_SHARER_USER_ID, USER_ID)
                        .param("state", "UNSUPPORTED_STATUS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Unknown state: UNSUPPORTED_STATUS"));

        Mockito.verifyNoInteractions(bookingClient);
    }
}
