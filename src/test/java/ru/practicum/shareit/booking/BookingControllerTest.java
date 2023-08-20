package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.RequestBookingStatus;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @Test
    void getByIdTest() throws Exception {
        Long userId = 1L;

        BookingResponseDto responseDto = getBookingResponseDto(10L);

        when(bookingService.getById(anyLong(), anyLong()))
                .thenReturn(responseDto);

        mockMvc.perform(get("/bookings/" + responseDto.getId())
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()));

        verify(bookingService, times(1)).getById(anyLong(), anyLong());
        verifyNoMoreInteractions(bookingService);
    }

    @Test
    void getAllByStateTest() throws Exception {
        Long userId = 1L;

        BookingResponseDto responseDto1 = getBookingResponseDto(10L);
        BookingResponseDto responseDto2 = getBookingResponseDto(11L);

        List<BookingResponseDto> responseDtoList = Arrays.asList(
                responseDto1,
                responseDto2
        );

        when(bookingService.getAllByState(any(), eq(userId), anyInt(), anyInt()))
                .thenReturn(responseDtoList);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(responseDto1.getId()))
                .andExpect(jsonPath("$[1].id").value(responseDto2.getId()));

        verify(bookingService, times(1)).getAllByState(eq(RequestBookingStatus.ALL), eq(userId), anyInt(), anyInt());
        verifyNoMoreInteractions(bookingService);
    }

    @Test
    void getAllByStateForOwnerTest() throws Exception {
        Long userId = 1L;

        BookingResponseDto responseDto1 = getBookingResponseDto(10L);
        BookingResponseDto responseDto2 = getBookingResponseDto(11L);

        List<BookingResponseDto> responseDtoList = Arrays.asList(
                responseDto1,
                responseDto2
        );

        when(bookingService.getAllByStateForOwner(any(), eq(userId), anyInt(), anyInt())).thenReturn(responseDtoList);

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(responseDto1.getId()))
                .andExpect(jsonPath("$[1].id").value(responseDto2.getId()));

        verify(bookingService, times(1)).getAllByStateForOwner(eq(RequestBookingStatus.ALL), eq(userId), anyInt(), anyInt());
        verifyNoMoreInteractions(bookingService);
    }

    @Test
    void createTest() throws Exception {
        Long userId = 1L;

        BookingRequestDto requestDto = BookingRequestDto.builder()
                .start(LocalDateTime.now().plusDays(10))
                .end(LocalDateTime.now().plusDays(20))
                .itemId(1L)
                .build();

        BookingResponseDto responseDto = getBookingResponseDto(10L);

        when(bookingService.create(any(BookingRequestDto.class), eq(userId))).thenReturn(responseDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()));

        verify(bookingService, times(1)).create(any(BookingRequestDto.class), eq(userId));
        verifyNoMoreInteractions(bookingService);
    }

    @Test
    void approveTest() throws Exception {
        Long userId = 1L;

        BookingResponseDto responseDto = getBookingResponseDto(10L);

        when(bookingService.approve(anyLong(), anyBoolean(), anyLong()))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/bookings/" + responseDto.getId())
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()));

        verify(bookingService, times(1)).approve(anyLong(), eq(false), anyLong());
        verifyNoMoreInteractions(bookingService);
    }

    private BookingResponseDto getBookingResponseDto(Long id) {
        return BookingResponseDto.builder()
                .id(id)
                .build();
    }
}