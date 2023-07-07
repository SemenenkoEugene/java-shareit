package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Component("InMemoryItemStorage")
public class InMemoryItemStorage implements ItemStorage {

    private final Map<Long, Item> items = new HashMap<>();
    private Long currentId = 0L;

    @Override
    public Item create(Item item) {
        var nextId = getNextId();
        item.setId(nextId);
        items.put(nextId, item);
        return item;
    }

    private Long getNextId() {
        return ++currentId;
    }

    @Override
    public Item update(Long itemId, Long userId, Item itemUpdate) {
        var item = items.get(itemId);
        if (item == null) {
            throw new ItemNotFoundException("Вещь с ID=" + itemId + " не найдена!");
        }
        if (!Objects.equals(item.getOwnerId(), userId)) {
            throw new ItemNotFoundException("Попытка обновить элемент другого пользователя!");
        }
        updateItemProperties(item, itemUpdate);
        return item;
    }

    private void updateItemProperties(Item item, Item itemUpdate) {
        if (itemUpdate.getName() != null) {
            item.setName(itemUpdate.getName());
        }
        if (itemUpdate.getDescription() != null) {
            item.setDescription(itemUpdate.getDescription());
        }
        if (itemUpdate.getAvailable() != null) {
            item.setAvailable(itemUpdate.getAvailable());
        }
    }

    @Override
    public Item delete(Long itemId) {
        if (itemId == null) {
            throw new ValidationException("Передан пустой аргумент!");
        }
        if (!items.containsKey(itemId)) {
            throw new ItemNotFoundException("Вещь с ID=" + itemId + " не найдена!");
        }
        return items.remove(itemId);
    }

    @Override
    public Item getItemById(Long itemId) {
        if (!items.containsKey(itemId)) {
            throw new ItemNotFoundException("Вещь с ID=" + itemId + " не найдена!");
        }
        return items.get(itemId);
    }

    @Override
    public List<Item> getItemsByOwner(Long ownerId) {
        return items.values().stream()
                .filter(item -> item.getOwnerId().equals(ownerId)).collect(Collectors.toList());
    }

    @Override
    public void deleteItemsByOwner(Long ownerId) {
        var deleteIds = items.values().stream()
                .filter(item -> item.getOwnerId().equals(ownerId))
                .map(Item::getId)
                .collect(Collectors.toCollection(ArrayList::new));
        for (Long deleteId : deleteIds) {
            items.remove(deleteId);
        }
    }

    @Override
    public List<Item> getItemsBySearchQuery(String text) {
        List<Item> searchItems = new ArrayList<>();
        var lowerCaseText = text.toLowerCase();
        if (!text.isBlank()) {
            searchItems = items.values().stream()
                    .filter(Item::getAvailable)
                    .filter(item -> item.getName().toLowerCase().contains(lowerCaseText) ||
                                    item.getDescription().toLowerCase().contains(lowerCaseText))
                    .collect(Collectors.toList());
        }
        return searchItems;
    }
}
