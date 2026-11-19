package ru.practicum.shareit.request;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.generated.request.dto.ItemRequestCreateResponse;
import ru.practicum.shareit.generated.request.dto.ItemRequestResponse;
import ru.practicum.shareit.generated.request.dto.RequestedItemResponse;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

@JsonTest
class ItemRequestJsonTest {

    @Autowired
    private JacksonTester<ItemRequestCreateResponse> itemRequestCreateResponseJacksonTester;

    @Autowired
    private JacksonTester<ItemRequestResponse> itemRequestResponseJacksonTester;

    @Test
    void itemRequestCreateResponseDtoTest() throws Exception {
        final LocalDateTime timestamp = LocalDateTime.of(2023, Month.AUGUST, 14, 11, 11, 11);

        final ItemRequestCreateResponse itemRequestCreateResponse = ItemRequestCreateResponse.builder()
                .id(5L)
                .description("Ищу гараж")
                .created(timestamp)
                .build();

        final JsonContent<ItemRequestCreateResponse> jsonContent =
                itemRequestCreateResponseJacksonTester.write(itemRequestCreateResponse);

        Assertions.assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(5);
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.description").isEqualTo("Ищу гараж");
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.created").isEqualTo("2023-08-14T11:11:11");
    }

    @Test
    void itemRequestGetResponseDtoTest() throws Exception {
        final LocalDateTime timestamp = LocalDateTime.of(2023, Month.AUGUST, 14, 10, 10, 10);

        final ItemRequestResponse itemRequestResponse = ItemRequestResponse.builder()
                .id(5L)
                .description("Ищу гараж")
                .created(timestamp)
                .items(List.of(
                        RequestedItemResponse.builder()
                                .id(10L)
                                .name("Гараж")
                                .description("Обновлю гараж")
                                .available(true)
                                .requestId(5L)
                                .build()))
                .build();

        final JsonContent<ItemRequestResponse> jsonContent =
                itemRequestResponseJacksonTester.write(itemRequestResponse);

        Assertions.assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(5);
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.description").isEqualTo("Ищу гараж");
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.created").isEqualTo("2023-08-14T10:10:10");
        Assertions.assertThat(jsonContent).extractingJsonPathArrayValue("$.items").hasSize(1);
        Assertions.assertThat(jsonContent).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(10);
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.items[0].name").isEqualTo("Гараж");
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.items[0].description").isEqualTo("Обновлю гараж");
        Assertions.assertThat(jsonContent).extractingJsonPathBooleanValue("$.items[0].available").isEqualTo(true);
        Assertions.assertThat(jsonContent).extractingJsonPathNumberValue("$.items[0].requestId").isEqualTo(5);
    }

    @Test
    void itemRequestResponseNullAndEmptyItemsTest() throws Exception {
        final JsonContent<ItemRequestResponse> nullItemsJson =
                itemRequestResponseJacksonTester.write(ItemRequestResponse.builder().build());
        final JsonContent<ItemRequestResponse> emptyItemsJson =
                itemRequestResponseJacksonTester.write(ItemRequestResponse.builder().items(List.of()).build());

        Assertions.assertThat(nullItemsJson).extractingJsonPathValue("$.id").isNull();
        Assertions.assertThat(nullItemsJson).extractingJsonPathValue("$.description").isNull();
        Assertions.assertThat(nullItemsJson).extractingJsonPathValue("$.created").isNull();
        Assertions.assertThat(nullItemsJson).extractingJsonPathValue("$.items").isNull();
        Assertions.assertThat(emptyItemsJson).extractingJsonPathArrayValue("$.items").isEmpty();
    }
}
