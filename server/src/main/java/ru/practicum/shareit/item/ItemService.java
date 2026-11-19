package ru.practicum.shareit.item;

import ru.practicum.shareit.generated.item.dto.CommentResponse;
import ru.practicum.shareit.generated.item.dto.CreateCommentRequest;
import ru.practicum.shareit.generated.item.dto.CreateItemRequest;
import ru.practicum.shareit.generated.item.dto.ItemResponse;
import ru.practicum.shareit.generated.item.dto.UpdateItemRequest;

import java.util.List;

public interface ItemService {

    ItemResponse create(CreateItemRequest itemDto, Long userId);

    CommentResponse createComment(CreateCommentRequest commentDto, Long userId, Long itemId);

    ItemResponse getItemById(Long userId, Long itemId);

    List<ItemResponse> getItemsByOwnerId(Long userId, int from, int size);

    List<ItemResponse> getItemsBySearchQuery(String searchText, int from, int size);

    ItemResponse update(UpdateItemRequest itemDto, Long itemId, Long userId);

    void delete(Long itemId);

}
