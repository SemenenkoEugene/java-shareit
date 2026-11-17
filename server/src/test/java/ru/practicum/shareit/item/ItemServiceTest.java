package ru.practicum.shareit.item;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.exception.ItemAlreadyExistsException;
import ru.practicum.shareit.exception.ItemForbiddenException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
class ItemServiceTest {

    private static final User USER = User.builder()
            .id(1L)
            .name("User")
            .email("user@test.ru")
            .build();
    private static final Item ITEM = Item.builder()
            .id(3L)
            .name("Item")
            .description("ItemDescription")
            .available(true)
            .build();
    private static final ItemRequest ITEM_REQUEST = ItemRequest.builder()
            .id(2L)
            .description("Request")
            .build();
    private static final Comment COMMENT = Comment.builder()
            .id(1L)
            .text("Comment text")
            .build();

    private static final CommentDto COMMENT_DTO = CommentDto.builder().build();
    private static final ItemDto ITEM_DTO = ItemDto.builder().id(3L).build();

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void createItem_Ok() {

        final Item item = ITEM.toBuilder()
                .owner(USER)
                .itemRequest(ITEM_REQUEST)
                .build();

        final ItemDto createDto = ItemDto.builder()
                .requestId(ITEM_REQUEST.getId())
                .build();

        Mockito.when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(USER));
        Mockito.when(itemRequestRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(ITEM_REQUEST));
        Mockito.when(itemRepository.save(Mockito.any())).thenReturn(item);

        final ItemDto resultDto = itemService.create(createDto, USER.getId());

        Assertions.assertThat(resultDto)
                .isNotNull()
                .extracting(ItemDto::getId, ItemDto::getName, ItemDto::getDescription, ItemDto::getAvailable, ItemDto::getRequestId)
                .containsExactly(item.getId(), item.getName(), item.getDescription(), item.getAvailable(), ITEM_REQUEST.getId());

