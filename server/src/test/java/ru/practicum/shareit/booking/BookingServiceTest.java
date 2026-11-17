package ru.practicum.shareit.booking;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
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
import java.util.stream.Stream;

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

    @ParameterizedTest
    @MethodSource("userBookingPaginationArguments")
    void getAllByStateShouldUseFromAsOffset(final int from, final int size,
                                             final List<Long> expectedBookingIds) {
        final List<Booking> bookings = Stream.iterate(0, index -> index + 1)
                .limit(5)
                .map(index -> BOOKING_1.toBuilder()
                        .id(100L + index)
                        .start(LocalDateTime.now().minusDays(index))
                        .end(LocalDateTime.now().minusDays(index).plusHours(1))
                        .build())
                .toList();

        Mockito.when(bookingRepository.findAllByUserIdOrderByStartDesc(Mockito.eq(BOOKER.getId()), Mockito.any()))
                .thenAnswer(invocation -> {
                    final Pageable pageable = invocation.getArgument(1);
                    final int firstIndex = Math.toIntExact(pageable.getOffset());
                    if (firstIndex >= bookings.size()) {
                        return List.of();
                    }
                    return bookings.subList(firstIndex, Math.min(firstIndex + pageable.getPageSize(), bookings.size()));
                });

        final List<Long> actualBookingIds = bookingService
                .getAllByState(RequestBookingStatus.ALL, BOOKER.getId(), from, size).stream()
                .map(BookingResponseDto::getId)
                .toList();

        Assertions.assertThat(actualBookingIds).isEqualTo(expectedBookingIds);
    }

    private static Stream<Arguments> userBookingPaginationArguments() {
        return Stream.of(
                Arguments.of(0, 2, List.of(100L, 101L)),
                Arguments.of(1, 2, List.of(101L, 102L)),
                Arguments.of(2, 2, List.of(102L, 103L)),
                Arguments.of(3, 2, List.of(103L, 104L)),
                Arguments.of(4, 2, List.of(104L))
        );
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

    @ParameterizedTest
    @MethodSource("ownerBookingPaginationArguments")
    void getAllByStateForOwnerShouldUseFromAsOffset(final int from, final int size,
                                                    final List<Long> expectedBookingIds) {
        final List<Booking> bookings = Stream.iterate(0, index -> index + 1)
                .limit(5)
                .map(index -> BOOKING_1.toBuilder()
                        .id(100L + index)
                        .start(LocalDateTime.now().minusDays(index))
                        .end(LocalDateTime.now().minusDays(index).plusHours(1))
                        .build())
                .toList();

        Mockito.when(bookingRepository.findAllByItemOwnerIdOrderByStartDesc(Mockito.eq(OWNER.getId()), Mockito.any()))
                .thenAnswer(invocation -> {
                    final Pageable pageable = invocation.getArgument(1);
                    final int firstIndex = Math.toIntExact(pageable.getOffset());
                    if (firstIndex >= bookings.size()) {
                        return List.of();
                    }
                    return bookings.subList(firstIndex, Math.min(firstIndex + pageable.getPageSize(), bookings.size()));
                });

        final List<Long> actualBookingIds = bookingService
                .getAllByStateForOwner(RequestBookingStatus.ALL, OWNER.getId(), from, size).stream()
                .map(BookingResponseDto::getId)
                .toList();

        Assertions.assertThat(actualBookingIds).isEqualTo(expectedBookingIds);
    }

    private static Stream<Arguments> ownerBookingPaginationArguments() {
        return Stream.of(
                Arguments.of(0, 2, List.of(100L, 101L)),
                Arguments.of(2, 2, List.of(102L, 103L)),
                Arguments.of(3, 2, List.of(103L, 104L)),
                Arguments.of(4, 2, List.of(104L)),
                Arguments.of(6, 2, List.of())
        );
    }

    @Test
    void createTest() {

        final LocalDateTime start = LocalDateTime.now().plusDays(1);
        final LocalDateTime end = start.plusDays(2);
        final BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(ITEM_1.getId())
                .start(start)
                .end(end)
                .build();
        final Booking savedBooking = Booking.builder()
                .id(100L)
                .start(start)
                .end(end)
                .status(Status.WAITING)
                .user(BOOKER)
                .item(ITEM_1)
                .build();
        final ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);

        Mockito.when(userRepository.findById(BOOKER.getId())).thenReturn(Optional.of(BOOKER));
        Mockito.when(itemRepository.findById(ITEM_1.getId())).thenReturn(Optional.of(ITEM_1));
        Mockito.when(bookingRepository.save(Mockito.any(Booking.class))).thenReturn(savedBooking);

        final BookingResponseDto responseDto = bookingService.create(requestDto, BOOKER.getId());

        Mockito.verify(userRepository).findById(BOOKER.getId());
        Mockito.verify(itemRepository).findById(ITEM_1.getId());
        Mockito.verify(bookingRepository).save(bookingCaptor.capture());

        Assertions.assertThat(bookingCaptor.getValue())
                .satisfies(booking -> {
                    Assertions.assertThat(booking.getStatus()).isEqualTo(Status.WAITING);
                    Assertions.assertThat(booking.getUser()).isEqualTo(BOOKER);
                    Assertions.assertThat(booking.getItem()).isEqualTo(ITEM_1);
                    Assertions.assertThat(booking.getStart()).isEqualTo(start);
                    Assertions.assertThat(booking.getEnd()).isEqualTo(end);
                });
        Assertions.assertThat(responseDto)
                .satisfies(dto -> {
                    Assertions.assertThat(dto.getId()).isEqualTo(savedBooking.getId());
                    Assertions.assertThat(dto.getStatus()).isEqualTo(Status.WAITING);
                    Assertions.assertThat(dto.getBooker().getId()).isEqualTo(BOOKER.getId());
                    Assertions.assertThat(dto.getItem().getId()).isEqualTo(ITEM_1.getId());
                    Assertions.assertThat(dto.getItem().getName()).isEqualTo(ITEM_1.getName());
                    Assertions.assertThat(dto.getStart()).isEqualTo(start);
                    Assertions.assertThat(dto.getEnd()).isEqualTo(end);
                });
        Mockito.verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository);
    }

    @ParameterizedTest
    @MethodSource("bookingDateArguments")
    void createShouldRequireEndAfterStart(final LocalDateTime start, final LocalDateTime end,
                                          final boolean validDates) {
        final BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(ITEM_1.getId())
                .start(start)
                .end(end)
                .build();
        final Booking savedBooking = Booking.builder()
                .id(100L)
                .start(start)
                .end(end)
                .status(Status.WAITING)
                .user(BOOKER)
                .item(ITEM_1)
                .build();

        Mockito.when(userRepository.findById(BOOKER.getId())).thenReturn(Optional.of(BOOKER));
        Mockito.when(itemRepository.findById(ITEM_1.getId())).thenReturn(Optional.of(ITEM_1));
        Mockito.when(bookingRepository.save(Mockito.any(Booking.class))).thenReturn(savedBooking);

        if (validDates) {
            final BookingResponseDto response = bookingService.create(requestDto, BOOKER.getId());

            Assertions.assertThat(response.getStart()).isEqualTo(start);
            Assertions.assertThat(response.getEnd()).isEqualTo(end);
            Mockito.verify(bookingRepository).save(Mockito.any(Booking.class));
        } else {
            Assertions.assertThatThrownBy(() -> bookingService.create(requestDto, BOOKER.getId()))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("Дата окончания бронирования должна быть позже даты начала");
            Mockito.verify(bookingRepository, Mockito.never()).save(Mockito.any(Booking.class));
        }
    }

    private static Stream<Arguments> bookingDateArguments() {
        final LocalDateTime start = LocalDateTime.of(2030, 1, 2, 12, 0);
        return Stream.of(
                Arguments.of(start, start.minusMinutes(1), false),
                Arguments.of(start, start, false),
                Arguments.of(start, start.plusMinutes(1), true)
        );
    }

    @ParameterizedTest
    @MethodSource("nullBookingDateArguments")
    void createShouldRejectNullDates(final LocalDateTime start, final LocalDateTime end) {
        final BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(ITEM_1.getId())
                .start(start)
                .end(end)
                .build();

        Mockito.when(userRepository.findById(BOOKER.getId())).thenReturn(Optional.of(BOOKER));
        Mockito.when(itemRepository.findById(ITEM_1.getId())).thenReturn(Optional.of(ITEM_1));

        Assertions.assertThatThrownBy(() -> bookingService.create(requestDto, BOOKER.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Дата начала и окончания бронирования должны быть указаны");
        Mockito.verify(bookingRepository, Mockito.never()).save(Mockito.any(Booking.class));
    }

    private static Stream<Arguments> nullBookingDateArguments() {
        final LocalDateTime date = LocalDateTime.of(2030, 1, 2, 12, 0);
        return Stream.of(
                Arguments.of(null, date),
                Arguments.of(date, null),
                Arguments.of(null, null)
        );
    }

    @ParameterizedTest
    @MethodSource("nonFutureStartArguments")
    void createShouldRequireStartAfterCurrentTime(final LocalDateTime start) {
        final LocalDateTime now = LocalDateTime.of(2030, 1, 2, 12, 0);
        final LocalDateTime end = now.plusHours(2);
        final BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(ITEM_1.getId())
                .start(start)
                .end(end)
                .build();
        final Booking savedBooking = Booking.builder()
                .id(100L)
                .start(start)
                .end(end)
                .status(Status.WAITING)
                .user(BOOKER)
                .item(ITEM_1)
                .build();

        Mockito.when(userRepository.findById(BOOKER.getId())).thenReturn(Optional.of(BOOKER));
        Mockito.when(itemRepository.findById(ITEM_1.getId())).thenReturn(Optional.of(ITEM_1));
        Mockito.when(bookingRepository.save(Mockito.any(Booking.class))).thenReturn(savedBooking);

        try (MockedStatic<LocalDateTime> localDateTime = Mockito.mockStatic(LocalDateTime.class,
                Mockito.CALLS_REAL_METHODS)) {
            localDateTime.when(LocalDateTime::now).thenReturn(now);

            Assertions.assertThatThrownBy(() -> bookingService.create(requestDto, BOOKER.getId()))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("Дата начала бронирования должна быть позже текущего времени");
        }
        Mockito.verify(bookingRepository, Mockito.never()).save(Mockito.any(Booking.class));
    }

    private static Stream<Arguments> nonFutureStartArguments() {
        final LocalDateTime now = LocalDateTime.of(2030, 1, 2, 12, 0);
        return Stream.of(
                Arguments.of(now.minusNanos(1)),
                Arguments.of(now)
        );
    }

    @Test
    void createTest_UserNotFound() {

        final BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(ITEM_1.getId())
                .build();

        Mockito.when(userRepository.findById(BOOKER.getId())).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> bookingService.create(requestDto, BOOKER.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь не найден");

        Mockito.verify(userRepository).findById(BOOKER.getId());
        Mockito.verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository);
    }

    @Test
    void createTest_ItemNotFound() {

        final BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(ITEM_1.getId())
                .build();

        Mockito.when(userRepository.findById(BOOKER.getId())).thenReturn(Optional.of(BOOKER));
        Mockito.when(itemRepository.findById(ITEM_1.getId())).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> bookingService.create(requestDto, BOOKER.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Вещь не найдена");

        Mockito.verify(userRepository).findById(BOOKER.getId());
        Mockito.verify(itemRepository).findById(ITEM_1.getId());
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

        Mockito.when(bookingRepository.findByIdForUpdate(Mockito.anyLong())).thenReturn(Optional.of(booking));
        Mockito.when(bookingRepository.save(Mockito.any())).thenReturn(booking);

        final BookingResponseDto responseDto = bookingService.approve(booking.getId(), true, OWNER.getId());

        Assertions.assertThat(responseDto.getId()).isEqualTo(booking.getId());
        Assertions.assertThat(responseDto.getStatus()).isEqualTo(booking.getStatus());
        Assertions.assertThat(responseDto.getBooker().getId()).isEqualTo(BOOKER.getId());
        Assertions.assertThat(responseDto.getItem().getId()).isEqualTo(ITEM_1.getId());
        Assertions.assertThat(responseDto.getItem().getName()).isEqualTo(ITEM_1.getName());

        Mockito.verify(bookingRepository).findByIdForUpdate(booking.getId());
        Mockito.verify(userRepository).findById(OWNER.getId());
        Mockito.verify(bookingRepository).save(Mockito.any(Booking.class));
        Mockito.verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository);
    }

    @Test
    void approveTest_ByNotOwner() {

        final Booking booking = BOOKING_1.toBuilder()
                .status(Status.WAITING)
                .build();

        Mockito.when(bookingRepository.findByIdForUpdate(Mockito.anyLong())).thenReturn(Optional.of(booking));
        Mockito.when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(BOOKER));

        Assertions.assertThatThrownBy(() -> bookingService.approve(booking.getId(), true, BOOKER.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Подтверждение доступно только для владельца вещи");

        Mockito.verify(bookingRepository).findByIdForUpdate(booking.getId());
        Mockito.verify(userRepository).findById(BOOKER.getId());
        Mockito.verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository);
    }

    @Test
    void approveTest_ForNotWaitingBooking() {

        final Booking booking = BOOKING_1.toBuilder()
                .status(Status.APPROVED)
                .build();

        Mockito.when(bookingRepository.findByIdForUpdate(Mockito.anyLong())).thenReturn(Optional.of(booking));

        Assertions.assertThatThrownBy(() -> bookingService.approve(booking.getId(), true, OWNER.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Вещь не ожидает подтверждения");

        Mockito.verify(bookingRepository).findByIdForUpdate(booking.getId());
        Mockito.verify(userRepository).findById(OWNER.getId());
        Mockito.verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository);
    }
}
