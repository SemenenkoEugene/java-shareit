package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    void createTest() throws Exception {
        Long userId = 1L;

        ItemDto requestDto = getRequestDto();
        ItemDto responseDto = getItemResponseDto(10L);

        when(itemService.create(any(ItemDto.class), eq(userId)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()));

        verify(itemService, times(1)).create(any(ItemDto.class), eq(userId));
        verifyNoMoreInteractions(itemService);
    }

    @Test
    void createCommentTest() throws Exception {
        Long userId = 1L;
        Long itemId = 10L;

        CommentDto requestDto = CommentDto.builder()
                .text("Комментарий")
                .build();

        CommentDto responseDto = CommentDto.builder()
                .id(100L)
                .build();

        when(itemService.createComment(any(CommentDto.class), eq(userId), eq(itemId)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/items/" + itemId + "/comment")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()));

        verify(itemService, times(1)).createComment(any(CommentDto.class), eq(userId), eq(itemId));
        verifyNoMoreInteractions(itemService);
    }

    @Test
    void getItemByIdTest() throws Exception {
        Long userId = 1L;

        ItemDto responseDto = getItemResponseDto(10L);

        when(itemService.getItemById(anyLong(), anyLong()))
                .thenReturn(responseDto);

        mockMvc.perform(get("/items/" + responseDto.getId())
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()));

        verify(itemService, times(1)).getItemById(anyLong(), anyLong());
        verifyNoMoreInteractions(itemService);
    }

    @Test
    void getItemsByOwnerIdTest() throws Exception {
        Long userId = 1L;

        ItemDto responseDto1 = getItemResponseDto(10L);
        ItemDto responseDto2 = getItemResponseDto(11L);

        List<ItemDto> responseDtoList = Arrays.asList(
                responseDto1,
                responseDto2
        );

        when(itemService.getItemsByOwnerId(eq(userId), anyInt(), anyInt()))
                .thenReturn(responseDtoList);

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(responseDto1.getId()))
                .andExpect(jsonPath("$[1].id").value(responseDto2.getId()));

        verify(itemService, times(1)).getItemsByOwnerId(eq(userId), anyInt(), anyInt());
        verifyNoMoreInteractions(itemService);
    }

    @Test
    void getItemsBySearchQueryTest() throws Exception {
        ItemDto responseDto1 = getItemResponseDto(10L);
        ItemDto responseDto2 = getItemResponseDto(11L);

        List<ItemDto> responseDtoList = Arrays.asList(
                responseDto1,
                responseDto2
        );

        when(itemService.getItemsBySearchQuery(anyString(), anyInt(), anyInt()))
                .thenReturn(responseDtoList);

        mockMvc.perform(get("/items/search")
                        .param("text", "someText"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(responseDto1.getId()))
                .andExpect(jsonPath("$[1].id").value(responseDto2.getId()));

        verify(itemService, times(1)).getItemsBySearchQuery(eq("someText"), anyInt(), anyInt());
        verifyNoMoreInteractions(itemService);
    }

    @Test
    void updateItemTest() throws Exception {
        Long userId = 1L;
        Long itemId = 10L;

        ItemDto requestDto = getRequestDto();
        ItemDto responseDto = getItemResponseDto(10L);

        when(itemService.update(any(ItemDto.class), eq(itemId), eq(userId)))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/items/" + itemId)
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()));

        verify(itemService, times(1)).update(any(ItemDto.class), eq(itemId), eq(userId));
        verifyNoMoreInteractions(itemService);
    }

    @Test
    void deleteItemTest() throws Exception {
        Long itemId = 1L;

        mockMvc.perform(delete("/items/" + itemId))
                .andExpect(status().isOk());

        verify(itemService, times(1)).delete(itemId);
        verifyNoMoreInteractions(itemService);
    }

    private ItemDto getRequestDto() {
        return ItemDto.builder()
                .name("Парафиновая свеча")
                .description("В упаковке")
                .available(true)
                .build();
    }

    private ItemDto getItemResponseDto(Long id) {
        return ItemDto.builder()
                .id(id)
                .build();
    }
}