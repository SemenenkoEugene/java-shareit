package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestGetResponseDto;
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
    public List<ItemRequestGetResponseDto> getAllByRequestorId(final Long userId, final int from, final int size) {

        final List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(userId, PageRequest.of(from / size, size));
        final Map<Long, List<Item>> itemsRequestId = itemRepository.findAllByItemRequestIn(itemRequests).stream()
                .collect(Collectors.groupingBy(item -> item.getItemRequest().getId()));

        return itemRequests.stream()
                .map(ItemRequestMapper::toGetResponseDto)
                .map(itemRequestGetResponseDto ->
                        addItemInfo(itemsRequestId.get(itemRequestGetResponseDto.getId()), itemRequestGetResponseDto))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestGetResponseDto> getAll(final Long userId, final int from, final int size) {
        return itemRequestRepository
                .findAllByRequestorIdNotOrderByCreatedDesc(userId, PageRequest.of(from / size, size)).stream()
                .map(ItemRequestMapper::toGetResponseDto)
                .map(this::addItemInfo)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ItemRequestGetResponseDto getById(final Long userId, final Long itemRequestId) {

        final ItemRequest itemRequest = itemRequestRepository.findById(itemRequestId)
                .orElseThrow(() -> new NotFoundException(REQUEST_NOT_FOUND));
        ItemRequestGetResponseDto responseDto = ItemRequestMapper.toGetResponseDto(itemRequest);
        responseDto = addItemInfo(responseDto);
        return responseDto;
    }

    @Override
    @Transactional
    public ItemRequestCreateResponseDto create(final ItemRequestCreateDto itemRequestCreateDto, final Long userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
        final ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestCreateDto);
        itemRequest.setRequestor(user);
        itemRequest.setCreated(LocalDateTime.now());

        return ItemRequestMapper.toCreateResponseDto(itemRequestRepository.save(itemRequest));
    }

    private ItemRequestGetResponseDto addItemInfo(final List<Item> items, final ItemRequestGetResponseDto itemRequestGetResponseDto) {

        return getItemRequestGetResponseDto(itemRequestGetResponseDto, items);
    }

    private ItemRequestGetResponseDto addItemInfo(final ItemRequestGetResponseDto itemRequestGetResponseDto) {
        final List<Item> items = itemRepository.findAllByItemRequestId(itemRequestGetResponseDto.getId());

        return getItemRequestGetResponseDto(itemRequestGetResponseDto, items);
    }

    private ItemRequestGetResponseDto getItemRequestGetResponseDto(final ItemRequestGetResponseDto itemRequestGetResponseDto, final List<Item> items) {

        itemRequestGetResponseDto.setItems(items == null || items.isEmpty() ? Collections.emptyList() :
                items.stream()
                        .map(item -> ItemRequestGetResponseDto.RequestedItem.builder()
                                .id(item.getId())
                                .name(item.getName())
                                .description(item.getDescription())
                                .available(item.getAvailable())
                                .requestId(item.getItemRequest().getId())
                                .build()
                        )
                        .toList());
        return itemRequestGetResponseDto;
    }
}
