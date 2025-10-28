package ru.practicum.shareit.booking;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.time.LocalDateTime;
import java.time.Month;

@JsonTest
class BookingJsonTest {

    @Autowired
    private JacksonTester<BookingResponseDto> bookingResponseDtoJacksonTester;

    @SneakyThrows
    @Test
    void shouldSerializeBookingResponseDtoToExpectedJson() {
        final LocalDateTime dateTime = LocalDateTime.of(2023, Month.AUGUST, 14, 13, 13, 13);
        final LocalDateTime endTimestamp = dateTime.plusDays(1);

        final BookingResponseDto bookingResponseDto = BookingResponseDto.builder()
                .id(5L)
                .start(dateTime)
                .end(endTimestamp)
                .status(Status.APPROVED)
                .booker(BookingResponseDto.BookingResponseUserDto.builder()
                        .id(10L)
                        .build())
                .item(BookingResponseDto.BookingResponseItemDto.builder()
                        .id(15L)
                        .name("Пылесос")
                        .build())
                .build();

        final JsonContent<BookingResponseDto> jsonContent = bookingResponseDtoJacksonTester.write(bookingResponseDto);

        Assertions.assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(5);
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.start")
                .isEqualTo("2023-08-14T13:13:13");
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.end")
                .isEqualTo("2023-08-15T13:13:13");
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.status").isEqualTo(Status.APPROVED.toString());
        Assertions.assertThat(jsonContent).extractingJsonPathNumberValue("$.booker.id").isEqualTo(10);
        Assertions.assertThat(jsonContent).extractingJsonPathNumberValue("$.item.id").isEqualTo(15);
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.item.name").isEqualTo("Пылесос");
    }
}
