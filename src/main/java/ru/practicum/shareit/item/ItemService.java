package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto getItemById(Long itemId);

    ItemDto create(ItemDto itemDto, Long ownerId);

    ItemDto update(Long itemId, Long ownerId, ItemDto itemDto);

    ItemDto delete(Long itemId, Long ownerId);

    void deleteItemsByOwner(Long ownerId);

    List<ItemDto> getItemsBySearchQuery(String text);

    List<ItemDto> getItemsByOwner(Long ownerId);
}
