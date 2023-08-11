package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.RequestBookingStatus;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void getByIdTest() {
        User owner = getUser(1L);
        User booker = getUser(2L);

        Item item = getItem(10L, owner);

        Booking booking = getBooking(100L, booker, item);

        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));
        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));

        BookingResponseDto responseDto = bookingService.getById(booking.getId(), owner.getId());

        assertThat(responseDto.getId(), equalTo(booking.getId()));
        assertThat(responseDto.getStatus(), equalTo(booking.getStatus()));
        assertThat(responseDto.getBooker().getId(), equalTo(booker.getId()));
        assertThat(responseDto.getItem().getId(), equalTo(item.getId()));
        assertThat(responseDto.getItem().getName(), equalTo(item.getName()));

        verify(bookingRepository, times(1)).findById(booking.getId());
        verify(userRepository, times(1)).findById(owner.getId());
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository);
    }

    @Test
    void getByIdTest_UnrelatedUser() {
        User owner = getUser(1L);
        User booker = getUser(2L);
        User unrelated = getUser(3L);

        Item item = getItem(10L, owner);

        Booking booking = getBooking(100L, booker, item);

        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));
        when(userRepository.findById(unrelated.getId()))
                .thenReturn(Optional.of(unrelated));

        var bookingNotFoundException = assertThrows(BookingNotFoundException.class,
                () -> bookingService.getById(booking.getId(), unrelated.getId()));

        assertThat(bookingNotFoundException.getMessage(), equalTo("Не найдено подходящих бронирований для пользователя " + unrelated.getId()));

        verify(bookingRepository, times(1)).findById(booking.getId());
        verify(userRepository, times(1)).findById(unrelated.getId());
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository);
    }

    @Test
    void getAllByStateTest() {
        User owner = getUser(1L);
        User booker = getUser(2L);

        Item item1 = getItem(10L, owner);
        Item item2 = getItem(11L, owner);

        Booking booking1 = getBooking(100L, booker, item1);
        booking1.setStart(LocalDateTime.now().minusDays(10));
        booking1.setEnd(LocalDateTime.now().minusDays(9));
        Booking booking2 = getBooking(101L, booker, item2);
        booking2.setStart(LocalDateTime.now().minusDays(8));
        booking2.setEnd(LocalDateTime.now().minusDays(7));

        List<Booking> bookingList = Arrays.asList(
                booking1,
                booking2
        );

        when(userRepository.findById(booker.getId()))
                .thenReturn(Optional.of(booker));

        when(bookingRepository.findAllByUserIdOrderByStartDesc(eq(booker.getId()), any(Pageable.class)))
                .thenReturn(bookingList);

        when(bookingRepository.findAllByUserIdAndEndBeforeOrderByStartDesc(eq(booker.getId()),
                any(LocalDateTime.class),
                any(Pageable.class)))
                .thenReturn(bookingList);

        when(bookingRepository.findAllByUserIdAndStartAfterOrderByStartDesc(eq(booker.getId()),
                any(LocalDateTime.class),
                any(Pageable.class)))
                .thenReturn(bookingList);

        when(bookingRepository.findAllByUserIdAndStartBeforeAndEndAfterOrderByStartDesc(eq(booker.getId()),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(Pageable.class)))
                .thenReturn(bookingList);

        when(bookingRepository.findAllByUserIdAndStatusOrderByStartDesc(eq(booker.getId()),
                eq(Status.WAITING),
                any(Pageable.class)))
                .thenReturn(bookingList);
        when(bookingRepository.findAllByUserIdAndStatusOrderByStartDesc(eq(booker.getId()),
                eq(Status.REJECTED),
                any(Pageable.class)))
                .thenReturn(bookingList);

        List<BookingResponseDto> responseDtoList;

        responseDtoList = bookingService.getAllByState(RequestBookingStatus.ALL, booker.getId(), 0, 10);
        bookingService.getAllByState(RequestBookingStatus.PAST, booker.getId(), 0, 10);
        bookingService.getAllByState(RequestBookingStatus.FUTURE, booker.getId(), 0, 10);
        bookingService.getAllByState(RequestBookingStatus.CURRENT, booker.getId(), 0, 10);
        bookingService.getAllByState(RequestBookingStatus.WAITING, booker.getId(), 0, 10);
        bookingService.getAllByState(RequestBookingStatus.REJECTED, booker.getId(), 0, 10);

        assertThat(responseDtoList.get(0).getId(), equalTo(booking1.getId()));
        assertThat(responseDtoList.get(1).getId(), equalTo(booking2.getId()));

        verify(userRepository, times(6)).findById(booker.getId());
        verify(bookingRepository, times(1))
                .findAllByUserIdOrderByStartDesc(eq(booker.getId()), any(Pageable.class));
        verify(bookingRepository, times(1))
                .findAllByUserIdAndEndBeforeOrderByStartDesc(eq(booker.getId()), any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, times(1))
                .findAllByUserIdAndStartAfterOrderByStartDesc(eq(booker.getId()), any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, times(1))
                .findAllByUserIdAndStartBeforeAndEndAfterOrderByStartDesc(eq(booker.getId()), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, times(1))
                .findAllByUserIdAndStatusOrderByStartDesc(eq(booker.getId()), eq(Status.WAITING), any(Pageable.class));
        verify(bookingRepository, times(1))
                .findAllByUserIdAndStatusOrderByStartDesc(eq(booker.getId()), eq(Status.REJECTED), any(Pageable.class));

        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository);
    }

    @Test
    void getAllByStateForOwnerTest() {
        User owner = getUser(1L);
        User booker = getUser(2L);

        Item item1 = getItem(10L, owner);
        Item item2 = getItem(11L, owner);

        Booking booking1 = getBooking(100L, booker, item1);
        booking1.setStart(LocalDateTime.now().minusDays(10));
        booking1.setEnd(LocalDateTime.now().minusDays(9));
        Booking booking2 = getBooking(101L, booker, item2);
        booking2.setStart(LocalDateTime.now().minusDays(8));
        booking2.setEnd(LocalDateTime.now().minusDays(7));

        List<Booking> bookingList = Arrays.asList(
                booking1,
                booking2
        );

        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByItemOwnerIdOrderByStartDesc(eq(owner.getId()),
                any(Pageable.class)))
                .thenReturn(bookingList);
        when(bookingRepository
                .findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(eq(owner.getId()),
                        any(LocalDateTime.class),
                        any(Pageable.class)))
                .thenReturn(bookingList);
        when(bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(eq(owner.getId()),
                any(LocalDateTime.class),
                any(Pageable.class)))
                .thenReturn(bookingList);
        when(bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(eq(owner.getId()),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(Pageable.class)))
                .thenReturn(bookingList);
        when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(eq(owner.getId()),
                eq(Status.WAITING),
                any(Pageable.class)))
                .thenReturn(bookingList);
        when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(eq(owner.getId()),
                eq(Status.REJECTED),
                any(Pageable.class)))
                .thenReturn(bookingList);

        List<BookingResponseDto> responseDtoList;

        responseDtoList = bookingService.getAllByStateForOwner(RequestBookingStatus.ALL, owner.getId(), 0, 10);
        bookingService.getAllByStateForOwner(RequestBookingStatus.PAST, owner.getId(), 0, 10);
        bookingService.getAllByStateForOwner(RequestBookingStatus.FUTURE, owner.getId(), 0, 10);
        bookingService.getAllByStateForOwner(RequestBookingStatus.CURRENT, owner.getId(), 0, 10);
        bookingService.getAllByStateForOwner(RequestBookingStatus.WAITING, owner.getId(), 0, 10);
        bookingService.getAllByStateForOwner(RequestBookingStatus.REJECTED, owner.getId(), 0, 10);

        assertThat(responseDtoList.get(0).getId(), equalTo(booking1.getId()));
        assertThat(responseDtoList.get(1).getId(), equalTo(booking2.getId()));

        verify(userRepository, times(6)).findById(owner.getId());
        verify(bookingRepository, times(1))
                .findAllByItemOwnerIdOrderByStartDesc(eq(owner.getId()), any(Pageable.class));
        verify(bookingRepository, times(1))
                .findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(eq(owner.getId()),
                        any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, times(1))
                .findAllByItemOwnerIdAndStartAfterOrderByStartDesc(eq(owner.getId()),
                        any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, times(1))
                .findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(eq(owner.getId()),
                        any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, times(1))
                .findAllByItemOwnerIdAndStatusOrderByStartDesc(eq(owner.getId()), eq(Status.WAITING), any(Pageable.class));
        verify(bookingRepository, times(1))
                .findAllByItemOwnerIdAndStatusOrderByStartDesc(eq(owner.getId()), eq(Status.REJECTED), any(Pageable.class));

        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository);
    }

    @Test
    void createTest_NotAvailableItem() {
        User owner = getUser(1L);
        User booker = getUser(2L);

        Item item = getItem(10L, owner);
        item.setAvailable(false);

        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(item.getId())
                .build();

        when(userRepository.findById(booker.getId()))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));

        var validationException = assertThrows(ValidationException.class,
                () -> bookingService.create(requestDto, booker.getId()));

        assertThat(validationException.getMessage(), equalTo("Вещь не доступна для бронирования"));

        verify(userRepository, times(1)).findById(booker.getId());
        verify(itemRepository, times(1)).findById(item.getId());
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository);
    }

    @Test
    void createTest_BookOwnItem() {
        User owner = getUser(1L);

        Item item = getItem(10L, owner);

        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(item.getId())
                .build();

        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));
        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));

        var bookingNotFoundException = assertThrows(BookingNotFoundException.class,
                () -> bookingService.create(requestDto, owner.getId()));

        assertThat(bookingNotFoundException.getMessage(), equalTo("Владелец не может бронировать свою вещь"));

        verify(userRepository, times(1)).findById(owner.getId());
        verify(itemRepository, times(1)).findById(item.getId());
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository);
    }

    @Test
    void approveTest() {
        User owner = getUser(1L);
        User booker = getUser(2L);

        Item item = getItem(10L, owner);

        Booking booking = getBooking(100L, booker, item);
        booking.setStatus(Status.WAITING);

        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));
        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));
        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        BookingResponseDto responseDto = bookingService.approve(booking.getId(), true, owner.getId());

        assertThat(responseDto.getId(), equalTo(booking.getId()));
        assertThat(responseDto.getStatus(), equalTo(booking.getStatus()));
        assertThat(responseDto.getBooker().getId(), equalTo(booker.getId()));
        assertThat(responseDto.getItem().getId(), equalTo(item.getId()));
        assertThat(responseDto.getItem().getName(), equalTo(item.getName()));

        verify(bookingRepository, times(1)).findById(booking.getId());
        verify(userRepository, times(1)).findById(owner.getId());
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository);
    }

    @Test
    void approveTest_ByNotOwner() {
        User owner = getUser(1L);
        User booker = getUser(2L);

        Item item = getItem(10L, owner);

        Booking booking = getBooking(100L, booker, item);
        booking.setStatus(Status.WAITING);

        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));
        when(userRepository.findById(booker.getId()))
                .thenReturn(Optional.of(booker));

        var bookingNotFoundException = assertThrows(BookingNotFoundException.class,
                () -> bookingService.approve(booking.getId(), true, booker.getId()));

        assertThat(bookingNotFoundException.getMessage(), equalTo("Подтверждение доступно только для владельца вещи"));

        verify(bookingRepository, times(1)).findById(booking.getId());
        verify(userRepository, times(1)).findById(booker.getId());
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository);
    }

    @Test
    void approveTest_ForNotWaitingBooking() {
        User owner = getUser(1L);
        User booker = getUser(2L);

        Item item = getItem(10L, owner);

        Booking booking = getBooking(100L, booker, item);
        booking.setStatus(Status.APPROVED);

        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));
        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));

        var validationException = assertThrows(ValidationException.class,
                () -> bookingService.approve(booking.getId(), true, owner.getId()));

        assertThat(validationException.getMessage(), equalTo("Вещь не ожидает подтверждения"));

        verify(bookingRepository, times(1)).findById(booking.getId());
        verify(userRepository, times(1)).findById(owner.getId());
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository);
    }

    private User getUser(Long id) {
        return User.builder()
                .id(id)
                .name("User " + id)
                .email("user" + id + "@user.ru")
                .build();
    }

    private Item getItem(Long id, User owner) {
        return Item.builder()
                .id(id)
                .name("Item " + id)
                .description("ItemDescription " + id)
                .available(true)
                .owner(owner)
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