        Mockito.verify(userRepository).findById(USER.getId());
        Mockito.verify(itemRequestRepository).findById(ITEM_REQUEST.getId());
        Mockito.verify(itemRepository).save(Mockito.any(Item.class));
        Mockito.verifyNoMoreInteractions(itemRequestRepository, userRepository, itemRepository);
    }

    @Test
    void createComment_Ok() {
        final Comment comment = COMMENT.toBuilder()
                .item(ITEM)
                .author(USER)
                .build();

        Mockito.when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(USER));
        Mockito.when(itemRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(ITEM));
        Mockito.when(bookingRepository.findByItemIdAndUserIdAndExpiredEndDateAndApprovedStatus(Mockito.anyLong(), Mockito.anyLong(), Mockito.any()))
                .thenReturn(List.of(new Booking()));
        Mockito.when(commentRepository.save(Mockito.any())).thenReturn(comment);

        final CommentDto resultDto = itemService.createComment(COMMENT_DTO, USER.getId(), ITEM.getId());

        Assertions.assertThat(resultDto)
                .isNotNull()
                .extracting(CommentDto::getId, CommentDto::getText, CommentDto::getAuthorName)
                .containsExactly(comment.getId(), comment.getText(), USER.getName());

        Mockito.verify(userRepository).findById(USER.getId());
        Mockito.verify(itemRepository).findById(ITEM.getId());
        Mockito.verify(bookingRepository).findByItemIdAndUserIdAndExpiredEndDateAndApprovedStatus(
                Mockito.eq(ITEM.getId()), Mockito.eq(USER.getId()), Mockito.any(LocalDateTime.class));
        Mockito.verify(commentRepository).save(Mockito.any(Comment.class));
        Mockito.verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository, commentRepository, itemRequestRepository);
    }

    @Test
    void createCommentTest_NoBookings() {

        Mockito.when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(USER));
        Mockito.when(itemRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(ITEM));
        Mockito.when(bookingRepository.findByItemIdAndUserIdAndExpiredEndDateAndApprovedStatus(Mockito.anyLong(), Mockito.anyLong(), Mockito.any()))
                .thenReturn(new ArrayList<>());

        Assertions.assertThatThrownBy(() ->
                        itemService.createComment(COMMENT_DTO, USER.getId(), ITEM.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Комментарии можно оставлять только к тем вещам, на которые было бронирование");
    }

    @Test
    void updateItemTest_NotOwner() {
        final User notOwner = USER.toBuilder()
                .id(2L)
                .build();
        final Item item = ITEM.toBuilder()
                .owner(USER)
                .build();

        Mockito.when(itemRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(item));

        Assertions.assertThatThrownBy(() ->
                        itemService.update(ITEM_DTO, item.getId(), notOwner.getId()))
                .isInstanceOf(ItemForbiddenException.class)
                .hasMessage("Редактирование вещи доступно только владельцу");
    }

    @Test
    void updateItemTest_Conflict() {
        final Item item = ITEM.toBuilder()
                .owner(USER)
                .build();

        Mockito.when(itemRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(item));
        Mockito.when(itemRepository.save(Mockito.any())).thenThrow(new DataIntegrityViolationException("DataIntegrityViolationException"));

        Assertions.assertThatThrownBy(() ->
                        itemService.update(ITEM_DTO, item.getId(), USER.getId()))
                .isInstanceOf(ItemAlreadyExistsException.class)
                .hasMessage("DataIntegrityViolationException");
    }

    @Test
    void getItemByIdShouldUseOnlyApprovedBookingForLastBooking() {
        final LocalDateTime now = LocalDateTime.now();
        final Item item = ITEM.toBuilder().owner(USER).build();
        final Booking approved = booking(1L, now.minusHours(6), now.minusHours(5), Status.APPROVED);
        final Booking waiting = booking(2L, now.minusHours(4), now.minusHours(3), Status.WAITING);
        final Booking rejected = booking(3L, now.minusHours(2), now.minusHours(1), Status.REJECTED);

        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.findAllByItemId(item.getId()))
                .thenReturn(List.of(approved, waiting, rejected));

        final ItemDto result = itemService.getItemById(USER.getId(), item.getId());

        Assertions.assertThat(result.getLastBooking())
                .isNotNull()
                .extracting(ItemDto.ItemBooking::getId, ItemDto.ItemBooking::getBookerId)
                .containsExactly(approved.getId(), USER.getId());
    }

    @Test
    void getItemByIdShouldReturnNullLastBookingWhenThereAreNoApprovedBookings() {
        final LocalDateTime now = LocalDateTime.now();
        final Item item = ITEM.toBuilder().owner(USER).build();
        final Booking waiting = booking(2L, now.minusHours(4), now.minusHours(3), Status.WAITING);
        final Booking rejected = booking(3L, now.minusHours(2), now.minusHours(1), Status.REJECTED);

        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.findAllByItemId(item.getId()))
                .thenReturn(List.of(waiting, rejected));

        final ItemDto result = itemService.getItemById(USER.getId(), item.getId());

        Assertions.assertThat(result.getLastBooking()).isNull();
    }

    @Test
    void getItemByIdShouldUseLatestCompletedApprovedBookingForLastBooking() {
        final LocalDateTime now = LocalDateTime.of(2030, 1, 1, 12, 0);
        final Item item = ITEM.toBuilder().owner(USER).build();
        final Booking completed = booking(1L, now.minusHours(4), now.minusHours(3), Status.APPROVED);
        final Booking active = booking(2L, now.minusHours(2), now.plusHours(1), Status.APPROVED);
        final Booking future = booking(3L, now.plusHours(2), now.plusHours(3), Status.APPROVED);
        final Booking endingNow = booking(4L, now.minusHours(1), now, Status.APPROVED);
        final Booking waiting = booking(5L, now.minusHours(2), now, Status.WAITING);
        final Booking rejected = booking(6L, now.minusHours(2), now, Status.REJECTED);

        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.findAllByItemId(item.getId()))
                .thenReturn(List.of(completed, active, future, waiting, rejected, endingNow));

        final ItemDto result;
        try (MockedStatic<LocalDateTime> localDateTime = Mockito.mockStatic(LocalDateTime.class,
                Mockito.CALLS_REAL_METHODS)) {
            localDateTime.when(LocalDateTime::now).thenReturn(now);
            result = itemService.getItemById(USER.getId(), item.getId());
        }

        Assertions.assertThat(result.getLastBooking())
                .isNotNull()
                .extracting(ItemDto.ItemBooking::getId, ItemDto.ItemBooking::getBookerId)
                .containsExactly(endingNow.getId(), USER.getId());
    }

    @Test
    void getItemsByOwnerIdShouldLoadRelatedDataWithTwoBatchCalls() {
        final LocalDateTime now = LocalDateTime.now();
        final Item firstItem = ITEM.toBuilder().id(10L).owner(USER).build();
        final Item secondItem = ITEM.toBuilder().id(20L).owner(USER).build();
        final Item thirdItem = ITEM.toBuilder().id(30L).owner(USER).build();
        final Booking firstLast = booking(1L, now.minusHours(6), now.minusHours(5), Status.APPROVED, firstItem);
        final Booking ignoredWaiting = booking(2L, now.minusHours(4), now.minusHours(3), Status.WAITING, firstItem);
        final Booking firstNext = booking(3L, now.plusHours(3), now.plusHours(4), Status.APPROVED, firstItem);
        final Booking ignoredRejected = booking(4L, now.plusHours(1), now.plusHours(2), Status.REJECTED, firstItem);
        final Booking secondLast = booking(5L, now.minusHours(2), now.minusHours(1), Status.APPROVED, secondItem);
        final Comment firstComment = COMMENT.toBuilder().item(firstItem).author(USER).created(now).build();

        Mockito.when(itemRepository.findAllByOwnerIdOrderByIdAsc(Mockito.eq(USER.getId()), Mockito.any()))
                .thenReturn(List.of(secondItem, firstItem, thirdItem));
        Mockito.when(bookingRepository.findAllByItemIdIn(Mockito.anyCollection()))
                .thenReturn(List.of(firstLast, ignoredWaiting, firstNext, ignoredRejected, secondLast));
        Mockito.when(commentRepository.findAllByItemIdIn(Mockito.anyCollection()))
                .thenReturn(List.of(firstComment));

        final List<ItemDto> result = itemService.getItemsByOwnerId(USER.getId(), 0, 10);

        Assertions.assertThat(result).extracting(ItemDto::getId)
                .containsExactly(firstItem.getId(), secondItem.getId(), thirdItem.getId());
        Assertions.assertThat(result.get(0).getLastBooking().getId()).isEqualTo(firstLast.getId());
        Assertions.assertThat(result.get(0).getNextBooking().getId()).isEqualTo(firstNext.getId());
        Assertions.assertThat(result.get(0).getComments()).extracting(CommentDto::getId)
                .containsExactly(firstComment.getId());
        Assertions.assertThat(result.get(1).getLastBooking().getId()).isEqualTo(secondLast.getId());
        Assertions.assertThat(result.get(1).getNextBooking()).isNull();
        Assertions.assertThat(result.get(1).getComments()).isEmpty();
        Assertions.assertThat(result.get(2).getLastBooking()).isNull();
        Assertions.assertThat(result.get(2).getNextBooking()).isNull();
        Assertions.assertThat(result.get(2).getComments()).isEmpty();

        Mockito.verify(bookingRepository).findAllByItemIdIn(Mockito.argThat(itemIds ->
                itemIds.containsAll(List.of(firstItem.getId(), secondItem.getId(), thirdItem.getId()))));
        Mockito.verify(commentRepository).findAllByItemIdIn(Mockito.argThat(itemIds ->
                itemIds.containsAll(List.of(firstItem.getId(), secondItem.getId(), thirdItem.getId()))));
        Mockito.verify(bookingRepository, Mockito.never()).findAllByItemId(Mockito.anyLong());
        Mockito.verify(commentRepository, Mockito.never()).findAllByItemId(Mockito.anyLong());
    }

    @Test
    void getItemsByOwnerIdShouldUseLatestCompletedApprovedBookingForLastBooking() {
        final LocalDateTime now = LocalDateTime.of(2030, 1, 1, 12, 0);
        final Item item = ITEM.toBuilder().owner(USER).build();
        final Booking completed = booking(1L, now.minusHours(4), now.minusHours(3), Status.APPROVED, item);
        final Booking active = booking(2L, now.minusHours(2), now.plusHours(1), Status.APPROVED, item);
        final Booking future = booking(3L, now.plusHours(2), now.plusHours(3), Status.APPROVED, item);
        final Booking endingNow = booking(4L, now.minusHours(1), now, Status.APPROVED, item);
        final Booking waiting = booking(5L, now.minusHours(2), now, Status.WAITING, item);
        final Booking rejected = booking(6L, now.minusHours(2), now, Status.REJECTED, item);

        Mockito.when(itemRepository.findAllByOwnerIdOrderByIdAsc(Mockito.eq(USER.getId()), Mockito.any()))
                .thenReturn(List.of(item));
        Mockito.when(bookingRepository.findAllByItemIdIn(Mockito.anyCollection()))
                .thenReturn(List.of(completed, active, future, waiting, rejected, endingNow));
        Mockito.when(commentRepository.findAllByItemIdIn(Mockito.anyCollection())).thenReturn(List.of());

        final List<ItemDto> result;
        try (MockedStatic<LocalDateTime> localDateTime = Mockito.mockStatic(LocalDateTime.class,
                Mockito.CALLS_REAL_METHODS)) {
            localDateTime.when(LocalDateTime::now).thenReturn(now);
            result = itemService.getItemsByOwnerId(USER.getId(), 0, 10);
        }

        Assertions.assertThat(result).singleElement()
                .extracting(ItemDto::getLastBooking)
                .extracting(ItemDto.ItemBooking::getId, ItemDto.ItemBooking::getBookerId)
                .containsExactly(endingNow.getId(), USER.getId());
    }

    @Test
    void getItemsByOwnerIdShouldSkipRelatedDataQueriesForEmptyPage() {
        Mockito.when(itemRepository.findAllByOwnerIdOrderByIdAsc(Mockito.eq(USER.getId()), Mockito.any()))
                .thenReturn(List.of());

        Assertions.assertThat(itemService.getItemsByOwnerId(USER.getId(), 20, 10)).isEmpty();

        Mockito.verifyNoInteractions(bookingRepository, commentRepository);
    }

    @Test
    void deleteItemTest() {
        itemService.delete(1L);
        Mockito.verify(itemRepository).deleteById(1L);
        Mockito.verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository, commentRepository, itemRequestRepository);
    }

    private Booking booking(final Long id, final LocalDateTime start, final LocalDateTime end, final Status status) {
        return booking(id, start, end, status, ITEM);
    }

    private Booking booking(final Long id, final LocalDateTime start, final LocalDateTime end, final Status status,
                            final Item item) {
        return Booking.builder()
                .id(id)
                .start(start)
                .end(end)
                .item(item)
                .user(USER)
                .status(status)
                .build();
    }
}
