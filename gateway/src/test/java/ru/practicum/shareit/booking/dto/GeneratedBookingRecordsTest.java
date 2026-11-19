package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.generated.booking.dto.BookingQueryState;
import ru.practicum.shareit.generated.booking.dto.BookingResponse;
import ru.practicum.shareit.generated.booking.dto.BookingResponseItem;
import ru.practicum.shareit.generated.booking.dto.BookingResponseUser;
import ru.practicum.shareit.generated.booking.dto.BookingStatus;
import ru.practicum.shareit.generated.booking.dto.CreateBookingRequest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class GeneratedBookingRecordsTest {
    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void generatedModelsShouldContainFourRecordsAndUnchangedEnums() {
        assertThat(List.of(CreateBookingRequest.class, BookingResponse.class,
                BookingResponseUser.class, BookingResponseItem.class)).allMatch(Class::isRecord);
        assertThat(BookingStatus.values()).extracting(Enum::name)
                .containsExactly("WAITING", "APPROVED", "REJECTED", "CANCELED");
        assertThat(BookingQueryState.values()).extracting(Enum::name)
                .containsExactly("ALL", "CURRENT", "PAST", "FUTURE", "WAITING", "REJECTED");
    }

    @Test
    void buildersAndToBuildersShouldWorkForAllRecords() {
        final LocalDateTime start = LocalDateTime.now().plusDays(1).withNano(0);
        assertThat(CreateBookingRequest.builder().start(start).end(start.plusHours(1)).itemId(1L).build()
                .toBuilder().itemId(2L).build().itemId()).isEqualTo(2L);
        assertThat(BookingResponseUser.builder().id(1L).build().toBuilder().id(2L).build().id()).isEqualTo(2L);
        assertThat(BookingResponseItem.builder().id(1L).name("Drill").build()
                .toBuilder().name("Saw").build().name()).isEqualTo("Saw");
        assertThat(BookingResponse.builder().id(1L).status(BookingStatus.WAITING).build()
                .toBuilder().status(BookingStatus.APPROVED).build().status()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void jacksonShouldRoundTripAllRecordsWithLocalDateTimeFormat() throws IOException {
        final LocalDateTime start = LocalDateTime.parse("2099-01-01T10:00:00");
        final CreateBookingRequest request = CreateBookingRequest.builder()
                .start(start).end(start.plusHours(1)).itemId(1L).build();
        final BookingResponse response = BookingResponse.builder()
                .id(2L).start(start).end(start.plusHours(1)).status(BookingStatus.WAITING)
                .booker(BookingResponseUser.builder().id(3L).build())
                .item(BookingResponseItem.builder().id(1L).name("Drill").build())
                .build();

        assertThat(roundTrip(request, CreateBookingRequest.class)).isEqualTo(request);
        assertThat(roundTrip(response, BookingResponse.class)).isEqualTo(response);
        assertThat(objectMapper.writeValueAsString(request)).contains("2099-01-01T10:00:00");
    }

    @Test
    void createBookingValidationShouldRejectNullAndPastDatesAndAcceptFutureDates() {
        final Set<ConstraintViolation<CreateBookingRequest>> nullViolations = validator.validate(
                CreateBookingRequest.builder().build());
        assertThat(nullViolations).hasSize(3)
                .allMatch(violation -> violation.getConstraintDescriptor().getAnnotation()
                        .annotationType().equals(NotNull.class));

        final Set<ConstraintViolation<CreateBookingRequest>> pastViolations = validator.validate(
                CreateBookingRequest.builder().start(LocalDateTime.now().minusDays(2))
                        .end(LocalDateTime.now().minusDays(1)).itemId(1L).build());
        assertThat(pastViolations).hasSize(2)
                .allMatch(violation -> violation.getConstraintDescriptor().getAnnotation()
                        .annotationType().equals(Future.class));

        assertThat(validator.validate(CreateBookingRequest.builder()
                .start(LocalDateTime.now().plusDays(1)).end(LocalDateTime.now().plusDays(2))
                .itemId(1L).build())).isEmpty();
    }

    private <T> T roundTrip(final T value, final Class<T> type) throws IOException {
        return objectMapper.readValue(objectMapper.writeValueAsBytes(value), type);
    }
}
