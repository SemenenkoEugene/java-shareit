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
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private static final String USER_NOT_FOUND = "Пользователь не найден";
    private static final String REQUEST_NOT_FOUND = "Запрос не найден";
    private static final String ITEM_NOT_FOUND = "Вещь не найдена";

    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    @Transactional
    public ItemDto create(final ItemDto itemDto, final Long userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        final Item item = ItemMapper.toItem(itemDto);
        item.setOwner(user);

        if (itemDto.getRequestId() != null) {
            final ItemRequest itemRequest = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException(REQUEST_NOT_FOUND));
            item.setItemRequest(itemRequest);
        }

        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public CommentDto createComment(final CommentDto commentDto, final Long userId, final Long itemId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        final Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(ITEM_NOT_FOUND));

        final Comment comment = CommentMapper.toComment(commentDto);

        if (bookingRepository.findByItemIdAndUserIdAndExpiredEndDateAndApprovedStatus(itemId, userId, LocalDateTime.now()).isEmpty()) {
            throw new ValidationException("Комментарии можно оставлять только к тем вещам, на которые было бронирование");
        }

        comment.setAuthor(user);
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());

        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDto getItemById(final Long userId, final Long itemId) {
        final Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(ITEM_NOT_FOUND));
        final ItemDto itemDto = ItemMapper.toItemDto(item);

        if (Objects.equals(item.getOwner().getId(), userId)) {
            addBookingInfo(itemDto);
        }

        addCommentsInfo(itemDto);

        return itemDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> getItemsByOwnerId(final Long userId, final int from, final int size) {
        return itemRepository.findAllByOwnerId(userId, PageRequest.of(from / size, size)).stream()
                .map(ItemMapper::toItemDto)
                .map(this::addBookingInfo)
                .map(this::addCommentsInfo)
                .sorted(Comparator.comparing(ItemDto::getId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> getItemsBySearchQuery(final String searchText, final int from, final int size) {
        if (searchText.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository.findBySearchText(searchText, PageRequest.of(from / size, size)).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    @Transactional
    public ItemDto update(final ItemDto itemDto, final Long itemId, final Long userId) {
        final Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(ITEM_NOT_FOUND));

        if (!item.getOwner().getId().equals(userId)) {
            throw new ItemForbiddenException("Редактирование вещи доступно только владельцу");
        }
        Optional.ofNullable(itemDto.getName()).ifPresent(item::setName);
        Optional.ofNullable(itemDto.getDescription()).ifPresent(item::setDescription);
        Optional.ofNullable(itemDto.getAvailable()).ifPresent(item::setAvailable);

        try {
            return ItemMapper.toItemDto(itemRepository.save(item));
        } catch (DataIntegrityViolationException e) {
            throw new ItemAlreadyExistsException(e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void delete(final Long itemId) {
        itemRepository.deleteById(itemId);
    }

    private ItemDto addCommentsInfo(final ItemDto itemDto) {
        itemDto.setComments(commentRepository.findAllByItemId(itemDto.getId()).stream()
                .map(CommentMapper::toCommentDto)
                .toList());
        return itemDto;
    }

    private ItemDto addBookingInfo(final ItemDto itemDto) {
        final List<Booking> bookings = bookingRepository.findAllByItemId(itemDto.getId());

        final Booking nextBooking = bookings.stream()
                .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                .filter(booking -> booking.getStatus().equals(Status.APPROVED))
                .min(Comparator.comparing(Booking::getStart))
                .orElse(null);

        final Booking lastBooking = bookings.stream()
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
