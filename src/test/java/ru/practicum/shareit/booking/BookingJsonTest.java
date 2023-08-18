package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.time.LocalDateTime;
import java.time.Month;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class BookingJsonTest {
    @Autowired
    private JacksonTester<BookingResponseDto> bookingResponseDtoJacksonTester;

    @Test
    void bookingResponseDtoTest() throws Exception {
        var dateTime = LocalDateTime.of(2023, Month.AUGUST, 14, 13, 13, 13);
        var startTimestamp = dateTime;
        LocalDateTime endTimestamp = dateTime.plusDays(1);

        BookingResponseDto bookingResponseDto = BookingResponseDto.builder()
                .id(5L)
                .start(startTimestamp)
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

        JsonContent<BookingResponseDto> jsonContent = bookingResponseDtoJacksonTester.write(bookingResponseDto);

        assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(5);
        assertThat(jsonContent).extractingJsonPathStringValue("$.start")
                .isEqualTo("2023-08-14T13:13:13");
        assertThat(jsonContent).extractingJsonPathStringValue("$.end")
                .isEqualTo("2023-08-15T13:13:13");
        assertThat(jsonContent).extractingJsonPathStringValue("$.status").isEqualTo(Status.APPROVED.toString());
        assertThat(jsonContent).extractingJsonPathNumberValue("$.booker.id").isEqualTo(10);
        assertThat(jsonContent).extractingJsonPathNumberValue("$.item.id").isEqualTo(15);
        assertThat(jsonContent).extractingJsonPathStringValue("$.item.name").isEqualTo("Пылесос");
    }
}
