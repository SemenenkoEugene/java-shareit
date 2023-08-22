package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
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

    @AssertTrue(message = "Время окончания должно быть после старта")
    private boolean isEndAfterStart() {
        return start == null || end == null || end.isAfter(start);
    }

    @NotNull(message = "ID вещи не может быть пустым")
    private Long itemId;
}
