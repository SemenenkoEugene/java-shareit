package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.OffsetBasedPageRequest;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.exception.ItemAlreadyExistsException;
import ru.practicum.shareit.exception.ItemForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.generated.item.dto.CommentResponse;
import ru.practicum.shareit.generated.item.dto.CreateCommentRequest;
import ru.practicum.shareit.generated.item.dto.CreateItemRequest;
import ru.practicum.shareit.generated.item.dto.ItemBookingResponse;
import ru.practicum.shareit.generated.item.dto.ItemResponse;
import ru.practicum.shareit.generated.item.dto.UpdateItemRequest;
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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public ItemResponse create(final CreateItemRequest itemDto, final Long userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        final Item item = ItemMapper.toItem(itemDto);
        item.setOwner(user);

        if (itemDto.requestId() != null) {
            final ItemRequest itemRequest = itemRequestRepository.findById(itemDto.requestId())
                    .orElseThrow(() -> new NotFoundException(REQUEST_NOT_FOUND));
            item.setItemRequest(itemRequest);
        }

        return ItemMapper.toItemResponse(itemRepository.save(item));
    }

    @Override
    @Transactional
    public CommentResponse createComment(final CreateCommentRequest commentDto, final Long userId, final Long itemId) {
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

        return CommentMapper.toCommentResponse(commentRepository.save(comment));
    }

    @Override
    @Transactional(readOnly = true)
    public ItemResponse getItemById(final Long userId, final Long itemId) {
        final Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(ITEM_NOT_FOUND));
        ItemResponse itemDto = ItemMapper.toItemResponse(item);

        if (Objects.equals(item.getOwner().getId(), userId)) {
            itemDto = addBookingInfo(itemDto);
        }

        return addCommentsInfo(itemDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> getItemsByOwnerId(final Long userId, final int from, final int size) {
        final List<Item> items = itemRepository.findAllByOwnerIdOrderByIdAsc(
                userId, new OffsetBasedPageRequest(from, size));
        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        final List<Long> itemIds = items.stream()
                .map(Item::getId)
                .toList();
        final Map<Long, List<Booking>> bookingsByItemId = bookingRepository.findAllByItemIdIn(itemIds).stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));
        final Map<Long, List<Comment>> commentsByItemId = commentRepository.findAllByItemIdIn(itemIds).stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));

        return items.stream()
                .map(ItemMapper::toItemResponse)
                .map(itemDto -> {
                    final List<Booking> bookings = bookingsByItemId.getOrDefault(itemDto.id(), List.of());
                    final Booking nextBooking = bookings.stream()
                            .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                            .filter(booking -> booking.getStatus().equals(Status.APPROVED))
                            .min(Comparator.comparing(Booking::getStart))
                            .orElse(null);
                    final LocalDateTime now = LocalDateTime.now();
                    final Booking lastBooking = bookings.stream()
                            .filter(booking -> isCompletedAt(booking, now))
                            .filter(booking -> booking.getStatus().equals(Status.APPROVED))
                            .max(Comparator.comparing(Booking::getEnd))
                            .orElse(null);

                    return itemDto.toBuilder()
                            .nextBooking(nextBooking != null ? ItemBookingResponse.builder()
                                    .id(nextBooking.getId())
                                    .bookerId(nextBooking.getUser().getId())
                                    .build() : null)
                            .lastBooking(lastBooking != null ? ItemBookingResponse.builder()
                                    .id(lastBooking.getId())
                                    .bookerId(lastBooking.getUser().getId())
                                    .build() : null)
                            .comments(commentsByItemId.getOrDefault(itemDto.id(), List.of()).stream()
                                    .map(CommentMapper::toCommentResponse)
                                    .toList())
                            .build();
                })
                .sorted(Comparator.comparing(ItemResponse::id))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> getItemsBySearchQuery(final String searchText, final int from, final int size) {
        if (searchText.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository.findBySearchText(searchText, new OffsetBasedPageRequest(from, size)).stream()
                .map(ItemMapper::toItemResponse)
                .toList();
    }

    @Override
    @Transactional
    public ItemResponse update(final UpdateItemRequest itemDto, final Long itemId, final Long userId) {
        final Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(ITEM_NOT_FOUND));

        if (!item.getOwner().getId().equals(userId)) {
            throw new ItemForbiddenException("Редактирование вещи доступно только владельцу");
        }
        Optional.ofNullable(itemDto.name()).ifPresent(item::setName);
        Optional.ofNullable(itemDto.description()).ifPresent(item::setDescription);
        Optional.ofNullable(itemDto.available()).ifPresent(item::setAvailable);

        try {
            return ItemMapper.toItemResponse(itemRepository.save(item));
        } catch (DataIntegrityViolationException e) {
            throw new ItemAlreadyExistsException(e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void delete(final Long itemId) {
        itemRepository.deleteById(itemId);
    }

    private ItemResponse addCommentsInfo(final ItemResponse itemDto) {
        return itemDto.toBuilder()
                .comments(commentRepository.findAllByItemId(itemDto.id()).stream()
                        .map(CommentMapper::toCommentResponse)
                        .toList())
                .build();
    }

    private ItemResponse addBookingInfo(final ItemResponse itemDto) {
        final List<Booking> bookings = bookingRepository.findAllByItemId(itemDto.id());

        final Booking nextBooking = bookings.stream()
                .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                .filter(booking -> booking.getStatus().equals(Status.APPROVED))
                .min(Comparator.comparing(Booking::getStart))
                .orElse(null);

        final LocalDateTime now = LocalDateTime.now();
        final Booking lastBooking = bookings.stream()
                .filter(booking -> isCompletedAt(booking, now))
                .filter(booking -> booking.getStatus().equals(Status.APPROVED))
                .max(Comparator.comparing(Booking::getEnd))
                .orElse(null);

        return itemDto.toBuilder()
                .nextBooking(nextBooking != null ? ItemBookingResponse.builder()
                        .id(nextBooking.getId())
                        .bookerId(nextBooking.getUser().getId())
                        .build() : null)
                .lastBooking(lastBooking != null ? ItemBookingResponse.builder()
                        .id(lastBooking.getId())
                        .bookerId(lastBooking.getUser().getId())
                        .build() : null)
                .build();
    }

    private boolean isCompletedAt(final Booking booking, final LocalDateTime timestamp) {
        return !booking.getEnd().isAfter(timestamp);
    }
}
