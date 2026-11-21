package ru.practicum.shareit.request;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.generated.request.dto.CreateItemRequestRequest;
import ru.practicum.shareit.generated.request.dto.ItemRequestCreateResponse;
import ru.practicum.shareit.generated.request.dto.ItemRequestResponse;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemRequestMapper {

    public static ItemRequestCreateResponse toCreateResponseDto(final ItemRequest itemRequest) {
        return ItemRequestCreateResponse.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .build();
    }

    public static ItemRequestResponse toGetResponseDto(final ItemRequest itemRequest) {
        return ItemRequestResponse.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .build();
    }

    public static ItemRequest toItemRequest(final CreateItemRequestRequest itemRequestCreateDto) {
        final ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(itemRequestCreateDto.description());
        return itemRequest;
    }
}
