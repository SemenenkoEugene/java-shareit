package ru.practicum.shareit.item;

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

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class ItemJsonTest {

    @Autowired
    private JacksonTester<ItemDto> itemDtoJacksonTester;

    @Autowired
    private JacksonTester<CommentDto> commentDtoJacksonTester;

    @Test
    void itemDtoTest() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Скарификатор")
                .description("Для удаления старой газонной травы")
                .available(true)
                .nextBooking(ItemDto.ItemBooking.builder()
                        .id(10123L)
                        .bookerId(1005L)
                        .build())
                .lastBooking(null)
                .comments(new ArrayList<>())
                .requestId(20L)
                .build();

        JsonContent<ItemDto> jsonContent = itemDtoJacksonTester.write(itemDto);

        assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(jsonContent).extractingJsonPathStringValue("$.name").isEqualTo("Скарификатор");
        assertThat(jsonContent).extractingJsonPathStringValue("$.description").isEqualTo("Для удаления старой газонной травы");
        assertThat(jsonContent).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
        assertThat(jsonContent).extractingJsonPathNumberValue("$.nextBooking.id").isEqualTo(10123);
        assertThat(jsonContent).extractingJsonPathNumberValue("$.nextBooking.bookerId").isEqualTo(1005);
        assertThat(jsonContent).extractingJsonPathStringValue("$.lastBooking").isNull();
        assertThat(jsonContent).extractingJsonPathArrayValue("$.comments").isEmpty();
        assertThat(jsonContent).extractingJsonPathNumberValue("$.requestId").isEqualTo(20);
    }

    @Test
    void commentDtoTest() throws Exception {
        LocalDateTime timestamp = LocalDateTime.of(2023, Month.AUGUST, 14, 12, 12, 12);

        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .text("Вещь хорошая")
                .authorName("Садовник Джо")
                .created(timestamp)
                .build();

        JsonContent<CommentDto> jsonContent = commentDtoJacksonTester.write(commentDto);

        assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(jsonContent).extractingJsonPathStringValue("$.text").isEqualTo("Вещь хорошая");
        assertThat(jsonContent).extractingJsonPathStringValue("$.authorName").isEqualTo("Садовник Джо");
        assertThat(jsonContent).extractingJsonPathStringValue("$.created").isEqualTo("2023-08-14T12:12:12");
    }

}
