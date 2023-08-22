package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.exception.ItemAlreadyExistsException;
import ru.practicum.shareit.exception.ItemForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;

import javax.validation.Validation;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private static final String USER_NOT_FOUND = "Пользователь не найден";
    private static final String REQUEST_NOT_FOUND = "Запрос не найден";
    private static final String ITEM_NOT_FOUND = "Вещь не найдена";
    private static final String INVALID_VALUE_FOR_UPDATE = "Некорректное значение для обновления";

    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    @Transactional
    public ItemDto create(ItemDto itemDto, Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        var item = ItemMapper.toItem(itemDto);
        item.setOwner(user);

        if (itemDto.getRequestId() != null) {
            var itemRequest = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException(REQUEST_NOT_FOUND));
            item.setItemRequest(itemRequest);
        }

        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public CommentDto createComment(CommentDto commentDto, Long userId, Long itemId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        var item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(ITEM_NOT_FOUND));

        var comment = CommentMapper.toComment(commentDto);

        if (bookingRepository.findByItemIdAndUserIdAndExpiredEndDateAndApprovedStatus(itemId,
                userId,
                LocalDateTime.now()).isEmpty()) {
            throw new ValidationException("Комментарии можно оставлять только к тем вещам, на которые было бронирование");
        }
        comment.setAuthor(user);
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());

        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDto getItemById(Long userId, Long itemId) {
        var item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(ITEM_NOT_FOUND));
        var itemDto = ItemMapper.toItemDto(item);
        if ((Objects.equals(item.getOwner().getId(), userId))) {
            addBookingInfo(itemDto);
        }
        addCommentsInfo(itemDto);
        return itemDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> getItemsByOwnerId(Long userId, int from, int size) {
        return itemRepository.findAllByOwnerId(userId, PageRequest.of(from / size, size)).stream()
                .map(ItemMapper::toItemDto)
                .map(this::addBookingInfo)
                .map(this::addCommentsInfo)
                .sorted(Comparator.comparing(ItemDto::getId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> getItemsBySearchQuery(String searchText, int from, int size) {
        if (searchText.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository.findBySearchText(searchText, PageRequest.of(from / size, size)).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ItemDto update(ItemDto itemDto, Long itemId, Long userId) {
        var item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(ITEM_NOT_FOUND));

        if (!item.getOwner().getId().equals(userId)) {
            throw new ItemForbiddenException("Редактирование вещи доступно только владельцу");
        }
        Optional.ofNullable(itemDto.getName()).ifPresent(item::setName);
        Optional.ofNullable(itemDto.getDescription()).ifPresent(item::setDescription);
        Optional.ofNullable(itemDto.getAvailable()).ifPresent(item::setAvailable);

        if (isValid(ItemMapper.toItemDto(item))) {
            try {
                return ItemMapper.toItemDto(itemRepository.save(item));
            } catch (DataIntegrityViolationException e) {
                throw new ItemAlreadyExistsException(e.getMessage());
            }
        } else {
            throw new ValidationException(INVALID_VALUE_FOR_UPDATE);
        }
    }

    @Override
    @Transactional
    public void delete(Long itemId) {
        itemRepository.deleteById(itemId);
    }

    private boolean isValid(ItemDto itemDto) {
        var validator = Validation.buildDefaultValidatorFactory().getValidator();
        var violations = validator.validate(itemDto);
        return violations.isEmpty();
    }

    private ItemDto addCommentsInfo(ItemDto itemDto) {
        itemDto.setComments(commentRepository.findAllByItemId(itemDto.getId()).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList()));
        return itemDto;
    }

    private ItemDto addBookingInfo(ItemDto itemDto) {
        var bookings = bookingRepository.findAllByItemId(itemDto.getId());

        var nextBooking = bookings.stream()
                .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                .filter(booking -> booking.getStatus().equals(Status.APPROVED))
                .min(Comparator.comparing(Booking::getStart))
                .orElse(null);

        var lastBooking = bookings.stream()
                .filter(booking -> booking.getStart().isBefore(LocalDateTime.now()))
                .max(Comparator.comparing(Booking::getEnd))
                .orElse(null);

        itemDto.setNextBooking(nextBooking != null ? ItemDto.ItemBooking.builder()
                .id(nextBooking.getId())
                .bookerId(nextBooking.getUser().getId())
                .build() : null);

        itemDto.setLastBooking(lastBooking != null ? ItemDto.ItemBooking.builder()
                .id(lastBooking.getId())
                .bookerId(lastBooking.getUser().getId())
                .build() : null);

        return itemDto;
    }
}
