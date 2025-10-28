package ru.practicum.shareit.item;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
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
    void deleteItemTest() {
        itemService.delete(1L);
        Mockito.verify(itemRepository).deleteById(1L);
        Mockito.verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository, commentRepository, itemRequestRepository);
    }
}
