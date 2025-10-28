package ru.practicum.shareit.request;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.request.dto.ItemRequestCreateResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestGetResponseDto;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

@JsonTest
class ItemRequestJsonTest {

    @Autowired
    private JacksonTester<ItemRequestCreateResponseDto> itemRequestCreateResponseDtoJacksonTester;

    @Autowired
    private JacksonTester<ItemRequestGetResponseDto> itemRequestGetResponseDtoJacksonTester;

    @Test
    void itemRequestCreateResponseDtoTest() throws Exception {
        final LocalDateTime timestamp = LocalDateTime.of(2023, Month.AUGUST, 14, 11, 11, 11);

        final ItemRequestCreateResponseDto itemRequestCreateResponseDto = ItemRequestCreateResponseDto.builder()
                .id(5L)
                .description("Ищу гараж")
                .created(timestamp)
                .build();

        final JsonContent<ItemRequestCreateResponseDto> jsonContent = itemRequestCreateResponseDtoJacksonTester.write(itemRequestCreateResponseDto);

        Assertions.assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(5);
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.description").isEqualTo("Ищу гараж");
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.created").isEqualTo("2023-08-14T11:11:11");
    }

    @Test
    void itemRequestGetResponseDtoTest() throws Exception {
        final LocalDateTime timestamp = LocalDateTime.of(2023, Month.AUGUST, 14, 10, 10, 10);

        final ItemRequestGetResponseDto itemRequestGetResponseDto = ItemRequestGetResponseDto.builder()
                .id(5L)
                .description("Ищу гараж")
                .created(timestamp)
                .items(List.of(
                        ItemRequestGetResponseDto.RequestedItem.builder()
                                .id(10L)
                                .name("Гараж")
                                .description("Обновлю гараж")
                                .available(true)
                                .requestId(5L)
                                .build()))
                .build();

        final JsonContent<ItemRequestGetResponseDto> jsonContent = itemRequestGetResponseDtoJacksonTester.write(itemRequestGetResponseDto);

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
}
