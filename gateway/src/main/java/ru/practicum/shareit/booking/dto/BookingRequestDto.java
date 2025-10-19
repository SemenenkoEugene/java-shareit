package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingRequestDto {
    @NotNull(message = "Время старта не может быть пустым")
    @Future(message = "Время старта должно быть в будущем")
    private LocalDateTime start;

    @NotNull(message = "Время окончания не может быть пустым")
    @Future(message = "Время окончания должно быть в будущем")
    private LocalDateTime end;
    @NotNull(message = "ID вещи не может быть пустым")
    private Long itemId;

    @AssertTrue(message = "Время окончания должно быть после старта")
    private boolean isEndAfterStart() {
        return start == null || end == null || end.isAfter(start);
    }
}
