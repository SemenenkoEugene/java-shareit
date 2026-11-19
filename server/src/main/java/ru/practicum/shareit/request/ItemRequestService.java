package ru.practicum.shareit.request;

import ru.practicum.shareit.generated.request.dto.CreateItemRequestRequest;
import ru.practicum.shareit.generated.request.dto.ItemRequestCreateResponse;
import ru.practicum.shareit.generated.request.dto.ItemRequestResponse;

import java.util.List;

public interface ItemRequestService {
    List<ItemRequestResponse> getAllByRequestorId(Long userId, int from, int size);

    List<ItemRequestResponse> getAll(Long userId, int from, int size);

    ItemRequestResponse getById(Long userId, Long itemRequestId);

    ItemRequestCreateResponse create(CreateItemRequestRequest itemRequestCreateDto, Long userId);
}
