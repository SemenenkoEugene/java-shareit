package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.shareit.booking.dto.RequestBookingStatus;
import ru.practicum.shareit.generated.booking.dto.BookingResponse;
import ru.practicum.shareit.generated.booking.dto.CreateBookingRequest;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    private static final Long USER_ID = 1L;
    private static final BookingResponse RESPONSE_DTO_1 = BookingResponse.builder()
            .id(10L)
            .build();

    private static final BookingResponse RESPONSE_DTO_2 = BookingResponse.builder()
            .id(11L)
            .build();

    private static final List<BookingResponse> RESPONSE_DTO_LIST = List.of(RESPONSE_DTO_1, RESPONSE_DTO_2);
    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookingService bookingService;

    @SneakyThrows
    @Test
    void getByIdTest() {

        Mockito.when(bookingService.getById(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(RESPONSE_DTO_1);

        mockMvc.perform(MockMvcRequestBuilders.get("/bookings/" + RESPONSE_DTO_1.id())
                        .header(X_SHARER_USER_ID, USER_ID))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(RESPONSE_DTO_1.id()));

        Mockito.verify(bookingService, Mockito.times(1)).getById(Mockito.anyLong(), Mockito.eq(USER_ID));
        Mockito.verifyNoMoreInteractions(bookingService);
    }

    @SneakyThrows
    @Test
    void getAllByStateTest() {

        Mockito.when(bookingService.getAllByState(Mockito.any(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(RESPONSE_DTO_LIST);

        mockMvc.perform(MockMvcRequestBuilders.get("/bookings")
                        .header(X_SHARER_USER_ID, USER_ID))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(RESPONSE_DTO_1.id()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value(RESPONSE_DTO_2.id()));

        Mockito.verify(bookingService, Mockito.times(1)).getAllByState(Mockito.eq(RequestBookingStatus.ALL), Mockito.eq(USER_ID), Mockito.anyInt(), Mockito.anyInt());
        Mockito.verifyNoMoreInteractions(bookingService);
    }

    @SneakyThrows
    @Test
    void getAllByStateForOwnerTest() {

        Mockito.when(bookingService.getAllByStateForOwner(Mockito.any(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(RESPONSE_DTO_LIST);

        mockMvc.perform(MockMvcRequestBuilders.get("/bookings/owner")
                        .header(X_SHARER_USER_ID, USER_ID))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(RESPONSE_DTO_1.id()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value(RESPONSE_DTO_2.id()));

        Mockito.verify(bookingService, Mockito.times(1)).getAllByStateForOwner(Mockito.eq(RequestBookingStatus.ALL), Mockito.eq(USER_ID), Mockito.anyInt(), Mockito.anyInt());
        Mockito.verifyNoMoreInteractions(bookingService);
    }

    @SneakyThrows
    @Test
    void createTest() {

        final CreateBookingRequest requestDto = CreateBookingRequest.builder()
                .start(LocalDateTime.now().plusDays(10))
                .end(LocalDateTime.now().plusDays(20))
                .itemId(1L)
                .build();


        Mockito.when(bookingService.create(Mockito.any(), Mockito.anyLong())).thenReturn(RESPONSE_DTO_1);

        mockMvc.perform(MockMvcRequestBuilders.post("/bookings")
                        .header(X_SHARER_USER_ID, USER_ID)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(RESPONSE_DTO_1.id()));

        Mockito.verify(bookingService, Mockito.times(1)).create(Mockito.any(CreateBookingRequest.class), Mockito.eq(USER_ID));
        Mockito.verifyNoMoreInteractions(bookingService);
    }

    @SneakyThrows
    @Test
    void approveTest() {

        Mockito.when(bookingService.approve(Mockito.anyLong(), Mockito.anyBoolean(), Mockito.anyLong()))
                .thenReturn(RESPONSE_DTO_1);

        mockMvc.perform(MockMvcRequestBuilders.patch("/bookings/" + RESPONSE_DTO_1.id())
                        .header(X_SHARER_USER_ID, USER_ID)
                        .param("approved", "false"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(RESPONSE_DTO_1.id()));

        Mockito.verify(bookingService, Mockito.times(1)).approve(Mockito.anyLong(), Mockito.eq(false), Mockito.anyLong());
        Mockito.verifyNoMoreInteractions(bookingService);
    }

    @SneakyThrows
    @ParameterizedTest
    @CsvSource({"waiting, WAITING", "REJECTED, REJECTED"})
    void getAllShouldParseSupportedStateIgnoringCase(final String state,
                                                      final RequestBookingStatus expectedStatus) {
        Mockito.when(bookingService.getAllByState(Mockito.any(), Mockito.eq(USER_ID), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(List.of());

        mockMvc.perform(MockMvcRequestBuilders.get("/bookings")
                        .header(X_SHARER_USER_ID, USER_ID)
                        .param("state", state))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(bookingService).getAllByState(expectedStatus, USER_ID, 0, 20);
    }

    @SneakyThrows
    @ParameterizedTest
    @ValueSource(strings = {"APPROVED", "CANCELED"})
    void getAllShouldRejectUnsupportedQueryState(final String state) {
        mockMvc.perform(MockMvcRequestBuilders.get("/bookings")
                        .header(X_SHARER_USER_ID, USER_ID)
                        .param("state", state))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verifyNoInteractions(bookingService);
    }

}
