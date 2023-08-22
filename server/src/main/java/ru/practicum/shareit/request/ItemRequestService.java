package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestGetResponseDto;

import java.util.List;

public interface ItemRequestService {
    List<ItemRequestGetResponseDto> getAllByRequestorId(Long userId, int from, int size);

    List<ItemRequestGetResponseDto> getAll(Long userId, int from, int size);

    ItemRequestGetResponseDto getById(Long userId, Long itemRequestId);

    ItemRequestCreateResponseDto create(ItemRequestCreateDto itemRequestCreateDto, Long userId);
}
