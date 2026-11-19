package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.OffsetBasedPageRequest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.generated.request.dto.CreateItemRequestRequest;
import ru.practicum.shareit.generated.request.dto.ItemRequestCreateResponse;
import ru.practicum.shareit.generated.request.dto.ItemRequestResponse;
import ru.practicum.shareit.generated.request.dto.RequestedItemResponse;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private static final String USER_NOT_FOUND = "Пользователь не найден";
    private static final String REQUEST_NOT_FOUND = "Запрос не найден";

    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestResponse> getAllByRequestorId(final Long userId, final int from, final int size) {

        final List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequestorIdOrderByCreatedDescIdDesc(
                userId, new OffsetBasedPageRequest(from, size));
        final Map<Long, List<Item>> itemsRequestId = itemRepository.findAllByItemRequestIn(itemRequests).stream()
                .collect(Collectors.groupingBy(item -> item.getItemRequest().getId()));

        return itemRequests.stream()
                .map(ItemRequestMapper::toGetResponseDto)
                .map(itemRequestResponse ->
                        addItemInfo(itemsRequestId.get(itemRequestResponse.id()), itemRequestResponse))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestResponse> getAll(final Long userId, final int from, final int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
        final List<ItemRequest> itemRequests = itemRequestRepository
                .findAllByRequestorIdNotOrderByCreatedDescIdDesc(
                        userId, new OffsetBasedPageRequest(from, size));
        if (itemRequests.isEmpty()) {
            return Collections.emptyList();
        }

        final Map<Long, List<Item>> itemsByRequestId = itemRepository.findAllByItemRequestIn(itemRequests).stream()
                .collect(Collectors.groupingBy(item -> item.getItemRequest().getId()));

        return itemRequests.stream()
                .map(ItemRequestMapper::toGetResponseDto)
                .map(dto -> addItemInfo(itemsByRequestId.get(dto.id()), dto))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ItemRequestResponse getById(final Long userId, final Long itemRequestId) {

        final ItemRequest itemRequest = itemRequestRepository.findById(itemRequestId)
                .orElseThrow(() -> new NotFoundException(REQUEST_NOT_FOUND));
        ItemRequestResponse responseDto = ItemRequestMapper.toGetResponseDto(itemRequest);
        responseDto = addItemInfo(responseDto);
        return responseDto;
    }

    @Override
    @Transactional
    public ItemRequestCreateResponse create(final CreateItemRequestRequest itemRequestCreateDto, final Long userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
        final ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestCreateDto);
        itemRequest.setRequestor(user);
        itemRequest.setCreated(LocalDateTime.now());

        return ItemRequestMapper.toCreateResponseDto(itemRequestRepository.save(itemRequest));
    }

    private ItemRequestResponse addItemInfo(final List<Item> items, final ItemRequestResponse itemRequestResponse) {

        return getItemRequestResponse(itemRequestResponse, items);
    }

    private ItemRequestResponse addItemInfo(final ItemRequestResponse itemRequestResponse) {
        final List<Item> items = itemRepository.findAllByItemRequestId(itemRequestResponse.id());

        return getItemRequestResponse(itemRequestResponse, items);
    }

    private ItemRequestResponse getItemRequestResponse(final ItemRequestResponse itemRequestResponse,
                                                       final List<Item> items) {
        return itemRequestResponse.toBuilder()
                .items(items == null || items.isEmpty() ? Collections.emptyList() : items.stream()
                        .map(item -> RequestedItemResponse.builder()
                                .id(item.getId())
                                .name(item.getName())
                                .description(item.getDescription())
                                .available(item.getAvailable())
                                .requestId(item.getItemRequest().getId())
                                .build()
                        )
                        .toList())
                .build();
    }
}
