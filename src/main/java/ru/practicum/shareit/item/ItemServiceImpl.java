package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    private final ItemStorage itemStorage;
    private final ItemMapper itemMapper;
    private final UserStorage userStorage;

    @Autowired
    public ItemServiceImpl(ItemStorage itemStorage, ItemMapper itemMapper, UserStorage userStorage) {
        this.itemStorage = itemStorage;
        this.itemMapper = itemMapper;
        this.userStorage = userStorage;
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        return itemMapper.toItemDto(itemStorage.getItemById(itemId));
    }

    @Override
    public ItemDto create(ItemDto itemDto, Long ownerId) {
        var userById = userStorage.getUserById(ownerId);
        var item = itemMapper.toItem(itemDto, userById.getId());
        return itemMapper.toItemDto(itemStorage.create(item));
    }

    @Override
    public ItemDto update(Long itemId, Long ownerId, ItemDto itemDto) {
        checkUserId(ownerId);
        var itemById = itemStorage.getItemById(itemId);
        if (!itemById.getOwnerId().equals(ownerId)){
            throw new ItemNotFoundException("У пользователя нет такой вещи!");
        }
        if (itemDto.getId() == null) {
            itemDto.setId(itemId);
        }
        var item = itemMapper.toItem(itemDto, ownerId);
        return itemMapper.toItemDto(itemStorage.update(itemId, ownerId, item));
    }

    @Override
    public ItemDto delete(Long itemId, Long ownerId) {
        var itemById = itemStorage.getItemById(itemId);
        if (!itemById.getOwnerId().equals(ownerId)) {
            throw new ItemNotFoundException("У пользователя нет такой вещи!");
        }
        return itemMapper.toItemDto(itemStorage.delete(itemId));
    }

    @Override
    public void deleteItemsByOwner(Long ownerId) {
        itemStorage.deleteItemsByOwner(ownerId);
    }

    @Override
    public List<ItemDto> getItemsBySearchQuery(String text) {
        text = text.toLowerCase();
        return itemStorage.getItemsBySearchQuery(text).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long ownerId) {
        return itemStorage.getItemsByOwner(ownerId).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private void checkUserId(Long userId) {
        userStorage.getUserById(userId);
    }
}
