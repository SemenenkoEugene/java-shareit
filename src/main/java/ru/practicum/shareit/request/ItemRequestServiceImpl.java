package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestGetResponseDto;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    public List<ItemRequestGetResponseDto> getAllByRequestorId(Long userId, int from, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
        return itemRequestRepository
                .findAllByRequestorIdOrderByCreatedDesc(userId, PageRequest.of(from / size, size)).stream()
                .map(ItemRequestMapper::toGetResponseDto)
                .map(this::addItemInfo)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestGetResponseDto> getAll(Long userId, int from, int size) {
        return itemRequestRepository
                .findAllByRequestorIdNotOrderByCreatedDesc(userId, PageRequest.of(from / size, size)).stream()
                .map(ItemRequestMapper::toGetResponseDto)
                .map(this::addItemInfo)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ItemRequestGetResponseDto getById(Long userId, Long itemRequestId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
        var itemRequest = itemRequestRepository.findById(itemRequestId)
                .orElseThrow(() -> new ItemRequestNotFoundException(REQUEST_NOT_FOUND));
        var responseDto = ItemRequestMapper.toGetResponseDto(itemRequest);
        responseDto = addItemInfo(responseDto);
        return responseDto;
    }

    @Override
    @Transactional
    public ItemRequestCreateResponseDto create(ItemRequestCreateDto itemRequestCreateDto, Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
        var itemRequest = ItemRequestMapper.toItemRequest(itemRequestCreateDto);
        itemRequest.setRequestor(user);
        itemRequest.setCreated(LocalDateTime.now());

        return ItemRequestMapper.toCreateResponseDto(itemRequestRepository.save(itemRequest));
    }

    private ItemRequestGetResponseDto addItemInfo(ItemRequestGetResponseDto itemRequestGetResponseDto) {
        var items = itemRepository.findAllByItemRequestId(itemRequestGetResponseDto.getId());

        itemRequestGetResponseDto.setItems(items.isEmpty() ? new ArrayList<>() :
                items.stream()
                        .map(item -> ItemRequestGetResponseDto.RequestedItem.builder()
                                .id(item.getId())
                                .name(item.getName())
                                .description(item.getDescription())
                                .available(item.getAvailable())
                                .requestId(item.getItemRequest().getId())
                                .build()
                        )
                        .collect(Collectors.toList())
        );
        return itemRequestGetResponseDto;
    }
}
