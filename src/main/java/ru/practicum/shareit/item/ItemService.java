package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto create(ItemDto itemDto, Long userId);

    CommentDto createComment(CommentDto commentDto, Long userId, Long itemId);

    ItemDto getItemById(Long userId, Long itemId);

    List<ItemDto> getItemsByOwnerId(Long userId, int from, int size);

    List<ItemDto> getItemsBySearchQuery(String searchText, int from, int size);

    ItemDto update(ItemDto itemDto, Long itemId, Long userId);

    void delete(Long itemId);

}
