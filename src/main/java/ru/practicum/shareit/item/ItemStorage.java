package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    Item create(Item item);

    Item update(Long itemId, Long userId, Item itemUpdate);

    Item delete(Long itemId);

    Item getItemById(Long itemId);

    List<Item> getItemsByOwner(Long ownerId);

    void deleteItemsByOwner(Long ownerId);

    List<Item> getItemsBySearchQuery(String text);
}
