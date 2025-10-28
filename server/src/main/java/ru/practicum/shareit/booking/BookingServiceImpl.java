package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final String USER_NOT_FOUND = "Пользователь не найден";
    private static final String ITEM_NOT_FOUND = "Вещь не найдена";
    private static final String BOOKING_NOT_FOUND = "Бронирование не найдено";

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDto getById(final Long bookingId, final Long userId) {
        final Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(BOOKING_NOT_FOUND));
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        if (!(Objects.equals(booking.getUser().getId(), userId) || Objects.equals(booking.getItem().getOwner().getId(), userId))) {
            throw new NotFoundException("Не найдено подходящих бронирований для пользователя " + userId);
        }
        return BookingMapper.toDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getAllByState(final RequestBookingStatus requestBookingStatus, final Long userId,
                                                  final int from, final int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
        final Pageable pageable = PageRequest.of(from / size, size);

        return switch (requestBookingStatus) {
            case ALL -> bookingRepository.findAllByUserIdOrderByStartDesc(userId, pageable).stream()
                    .map(BookingMapper::toDto)
                    .collect(Collectors.toList());
            case PAST -> bookingRepository
                    .findAllByUserIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now(), pageable).stream()
                    .map(BookingMapper::toDto)
                    .collect(Collectors.toList());
            case FUTURE -> bookingRepository
                    .findAllByUserIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now(), pageable).stream()
                    .map(BookingMapper::toDto)
                    .collect(Collectors.toList());
            case CURRENT -> bookingRepository.findAllByUserIdAndStartBeforeAndEndAfterOrderByStartDesc(userId,
                            LocalDateTime.now(), LocalDateTime.now(), pageable).stream()
                    .map(BookingMapper::toDto)
                    .collect(Collectors.toList());
            case WAITING -> bookingRepository
                    .findAllByUserIdAndStatusOrderByStartDesc(userId, Status.WAITING, pageable).stream()
                    .map(BookingMapper::toDto)
                    .collect(Collectors.toList());
            case REJECTED -> bookingRepository
                    .findAllByUserIdAndStatusOrderByStartDesc(userId, Status.REJECTED, pageable).stream()
                    .map(BookingMapper::toDto)
                    .collect(Collectors.toList());
        };
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getAllByStateForOwner(final RequestBookingStatus requestBookingStatus, final Long userId,
                                                          final int from, final int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
        final Pageable pageable = PageRequest.of(from, size);

        return switch (requestBookingStatus) {
            case ALL -> bookingRepository.findAllByItemOwnerIdOrderByStartDesc(userId, pageable).stream()
                    .map(BookingMapper::toDto)
                    .collect(Collectors.toList());
            case PAST -> bookingRepository
                    .findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now(), pageable).stream()
                    .map(BookingMapper::toDto)
                    .collect(Collectors.toList());
            case FUTURE -> bookingRepository
                    .findAllByItemOwnerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now(), pageable).stream()
                    .map(BookingMapper::toDto)
                    .collect(Collectors.toList());
            case CURRENT -> bookingRepository
                    .findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId,
                            LocalDateTime.now(), LocalDateTime.now(), pageable).stream()
                    .map(BookingMapper::toDto)
                    .collect(Collectors.toList());
            case WAITING -> bookingRepository
                    .findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.WAITING, pageable).stream()
                    .map(BookingMapper::toDto)
                    .collect(Collectors.toList());
            case REJECTED -> bookingRepository
                    .findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.REJECTED, pageable).stream()
                    .map(BookingMapper::toDto)
                    .collect(Collectors.toList());
        };
    }

    @Override
    @Transactional
    public BookingResponseDto create(final BookingRequestDto bookingRequestDto, final Long userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
        final Item item = itemRepository.findById(bookingRequestDto.getItemId())
                .orElseThrow(() -> new NotFoundException(ITEM_NOT_FOUND));

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь не доступна для бронирования");
        }

        if (userId.equals(item.getOwner().getId())) {
            throw new NotFoundException("Владелец не может бронировать свою вещь");
        }

        final Booking booking = BookingMapper.fromDto(bookingRequestDto);
        booking.setUser(user);
        booking.setItem(item);
        booking.setStatus(Status.WAITING);

        return BookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingResponseDto approve(final Long bookingId, final boolean approved, final Long userId) {
        final Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(BOOKING_NOT_FOUND));
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
        if (!Objects.equals(booking.getItem().getOwner().getId(), userId)) {
            throw new NotFoundException("Подтверждение доступно только для владельца вещи");
        }

        if (booking.getStatus() != Status.WAITING) {
            throw new ValidationException("Вещь не ожидает подтверждения");
        }

        booking.setStatus(approved ? Status.APPROVED : Status.REJECTED);
        return BookingMapper.toDto(bookingRepository.save(booking));
    }
}
