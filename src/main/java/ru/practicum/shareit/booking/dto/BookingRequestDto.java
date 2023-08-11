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

    @NotNull
    @Future
    private LocalDateTime start;

    @NotNull
    @Future
    private LocalDateTime end;
    @NotNull
    private Long itemId;

    @AssertTrue(message = "Время окончания не может быть до старта")
    private boolean isEndAfterStart() {
        return start == null || end == null || end.isAfter(start);
    }

    @AssertTrue(message = "Время старта и окончания не должны быть равны")
    private boolean isStartEqualsEnd() {
        return start == null || !start.equals(end);
    }
}
