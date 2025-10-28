package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.nio.charset.StandardCharsets;
import java.util.List;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";
    private static final Long USER_ID = 1L;
    private static final Long ITEM_ID = 10L;

    private static final CommentDto REQUEST_DTO = CommentDto.builder()
            .text("Комментарий")
            .build();

    private static final CommentDto COMMENT_DTO = CommentDto.builder()
            .id(100L)
            .build();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ItemService itemService;

    @Test
    @SneakyThrows
    void createTest() {

        final ItemDto requestDto = getRequestDto();
        final ItemDto responseDto = getItemResponseDto(ITEM_ID);

        Mockito.when(itemService.create(Mockito.any(), Mockito.anyLong())).thenReturn(responseDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/items")
                        .header(X_SHARER_USER_ID, USER_ID)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(responseDto.getId()));

        Mockito.verify(itemService).create(Mockito.any(ItemDto.class), Mockito.eq(USER_ID));
        Mockito.verifyNoMoreInteractions(itemService);
    }

    @Test
    @SneakyThrows
    void createCommentTest() {

        Mockito.when(itemService.createComment(Mockito.any(), Mockito.anyLong(), Mockito.anyLong())).thenReturn(COMMENT_DTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/items/" + ITEM_ID + "/comment")
                        .header(X_SHARER_USER_ID, USER_ID)
                        .content(objectMapper.writeValueAsString(REQUEST_DTO))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(COMMENT_DTO.getId()));

        Mockito.verify(itemService).createComment(Mockito.any(CommentDto.class), Mockito.eq(USER_ID), Mockito.eq(ITEM_ID));
        Mockito.verifyNoMoreInteractions(itemService);
    }

    @SneakyThrows
    @Test
    void getItemByIdTest() {

        final ItemDto responseDto = getItemResponseDto(ITEM_ID);

        Mockito.when(itemService.getItemById(Mockito.anyLong(), Mockito.anyLong())).thenReturn(responseDto);

        mockMvc.perform(MockMvcRequestBuilders.get("/items/" + responseDto.getId())
                        .header(X_SHARER_USER_ID, USER_ID))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(responseDto.getId()));

        Mockito.verify(itemService).getItemById(Mockito.eq(USER_ID), Mockito.eq(ITEM_ID));
        Mockito.verifyNoMoreInteractions(itemService);
    }

    @SneakyThrows
    @Test
    void getItemsByOwnerIdTest() {
        final ItemDto responseDto1 = getItemResponseDto(ITEM_ID);

        final List<ItemDto> responseDtoList = List.of(responseDto1);

        Mockito.when(itemService.getItemsByOwnerId(Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(responseDtoList);

        mockMvc.perform(MockMvcRequestBuilders.get("/items")
                        .header(X_SHARER_USER_ID, USER_ID))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(responseDto1.getId()));

        Mockito.verify(itemService).getItemsByOwnerId(Mockito.eq(USER_ID), Mockito.anyInt(), Mockito.anyInt());
        Mockito.verifyNoMoreInteractions(itemService);
    }

    @SneakyThrows
    @Test
    void getItemsBySearchQueryTest() {
        final ItemDto responseDto1 = getItemResponseDto(ITEM_ID);
        final List<ItemDto> responseDtoList = List.of(responseDto1);

        Mockito.when(itemService.getItemsBySearchQuery(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(responseDtoList);

        mockMvc.perform(MockMvcRequestBuilders.get("/items/search")
                        .param("text", "someText"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(responseDto1.getId()));

        Mockito.verify(itemService).getItemsBySearchQuery(Mockito.eq("someText"), Mockito.anyInt(), Mockito.anyInt());
        Mockito.verifyNoMoreInteractions(itemService);
    }

    @SneakyThrows
    @Test
    void updateItemTest() {
        final ItemDto requestDto = getRequestDto();
        final ItemDto responseDto = getItemResponseDto(ITEM_ID);

        Mockito.when(itemService.update(Mockito.any(), Mockito.anyLong(), Mockito.anyLong())).thenReturn(responseDto);

        mockMvc.perform(MockMvcRequestBuilders.patch("/items/" + ITEM_ID)
                        .header(X_SHARER_USER_ID, USER_ID)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(responseDto.getId()));

        Mockito.verify(itemService).update(Mockito.any(ItemDto.class), Mockito.eq(ITEM_ID), Mockito.eq(USER_ID));
        Mockito.verifyNoMoreInteractions(itemService);
    }

    @SneakyThrows
    @Test
    void deleteItemTest() {
        final Long itemId = 1L;

        mockMvc.perform(MockMvcRequestBuilders.delete("/items/" + itemId))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(itemService).delete(Mockito.eq(itemId));
        Mockito.verifyNoMoreInteractions(itemService);
    }

    private ItemDto getRequestDto() {
        return ItemDto.builder()
                .name("Парафиновая свеча")
                .description("В упаковке")
                .available(true)
                .build();
    }

    private ItemDto getItemResponseDto(final Long id) {
        return ItemDto.builder()
                .id(id)
                .build();
    }
}