package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.generated.item.dto.CommentResponse;
import ru.practicum.shareit.generated.item.dto.CreateCommentRequest;
import ru.practicum.shareit.generated.item.dto.CreateItemRequest;
import ru.practicum.shareit.generated.item.dto.ItemBookingResponse;
import ru.practicum.shareit.generated.item.dto.ItemResponse;
import ru.practicum.shareit.generated.item.dto.UpdateItemRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GeneratedItemRecordsTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private static ValidatorFactory validatorFactory;
    private static Validator validator;

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
    void allGeneratedItemModelsShouldBeRecords() {
        assertThat(List.of(CreateItemRequest.class, UpdateItemRequest.class, CreateCommentRequest.class,
                ItemResponse.class, CommentResponse.class, ItemBookingResponse.class))
                .allMatch(Class::isRecord);
    }

    @Test
    void buildersAndToBuildersShouldWorkForAllModels() {
        assertThat(CreateItemRequest.builder().name("Drill").description("Cordless").available(true).build()
                .toBuilder().name("Saw").build().name()).isEqualTo("Saw");
        assertThat(UpdateItemRequest.builder().name("Drill").build()
                .toBuilder().available(false).build().available()).isFalse();
        assertThat(CreateCommentRequest.builder().text("Useful").build()
                .toBuilder().text("Updated").build().text()).isEqualTo("Updated");
        assertThat(ItemResponse.builder().id(1L).build().toBuilder().id(2L).build().id()).isEqualTo(2L);
        assertThat(CommentResponse.builder().created(LocalDateTime.parse("2026-11-19T12:00:00")).build()
                .toBuilder().text("Good").build().text()).isEqualTo("Good");
        assertThat(ItemBookingResponse.builder().id(1L).bookerId(2L).build()
                .toBuilder().bookerId(3L).build().bookerId()).isEqualTo(3L);
    }

    @Test
    void jacksonShouldSerializeAndDeserializeAllModels() throws Exception {
        assertThat(roundTrip(CreateItemRequest.builder()
                .name("Drill").description("Cordless").available(true).build(), CreateItemRequest.class))
                .isEqualTo(CreateItemRequest.builder()
                        .name("Drill").description("Cordless").available(true).build());
        assertThat(roundTrip(UpdateItemRequest.builder().name("Drill").build(), UpdateItemRequest.class))
                .isEqualTo(UpdateItemRequest.builder().name("Drill").build());
        assertThat(roundTrip(CreateCommentRequest.builder().text("Useful").build(), CreateCommentRequest.class))
                .isEqualTo(CreateCommentRequest.builder().text("Useful").build());

        final ItemBookingResponse booking = ItemBookingResponse.builder().id(2L).bookerId(3L).build();
        final CommentResponse comment = CommentResponse.builder()
                .id(4L).text("Useful").authorName("Anna")
                .created(LocalDateTime.parse("2026-11-19T12:00:00")).build();
        final ItemResponse item = ItemResponse.builder()
                .id(1L).name("Drill").description("Cordless").available(true)
                .nextBooking(booking).comments(List.of(comment)).requestId(5L).build();

        final ItemResponse restored = roundTrip(item, ItemResponse.class);
        assertThat(restored).isEqualTo(item);
        assertThat(restored.comments()).singleElement().extracting(CommentResponse::created)
                .isEqualTo(LocalDateTime.parse("2026-11-19T12:00:00"));
        assertThat(CommentResponse.class.getRecordComponents()[3].getType()).isEqualTo(LocalDateTime.class);
        assertThat(OBJECT_MAPPER.writeValueAsString(comment))
                .contains("\"created\":\"2026-11-19T12:00:00\"");
    }

    @Test
    void requestValidationShouldPreserveCreateUpdateAndCommentRules() {
        assertThat(validator.validate(CreateItemRequest.builder().build())).isNotEmpty();
        assertThat(validator.validate(CreateItemRequest.builder()
                .name(" ").description("Cordless").available(true).build())).isNotEmpty();
        assertThat(validator.validate(CreateItemRequest.builder()
                .name("Drill").description(" ").available(true).build())).isNotEmpty();
        assertThat(validator.validate(CreateItemRequest.builder()
                .name("Drill").description("Cordless").available(true).build())).isEmpty();

        assertThat(validator.validate(UpdateItemRequest.builder().build())).isEmpty();
        assertThat(validator.validate(UpdateItemRequest.builder().name(" ").build())).isNotEmpty();
        assertThat(validator.validate(UpdateItemRequest.builder().description("").build())).isNotEmpty();
        assertThat(validator.validate(UpdateItemRequest.builder().name("Drill").build())).isEmpty();

        assertThat(validator.validate(CreateCommentRequest.builder().build())).isNotEmpty();
        assertThat(validator.validate(CreateCommentRequest.builder().text(" ").build())).isNotEmpty();
        assertThat(validator.validate(CreateCommentRequest.builder().text("Useful").build())).isEmpty();
    }

    private static <T> T roundTrip(final T value, final Class<T> type) throws Exception {
        return OBJECT_MAPPER.readValue(OBJECT_MAPPER.writeValueAsBytes(value), type);
    }
}
