package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

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
        User user = getUser(1L);

        ItemRequest itemRequest = getItemRequest(2L);

        Item item = getItem(3L);
        item.setOwner(user);
        item.setItemRequest(itemRequest);

        ItemDto createDto = ItemDto.builder()
                .requestId(itemRequest.getId())
                .build();

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(itemRequest.getId()))
                .thenReturn(Optional.of(itemRequest));
        when(itemRepository.save(any(Item.class)))
                .thenReturn(item);

        ItemDto resultDto = itemService.create(createDto, user.getId());

        assertThat(resultDto.getId(), equalTo(item.getId()));
        assertThat(resultDto.getName(), equalTo(item.getName()));
        assertThat(resultDto.getDescription(), equalTo(item.getDescription()));
        assertThat(resultDto.getAvailable(), equalTo(item.getAvailable()));
        assertThat(resultDto.getRequestId(), equalTo(itemRequest.getId()));

        verify(userRepository, times(1)).findById(user.getId());
        verify(itemRequestRepository, times(1)).findById(itemRequest.getId());
        verify(itemRepository, times(1)).save(any(Item.class));
        verifyNoMoreInteractions(itemRequestRepository, userRepository, itemRepository);
    }

    @Test
    void createComment_Ok() {
        User user = getUser(1L);
        Item item = getItem(2L);

        Comment comment = getComment(3L);
        comment.setItem(item);
        comment.setAuthor(user);

        CommentDto createDto = CommentDto.builder().build();

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(bookingRepository
                .findByItemIdAndUserIdAndExpiredEndDateAndApprovedStatus(eq(item.getId()), eq(user.getId()), any(LocalDateTime.class))
        )
                .thenReturn(List.of(new Booking()));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto resultDto = itemService.createComment(createDto, user.getId(), item.getId());

        assertThat(resultDto.getId(), equalTo(comment.getId()));
        assertThat(resultDto.getText(), equalTo(comment.getText()));
        assertThat(resultDto.getAuthorName(), equalTo(user.getName()));

        verify(userRepository, times(1)).findById(user.getId());
        verify(itemRepository, times(1)).findById(item.getId());
        verify(bookingRepository, times(1))
                .findByItemIdAndUserIdAndExpiredEndDateAndApprovedStatus(eq(item.getId()), eq(user.getId()), any(LocalDateTime.class));
        verify(commentRepository, times(1)).save(any(Comment.class));
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository, commentRepository, itemRequestRepository);
    }

    @Test
    void createCommentTest_NoBookings() {
        User user = getUser(1L);
        Item item = getItem(2L);

        Comment comment = getComment(3L);
        comment.setItem(item);
        comment.setAuthor(user);

        CommentDto createDto = CommentDto.builder().build();

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(bookingRepository
                .findByItemIdAndUserIdAndExpiredEndDateAndApprovedStatus(eq(item.getId()), eq(user.getId()), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        var validationException = assertThrows(ValidationException.class, () -> {
            itemService.createComment(createDto, user.getId(), item.getId());
        });

        assertThat(validationException.getMessage(), equalTo("Комментарии можно оставлять только к тем вещам, на которые было бронирование"));

        verify(userRepository, times(1)).findById(user.getId());
        verify(itemRepository, times(1)).findById(item.getId());
        verify(bookingRepository, times(1))
                .findByItemIdAndUserIdAndExpiredEndDateAndApprovedStatus(eq(item.getId()), eq(user.getId()), any(LocalDateTime.class));
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository, commentRepository, itemRequestRepository);
    }

    @Test
    void getItemByIdAsOwnerTest() {
        User owner = getUser(1L);
        User booker = getUser(2L);

        Item item = getItem(10L);
        item.setOwner(owner);

        Booking lastBooking = getBooking(100L, booker, item);
        lastBooking.setStart(LocalDateTime.now().minusDays(2));
        lastBooking.setEnd(LocalDateTime.now().minusDays(1));

        Booking nextBooking = getBooking(101L, booker, item);
        nextBooking.setStart(LocalDateTime.now().plusDays(1));
        nextBooking.setEnd(LocalDateTime.now().plusDays(2));

        List<Booking> bookingList = Arrays.asList(
                lastBooking,
                nextBooking
        );

        Comment comment1 = getComment(1000L);
        comment1.setAuthor(booker);

        Comment comment2 = getComment(1001L);
        comment2.setAuthor(booker);

        List<Comment> commentList = Arrays.asList(
                comment1,
                comment2
        );

        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(bookingRepository.findAllByItemId(item.getId()))
                .thenReturn(bookingList);
        when(commentRepository.findAllByItemId(item.getId()))
                .thenReturn(commentList);

        ItemDto resultDto = itemService.getItemById(owner.getId(), item.getId());

        assertThat(resultDto.getId(), equalTo(item.getId()));
        assertThat(resultDto.getName(), equalTo(item.getName()));
        assertThat(resultDto.getDescription(), equalTo(item.getDescription()));
        assertThat(resultDto.getAvailable(), equalTo(item.getAvailable()));

        assertThat(resultDto.getNextBooking().getId(), equalTo(nextBooking.getId()));
        assertThat(resultDto.getNextBooking().getBookerId(), equalTo(booker.getId()));
        assertThat(resultDto.getLastBooking().getId(), equalTo(lastBooking.getId()));
        assertThat(resultDto.getLastBooking().getBookerId(), equalTo(booker.getId()));

        assertThat(resultDto.getComments().size(), equalTo(2));
        assertThat(resultDto.getComments().get(0).getId(), equalTo(comment1.getId()));
        assertThat(resultDto.getComments().get(0).getText(), equalTo(comment1.getText()));
        assertThat(resultDto.getComments().get(0).getAuthorName(), equalTo(booker.getName()));
        assertThat(resultDto.getComments().get(1).getId(), equalTo(comment2.getId()));
        assertThat(resultDto.getComments().get(1).getText(), equalTo(comment2.getText()));
        assertThat(resultDto.getComments().get(1).getAuthorName(), equalTo(booker.getName()));

        verify(itemRepository, times(1)).findById(item.getId());
        verify(bookingRepository, times(1)).findAllByItemId(item.getId());
        verify(commentRepository, times(1)).findAllByItemId(item.getId());
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository, commentRepository, itemRequestRepository);
    }

    @Test
    void getItemByIdAsNotOwnerTest() {
        User owner = getUser(1L);
        User booker = getUser(2L);
        User notOwner = getUser(3L);

        Item item = getItem(10L);
        item.setOwner(owner);

        Booking lastBooking = getBooking(100L, booker, item);
        lastBooking.setStart(LocalDateTime.now().minusDays(2));
        lastBooking.setEnd(LocalDateTime.now().minusDays(1));

        Booking nextBooking = getBooking(101L, booker, item);
        nextBooking.setStart(LocalDateTime.now().plusDays(1));
        nextBooking.setEnd(LocalDateTime.now().plusDays(2));

        List<Booking> bookingList = Arrays.asList(
                lastBooking,
                nextBooking
        );

        Comment comment1 = getComment(1000L);
        comment1.setAuthor(booker);

        Comment comment2 = getComment(1001L);
        comment2.setAuthor(booker);

        List<Comment> commentList = Arrays.asList(
                comment1,
                comment2
        );

        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemId(item.getId()))
                .thenReturn(commentList);

        ItemDto resultDto = itemService.getItemById(notOwner.getId(), item.getId());

        assertThat(resultDto.getId(), equalTo(item.getId()));
        assertThat(resultDto.getName(), equalTo(item.getName()));
        assertThat(resultDto.getDescription(), equalTo(item.getDescription()));
        assertThat(resultDto.getAvailable(), equalTo(item.getAvailable()));

        assertThat(resultDto.getNextBooking(), equalTo(null));
        assertThat(resultDto.getLastBooking(), equalTo(null));

        assertThat(resultDto.getComments().size(), equalTo(2));
        assertThat(resultDto.getComments().get(0).getId(), equalTo(comment1.getId()));
        assertThat(resultDto.getComments().get(0).getText(), equalTo(comment1.getText()));
        assertThat(resultDto.getComments().get(0).getAuthorName(), equalTo(booker.getName()));
        assertThat(resultDto.getComments().get(1).getId(), equalTo(comment2.getId()));
        assertThat(resultDto.getComments().get(1).getText(), equalTo(comment2.getText()));
        assertThat(resultDto.getComments().get(1).getAuthorName(), equalTo(booker.getName()));

        verify(itemRepository, times(1)).findById(item.getId());
        verify(commentRepository, times(1)).findAllByItemId(item.getId());
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository, commentRepository, itemRequestRepository);
    }

    @Test
    void getItemsByOwnerIdTest() {
        User owner = getUser(1L);
        User booker = getUser(2L);

        Item item1 = getItem(10L);
        item1.setOwner(owner);

        Item item2 = getItem(11L);
        item2.setOwner(owner);

        List<Item> itemList = Arrays.asList(
                item1,
                item2
        );

        Booking item1lastBooking = getBooking(100L, booker, item1);
        item1lastBooking.setStart(LocalDateTime.now().minusDays(2));
        item1lastBooking.setEnd(LocalDateTime.now().minusDays(1));

        Booking item1nextBooking = getBooking(101L, booker, item1);
        item1nextBooking.setStart(LocalDateTime.now().plusDays(1));
        item1nextBooking.setEnd(LocalDateTime.now().plusDays(2));

        List<Booking> item1bookingList = Arrays.asList(
                item1lastBooking,
                item1nextBooking
        );

        Comment comment1 = getComment(1000L);
        comment1.setAuthor(booker);

        Comment comment2 = getComment(1001L);
        comment2.setAuthor(booker);

        List<Comment> item1commentList = Arrays.asList(
                comment1,
                comment2
        );

        when(itemRepository.findAllByOwnerId(eq(owner.getId()), any(Pageable.class)))
                .thenReturn(itemList);
        when(bookingRepository.findAllByItemId(item1.getId()))
                .thenReturn(item1bookingList);
        when(bookingRepository.findAllByItemId(item2.getId()))
                .thenReturn(new ArrayList<>());
        when(commentRepository.findAllByItemId(item1.getId()))
                .thenReturn(item1commentList);
        when(commentRepository.findAllByItemId(item2.getId()))
                .thenReturn(new ArrayList<>());

        List<ItemDto> resultDtoList = itemService.getItemsByOwnerId(owner.getId(), 0, 10);

        assertThat(resultDtoList.size(), equalTo(2));
        assertThat(resultDtoList.get(0).getId(), equalTo(item1.getId()));
        assertThat(resultDtoList.get(0).getName(), equalTo(item1.getName()));
        assertThat(resultDtoList.get(0).getDescription(), equalTo(item1.getDescription()));
        assertThat(resultDtoList.get(0).getAvailable(), equalTo(item1.getAvailable()));

        assertThat(resultDtoList.get(0).getNextBooking().getId(), equalTo(item1nextBooking.getId()));
        assertThat(resultDtoList.get(0).getNextBooking().getBookerId(), equalTo(booker.getId()));
        assertThat(resultDtoList.get(0).getLastBooking().getId(), equalTo(item1lastBooking.getId()));
        assertThat(resultDtoList.get(0).getLastBooking().getBookerId(), equalTo(booker.getId()));

        assertThat(resultDtoList.get(0).getComments().size(), equalTo(2));
        assertThat(resultDtoList.get(0).getComments().get(0).getId(), equalTo(comment1.getId()));
        assertThat(resultDtoList.get(0).getComments().get(0).getText(), equalTo(comment1.getText()));
        assertThat(resultDtoList.get(0).getComments().get(0).getAuthorName(), equalTo(booker.getName()));
        assertThat(resultDtoList.get(0).getComments().get(1).getId(), equalTo(comment2.getId()));
        assertThat(resultDtoList.get(0).getComments().get(1).getText(), equalTo(comment2.getText()));
        assertThat(resultDtoList.get(0).getComments().get(1).getAuthorName(), equalTo(booker.getName()));

        assertThat(resultDtoList.get(1).getId(), equalTo(item2.getId()));
        assertThat(resultDtoList.get(1).getName(), equalTo(item2.getName()));
        assertThat(resultDtoList.get(1).getDescription(), equalTo(item2.getDescription()));
        assertThat(resultDtoList.get(1).getAvailable(), equalTo(item2.getAvailable()));

        assertThat(resultDtoList.get(1).getNextBooking(), equalTo(null));
        assertThat(resultDtoList.get(1).getLastBooking(), equalTo(null));

        assertThat(resultDtoList.get(1).getComments().size(), equalTo(0));

        verify(itemRepository, times(1)).findAllByOwnerId(eq(owner.getId()), any(Pageable.class));
        verify(bookingRepository, times(1)).findAllByItemId(item1.getId());
        verify(bookingRepository, times(1)).findAllByItemId(item2.getId());
        verify(commentRepository, times(1)).findAllByItemId(item1.getId());
        verify(commentRepository, times(1)).findAllByItemId(item2.getId());
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository, commentRepository, itemRequestRepository);
    }

    @Test
    void getItemsBySearchQueryTest() {
        String searchText = "Item";

        Item item1 = getItem(1L);
        Item item2 = getItem(2L);

        List<Item> itemList = Arrays.asList(
                item1,
                item2
        );

        when(itemRepository.findBySearchText(eq(searchText), any(Pageable.class))).thenReturn(itemList);

        List<ItemDto> resultDtoList = itemService.getItemsBySearchQuery(searchText, 0, 10);

        assertThat(resultDtoList.size(), equalTo(2));

        assertThat(resultDtoList.get(0).getId(), equalTo(item1.getId()));
        assertThat(resultDtoList.get(0).getName(), equalTo(item1.getName()));
        assertThat(resultDtoList.get(0).getDescription(), equalTo(item1.getDescription()));
        assertThat(resultDtoList.get(0).getAvailable(), equalTo(item1.getAvailable()));

        assertThat(resultDtoList.get(1).getId(), equalTo(item2.getId()));
        assertThat(resultDtoList.get(1).getName(), equalTo(item2.getName()));
        assertThat(resultDtoList.get(1).getDescription(), equalTo(item2.getDescription()));
        assertThat(resultDtoList.get(1).getAvailable(), equalTo(item2.getAvailable()));

        verify(itemRepository, times(1)).findBySearchText(eq(searchText), any(Pageable.class));
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository, commentRepository, itemRequestRepository);
    }

    @Test
    void getItemsBySearchQueryTest_BlankQuery() {
        List<ItemDto> resultDtoList = itemService.getItemsBySearchQuery(" ", 0, 10);

        assertThat(resultDtoList.size(), equalTo(0));

        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository, commentRepository, itemRequestRepository);
    }

    @Test
    void updateItemTest() {
        ItemDto inputDto = ItemDto.builder().build();

        User owner = getUser(1L);

        Item item = getItem(10L);
        item.setOwner(owner);

        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class)))
                .thenReturn(item);

        ItemDto resultDto = itemService.update(inputDto, item.getId(), owner.getId());

        assertThat(resultDto.getId(), equalTo(item.getId()));
        assertThat(resultDto.getName(), equalTo(item.getName()));
        assertThat(resultDto.getDescription(), equalTo(item.getDescription()));
        assertThat(resultDto.getAvailable(), equalTo(item.getAvailable()));

        verify(itemRepository, times(1)).findById(item.getId());
        verify(itemRepository, times(1)).save(any(Item.class));
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository, commentRepository, itemRequestRepository);
    }

    @Test
    void updateItemTest_NotOwner() {
        ItemDto inputDto = ItemDto.builder().build();

        User owner = getUser(1L);
        User notOwner = getUser(2L);

        Item item = getItem(10L);
        item.setOwner(owner);

        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));

        var itemForbiddenException = assertThrows(ItemForbiddenException.class,
                () -> itemService.update(inputDto, item.getId(), notOwner.getId()));

        assertThat(itemForbiddenException.getMessage(), equalTo("Редактирование вещи доступно только владельцу"));

        verify(itemRepository, times(1)).findById(item.getId());
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository, commentRepository, itemRequestRepository);
    }

    @Test
    void updateItemTest_InvalidDto() {
        ItemDto inputDto = ItemDto.builder()
                .name("")
                .build();

        User owner = getUser(1L);

        Item item = getItem(10L);
        item.setOwner(owner);

        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));

        var validationException = assertThrows(ValidationException.class, () -> {
            itemService.update(inputDto, item.getId(), owner.getId());
        });

        assertThat(validationException.getMessage(), equalTo("Некорректное значение для обновления"));

        verify(itemRepository, times(1)).findById(item.getId());
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository, commentRepository, itemRequestRepository);
    }

    @Test
    void updateItemTest_Conflict() {
        ItemDto inputDto = ItemDto.builder().build();

        User owner = getUser(1L);

        Item item = getItem(10L);
        item.setOwner(owner);

        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class)))
                .thenThrow(new DataIntegrityViolationException("DataIntegrityViolationException"));

        var itemAlreadyExistsException = assertThrows(ItemAlreadyExistsException.class,
                () -> itemService.update(inputDto, item.getId(), owner.getId()));

        assertThat(itemAlreadyExistsException.getMessage(), equalTo("DataIntegrityViolationException"));

        verify(itemRepository, times(1)).findById(item.getId());
        verify(itemRepository, times(1)).save(any(Item.class));
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository, commentRepository, itemRequestRepository);
    }

    @Test
    void deleteItemTest() {
        itemService.delete(1L);

        verify(itemRepository, times(1)).deleteById(1L);
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository, commentRepository, itemRequestRepository);

    }

    private User getUser(Long id) {
        return User.builder()
                .id(id)
                .name("User " + id)
                .email("user" + id + "@test.ru")
                .build();
    }

    private Item getItem(Long id) {
        return Item.builder()
                .id(id)
                .name("Item " + id)
                .description("ItemDescription " + id)
                .available(true)
                .build();
    }

    private Comment getComment(Long id) {
        return Comment.builder()
                .id(id)
                .text("Comment text " + id)
                .build();
    }

    private ItemRequest getItemRequest(Long id) {
        return ItemRequest.builder()
                .id(id)
                .description("Request " + id)
                .build();
    }

    private Booking getBooking(Long id, User booker, Item item) {
        return Booking.builder()
                .id(id)
                .status(Status.APPROVED)
                .user(booker)
                .item(item)
                .build();
    }
}