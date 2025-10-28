package ru.practicum.shareit.item;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;

@JsonTest
class ItemJsonTest {

    @Autowired
    private JacksonTester<ItemDto> itemDtoJacksonTester;

    @Autowired
    private JacksonTester<CommentDto> commentDtoJacksonTester;

    @SneakyThrows
    @Test
    void itemDtoTest()  {
        final ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Скарификатор")
                .description("Для удаления старой газонной травы")
                .available(true)
                .nextBooking(ItemDto.ItemBooking.builder()
                        .id(10_123L)
                        .bookerId(1005L)
                        .build())
                .lastBooking(null)
                .comments(new ArrayList<>())
                .requestId(20L)
                .build();

        final JsonContent<ItemDto> jsonContent = itemDtoJacksonTester.write(itemDto);

        Assertions.assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.name").isEqualTo("Скарификатор");
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.description").isEqualTo("Для удаления старой газонной травы");
        Assertions.assertThat(jsonContent).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
        Assertions.assertThat(jsonContent).extractingJsonPathNumberValue("$.nextBooking.id").isEqualTo(10_123);
        Assertions.assertThat(jsonContent).extractingJsonPathNumberValue("$.nextBooking.bookerId").isEqualTo(1005);
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.lastBooking").isNull();
        Assertions.assertThat(jsonContent).extractingJsonPathArrayValue("$.comments").isEmpty();
        Assertions.assertThat(jsonContent).extractingJsonPathNumberValue("$.requestId").isEqualTo(20);
    }

    @Test
    void commentDtoTest() throws Exception {
        final LocalDateTime timestamp = LocalDateTime.of(2023, Month.AUGUST, 14, 12, 12, 12);

        final CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .text("Вещь хорошая")
                .authorName("Садовник Джо")
                .created(timestamp)
                .build();

        final JsonContent<CommentDto> jsonContent = commentDtoJacksonTester.write(commentDto);

        Assertions.assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.text").isEqualTo("Вещь хорошая");
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.authorName").isEqualTo("Садовник Джо");
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.created").isEqualTo("2023-08-14T12:12:12");
    }

}
