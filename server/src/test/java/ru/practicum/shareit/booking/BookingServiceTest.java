package ru.practicum.shareit.booking;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.RequestBookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
class BookingServiceTest {

    private static final User OWNER = User.builder()
            .id(1L)
            .name("User " + 1L)
            .email("user" + 1L + "@user.ru")
            .build();
    private static final User BOOKER = User.builder()
            .id(2L)
            .name("User " + 2L)
            .email("user" + 2L + "@user.ru")
            .build();

    private static final User UNRELATED = User.builder()
            .id(3L)
            .name("User " + 3L)
            .email("user" + 3L + "@user.ru")
            .build();

    private static final Item ITEM_1 = Item.builder()
            .id(10L)
            .name("Item " + 10L)
            .description("ItemDescription " + 10L)
            .available(true)
            .owner(OWNER)
            .build();
    private static final Item ITEM_2 = Item.builder()
            .id(11L)
            .name("Item " + 11L)
            .description("ItemDescription " + 11L)
            .available(false)
            .owner(OWNER)
            .build();

    private static final Booking BOOKING_1 = Booking.builder()
            .id(100L)
            .status(Status.APPROVED)
            .user(BOOKER)
            .item(ITEM_1)
            .build();
    private static final Booking BOOKING_2 = Booking.builder()
            .id(101L)
            .status(Status.APPROVED)
            .user(BOOKER)
            .item(ITEM_2)
            .build();

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @BeforeEach
    void setUp() {
        Mockito.when(bookingRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(BOOKING_1));
        Mockito.when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(OWNER));

    }

    @Test
    void getByIdTest() {

        final BookingResponseDto responseDto = bookingService.getById(BOOKING_1.getId(), OWNER.getId());

        Assertions.assertThat(responseDto.getId()).isEqualTo(BOOKING_1.getId());
        Assertions.assertThat(responseDto.getStatus()).isEqualTo(BOOKING_1.getStatus());
        Assertions.assertThat(responseDto.getBooker().getId()).isEqualTo(BOOKER.getId());
        Assertions.assertThat(responseDto.getItem().getId()).isEqualTo(ITEM_1.getId());
        Assertions.assertThat(responseDto.getItem().getName()).isEqualTo(ITEM_1.getName());

        Mockito.verify(bookingRepository, Mockito.times(1)).findById(BOOKING_1.getId());
        Mockito.verify(userRepository, Mockito.times(1)).findById(OWNER.getId());
        Mockito.verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository);
    }

    @Test
    void getByIdTest_UnrelatedUser() {

        Mockito.when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(UNRELATED));

        Assertions.assertThatThrownBy(() -> bookingService.getById(BOOKING_1.getId(), UNRELATED.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Не найдено подходящих бронирований для пользователя " + UNRELATED.getId());

        Mockito.verify(bookingRepository).findById(BOOKING_1.getId());
        Mockito.verify(userRepository).findById(UNRELATED.getId());
        Mockito.verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository);
    }

    @Test
    void getAllByStateTest() {

        final Booking booking1 = BOOKING_1.toBuilder()
                .start(LocalDateTime.now().minusDays(10))
                .end(LocalDateTime.now().minusDays(9))
                .build();

        final Booking booking2 = BOOKING_2.toBuilder()
                .start(LocalDateTime.now().minusDays(8))
                .end(LocalDateTime.now().minusDays(7))
                .build();

        final List<Booking> bookingList = List.of(booking1, booking2);

        Mockito.when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(BOOKER));
        Mockito.when(bookingRepository.findAllByUserIdOrderByStartDesc(Mockito.anyLong(), Mockito.any())).thenReturn(bookingList);
        Mockito.when(bookingRepository.findAllByUserIdAndEndBeforeOrderByStartDesc(Mockito.anyLong(), Mockito.any(), Mockito.any()))
                .thenReturn(bookingList);
        Mockito.when(bookingRepository.findAllByUserIdAndStartAfterOrderByStartDesc(Mockito.anyLong(), Mockito.any(), Mockito.any()))
                .thenReturn(bookingList);
        Mockito.when(bookingRepository.findAllByUserIdAndStartBeforeAndEndAfterOrderByStartDesc(Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(bookingList);
        Mockito.when(bookingRepository.findAllByUserIdAndStatusOrderByStartDesc(Mockito.anyLong(), Mockito.eq(Status.WAITING), Mockito.any()))
                .thenReturn(bookingList);
        Mockito.when(bookingRepository.findAllByUserIdAndStatusOrderByStartDesc(Mockito.anyLong(), Mockito.eq(Status.REJECTED), Mockito.any()))
                .thenReturn(bookingList);

        final List<BookingResponseDto> responseDtoList = bookingService.getAllByState(RequestBookingStatus.ALL, BOOKER.getId(), 0, 10);

        bookingService.getAllByState(RequestBookingStatus.PAST, BOOKER.getId(), 0, 10);
        bookingService.getAllByState(RequestBookingStatus.FUTURE, BOOKER.getId(), 0, 10);
        bookingService.getAllByState(RequestBookingStatus.CURRENT, BOOKER.getId(), 0, 10);
        bookingService.getAllByState(RequestBookingStatus.WAITING, BOOKER.getId(), 0, 10);
        bookingService.getAllByState(RequestBookingStatus.REJECTED, BOOKER.getId(), 0, 10);

        Assertions.assertThat(responseDtoList.get(0).getId()).isEqualTo(booking1.getId());
        Assertions.assertThat(responseDtoList.get(1).getId()).isEqualTo(booking2.getId());

        Mockito.verify(userRepository, Mockito.times(6)).findById(BOOKER.getId());
        Mockito.verify(bookingRepository)
                .findAllByUserIdOrderByStartDesc(Mockito.eq(BOOKER.getId()), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository)
                .findAllByUserIdAndEndBeforeOrderByStartDesc(Mockito.eq(BOOKER.getId()), Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository)
                .findAllByUserIdAndStartAfterOrderByStartDesc(Mockito.eq(BOOKER.getId()), Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository)
                .findAllByUserIdAndStartBeforeAndEndAfterOrderByStartDesc(Mockito.eq(BOOKER.getId()), Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository)
                .findAllByUserIdAndStatusOrderByStartDesc(Mockito.eq(BOOKER.getId()), Mockito.eq(Status.WAITING), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository)
                .findAllByUserIdAndStatusOrderByStartDesc(Mockito.eq(BOOKER.getId()), Mockito.eq(Status.REJECTED), Mockito.any(Pageable.class));

        Mockito.verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository);
    }

    @Test
    void getAllByStateForOwnerTest() {

        final Booking booking1 = BOOKING_1.toBuilder()
                .start(LocalDateTime.now().minusDays(10))
                .end(LocalDateTime.now().minusDays(9))
                .build();

        final Booking booking2 = BOOKING_2.toBuilder()
                .start(LocalDateTime.now().minusDays(8))
                .end(LocalDateTime.now().minusDays(7))
                .build();

        final List<Booking> bookingList = List.of(booking1, booking2);

        Mockito.when(bookingRepository.findAllByItemOwnerIdOrderByStartDesc(Mockito.anyLong(), Mockito.any()))
                .thenReturn(bookingList);
        Mockito.when(bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(Mockito.anyLong(), Mockito.any(), Mockito.any()))
                .thenReturn(bookingList);
        Mockito.when(bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(Mockito.anyLong(), Mockito.any(), Mockito.any()))
                .thenReturn(bookingList);
        Mockito.when(bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(bookingList);
        Mockito.when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(Mockito.any(), Mockito.eq(Status.WAITING), Mockito.any()))
                .thenReturn(bookingList);
        Mockito.when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(Mockito.anyLong(), Mockito.eq(Status.REJECTED), Mockito.any()))
                .thenReturn(bookingList);

        final List<BookingResponseDto> responseDtoList = bookingService.getAllByStateForOwner(RequestBookingStatus.ALL, OWNER.getId(), 0, 10);

        bookingService.getAllByStateForOwner(RequestBookingStatus.PAST, OWNER.getId(), 0, 10);
        bookingService.getAllByStateForOwner(RequestBookingStatus.FUTURE, OWNER.getId(), 0, 10);
        bookingService.getAllByStateForOwner(RequestBookingStatus.CURRENT, OWNER.getId(), 0, 10);
        bookingService.getAllByStateForOwner(RequestBookingStatus.WAITING, OWNER.getId(), 0, 10);
        bookingService.getAllByStateForOwner(RequestBookingStatus.REJECTED, OWNER.getId(), 0, 10);

        Assertions.assertThat(responseDtoList.get(0).getId()).isEqualTo(booking1.getId());
        Assertions.assertThat(responseDtoList.get(1).getId()).isEqualTo(booking2.getId());

        Mockito.verify(userRepository, Mockito.times(6)).findById(OWNER.getId());
        Mockito.verify(bookingRepository).findAllByItemOwnerIdOrderByStartDesc(Mockito.eq(OWNER.getId()), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository).findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(Mockito.eq(OWNER.getId()),
                Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository).findAllByItemOwnerIdAndStartAfterOrderByStartDesc(Mockito.eq(OWNER.getId()),
                Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository).findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(Mockito.eq(OWNER.getId()),
                Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository).findAllByItemOwnerIdAndStatusOrderByStartDesc(Mockito.eq(OWNER.getId()), Mockito.eq(Status.WAITING), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository).findAllByItemOwnerIdAndStatusOrderByStartDesc(Mockito.eq(OWNER.getId()), Mockito.eq(Status.REJECTED), Mockito.any(Pageable.class));

        Mockito.verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository);
    }

    @Test
    void createTest_NotAvailableItem() {

        final Item item = ITEM_1.toBuilder()
                .available(false)
                .build();

        final BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(item.getId())
                .build();

        Mockito.when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(BOOKER));
        Mockito.when(itemRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(item));

        Assertions.assertThatThrownBy(() -> bookingService.create(requestDto, BOOKER.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Вещь не доступна для бронирования");

        Mockito.verify(userRepository).findById(BOOKER.getId());
        Mockito.verify(itemRepository).findById(item.getId());
        Mockito.verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository);
    }

    @Test
    void createTest_BookOwnItem() {

        final BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(ITEM_1.getId())
                .build();

        Mockito.when(itemRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(ITEM_1));

        Assertions.assertThatThrownBy(() -> bookingService.create(requestDto, OWNER.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Владелец не может бронировать свою вещь");


        Mockito.verify(userRepository).findById(OWNER.getId());
        Mockito.verify(itemRepository).findById(ITEM_1.getId());
        Mockito.verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository);
    }

    @Test
    void approveTest() {

        final Booking booking = BOOKING_1.toBuilder()
                .status(Status.WAITING)
                .build();

        Mockito.when(bookingRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(booking));
        Mockito.when(bookingRepository.save(Mockito.any())).thenReturn(booking);

        final BookingResponseDto responseDto = bookingService.approve(booking.getId(), true, OWNER.getId());

        Assertions.assertThat(responseDto.getId()).isEqualTo(booking.getId());
        Assertions.assertThat(responseDto.getStatus()).isEqualTo(booking.getStatus());
        Assertions.assertThat(responseDto.getBooker().getId()).isEqualTo(BOOKER.getId());
        Assertions.assertThat(responseDto.getItem().getId()).isEqualTo(ITEM_1.getId());
        Assertions.assertThat(responseDto.getItem().getName()).isEqualTo(ITEM_1.getName());

        Mockito.verify(bookingRepository).findById(booking.getId());
        Mockito.verify(userRepository).findById(OWNER.getId());
        Mockito.verify(bookingRepository).save(Mockito.any(Booking.class));
        Mockito.verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository);
    }

    @Test
    void approveTest_ByNotOwner() {

        final Booking booking = BOOKING_1.toBuilder()
                .status(Status.WAITING)
                .build();

        Mockito.when(bookingRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(booking));
        Mockito.when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(BOOKER));

        Assertions.assertThatThrownBy(() -> bookingService.approve(booking.getId(), true, BOOKER.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Подтверждение доступно только для владельца вещи");

        Mockito.verify(bookingRepository).findById(booking.getId());
        Mockito.verify(userRepository).findById(BOOKER.getId());
        Mockito.verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository);
    }

    @Test
    void approveTest_ForNotWaitingBooking() {

        final Booking booking = BOOKING_1.toBuilder()
                .status(Status.APPROVED)
                .build();

        Mockito.when(bookingRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(booking));

        Assertions.assertThatThrownBy(() -> bookingService.approve(booking.getId(), true, OWNER.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Вещь не ожидает подтверждения");

        Mockito.verify(bookingRepository).findById(booking.getId());
        Mockito.verify(userRepository).findById(OWNER.getId());
        Mockito.verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository);
    }
}