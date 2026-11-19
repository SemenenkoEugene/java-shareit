package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.generated.booking.dto.BookingResponse;
import ru.practicum.shareit.generated.booking.dto.BookingResponseItem;
import ru.practicum.shareit.generated.booking.dto.BookingResponseUser;
import ru.practicum.shareit.generated.booking.dto.BookingStatus;

import java.time.LocalDateTime;
import java.time.Month;

@JsonTest
class BookingJsonTest {

    @Autowired
    private JacksonTester<BookingResponse> bookingResponseJacksonTester;

    @Autowired
    private ObjectMapper objectMapper;

    @SneakyThrows
    @ParameterizedTest
    @EnumSource(BookingStatus.class)
    void shouldSerializeAndDeserializeBookingResponse(final BookingStatus status) {
        final LocalDateTime dateTime = LocalDateTime.of(2023, Month.AUGUST, 14, 13, 13, 13);
        final LocalDateTime endTimestamp = dateTime.plusDays(1);

        final BookingResponse bookingResponse = BookingResponse.builder()
                .id(5L)
                .start(dateTime)
                .end(endTimestamp)
                .status(status)
                .booker(BookingResponseUser.builder()
                        .id(10L)
                        .build())
                .item(BookingResponseItem.builder()
                        .id(15L)
                        .name("Пылесос")
                        .build())
                .build();

        final JsonContent<BookingResponse> jsonContent = bookingResponseJacksonTester.write(bookingResponse);

        Assertions.assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(5);
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.start")
                .isEqualTo("2023-08-14T13:13:13");
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.end")
                .isEqualTo("2023-08-15T13:13:13");
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.status").isEqualTo(status.toString());
        Assertions.assertThat(jsonContent).extractingJsonPathNumberValue("$.booker.id").isEqualTo(10);
        Assertions.assertThat(jsonContent).extractingJsonPathNumberValue("$.item.id").isEqualTo(15);
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.item.name").isEqualTo("Пылесос");
        Assertions.assertThat(objectMapper.readValue(jsonContent.getJson(), BookingResponse.class))
                .isEqualTo(bookingResponse);
    }
}
