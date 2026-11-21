package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.generated.item.dto.CommentResponse;
import ru.practicum.shareit.generated.item.dto.CreateCommentRequest;
import ru.practicum.shareit.generated.item.dto.CreateItemRequest;
import ru.practicum.shareit.generated.item.dto.ItemBookingResponse;
import ru.practicum.shareit.generated.item.dto.ItemResponse;
import ru.practicum.shareit.generated.item.dto.UpdateItemRequest;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

@JsonTest
class ItemJsonTest {

    @Autowired
    private JacksonTester<ItemResponse> itemResponseJacksonTester;

    @Autowired
    private JacksonTester<CommentResponse> commentResponseJacksonTester;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void itemResponseShouldPreserveJsonContract() throws Exception {
        final ItemResponse itemResponse = ItemResponse.builder()
                .id(1L)
                .name("Скарификатор")
                .description("Для удаления старой газонной травы")
                .available(true)
                .nextBooking(ItemBookingResponse.builder().id(10_123L).bookerId(1005L).build())
                .lastBooking(null)
                .comments(List.of())
                .requestId(20L)
                .build();

        final JsonContent<ItemResponse> jsonContent = itemResponseJacksonTester.write(itemResponse);

        Assertions.assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.name").isEqualTo("Скарификатор");
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.description")
                .isEqualTo("Для удаления старой газонной травы");
        Assertions.assertThat(jsonContent).extractingJsonPathBooleanValue("$.available").isTrue();
        Assertions.assertThat(jsonContent).extractingJsonPathNumberValue("$.nextBooking.id").isEqualTo(10_123);
        Assertions.assertThat(jsonContent).extractingJsonPathNumberValue("$.nextBooking.bookerId").isEqualTo(1005);
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.lastBooking").isNull();
        Assertions.assertThat(jsonContent).extractingJsonPathArrayValue("$.comments").isEmpty();
        Assertions.assertThat(jsonContent).extractingJsonPathNumberValue("$.requestId").isEqualTo(20);
    }

    @Test
    void commentResponseShouldSerializeLocalDateTimeWithoutOffset() throws Exception {
        final LocalDateTime timestamp = LocalDateTime.of(2023, Month.AUGUST, 14, 12, 12, 12);
        final CommentResponse commentResponse = CommentResponse.builder()
                .id(1L)
                .text("Вещь хорошая")
                .authorName("Садовник Джо")
                .created(timestamp)
                .build();

        final JsonContent<CommentResponse> jsonContent = commentResponseJacksonTester.write(commentResponse);

        Assertions.assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.text").isEqualTo("Вещь хорошая");
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.authorName").isEqualTo("Садовник Джо");
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.created")
                .isEqualTo("2023-08-14T12:12:12");
    }

    @Test
    void generatedRequestsShouldBindExistingJsonShapes() throws Exception {
        final CreateItemRequest create = objectMapper.readValue(
                "{\"name\":\"Дрель\",\"description\":\"Аккумуляторная\",\"available\":true,\"requestId\":7}",
                CreateItemRequest.class);
        final UpdateItemRequest emptyPatch = objectMapper.readValue("{}", UpdateItemRequest.class);
        final UpdateItemRequest nullPatch = objectMapper.readValue(
                "{\"name\":null,\"description\":null,\"available\":null}", UpdateItemRequest.class);
        final CreateCommentRequest comment = objectMapper.readValue(
                "{\"text\":\"Отличная вещь\",\"created\":\"2000-01-01T00:00:00\"}",
                CreateCommentRequest.class);

        Assertions.assertThat(create)
                .extracting(CreateItemRequest::name, CreateItemRequest::description,
                        CreateItemRequest::available, CreateItemRequest::requestId)
                .containsExactly("Дрель", "Аккумуляторная", true, 7L);
        Assertions.assertThat(emptyPatch)
                .extracting(UpdateItemRequest::name, UpdateItemRequest::description, UpdateItemRequest::available)
                .containsOnlyNulls();
        Assertions.assertThat(nullPatch)
                .extracting(UpdateItemRequest::name, UpdateItemRequest::description, UpdateItemRequest::available)
                .containsOnlyNulls();
        Assertions.assertThat(comment.text()).isEqualTo("Отличная вещь");
        Assertions.assertThat(comment.created()).isEqualTo("2000-01-01T00:00:00");
    }

    @Test
    void itemResponseShouldSerializeNullableFieldsAndEmptyComments() throws Exception {
        final JsonContent<ItemResponse> jsonContent = itemResponseJacksonTester.write(
                ItemResponse.builder().comments(List.of()).build());

        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.name").isNull();
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.nextBooking").isNull();
        Assertions.assertThat(jsonContent).extractingJsonPathArrayValue("$.comments").isEmpty();
    }
}
