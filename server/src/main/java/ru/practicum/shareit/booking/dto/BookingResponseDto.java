package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.Status;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingResponseDto {

    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private Status status;
    private BookingResponseUserDto booker;
    private BookingResponseItemDto item;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingResponseUserDto {
        private Long id;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingResponseItemDto {
        private Long id;
        private String name;
    }
}
