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
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    public BookingResponseDto getById(Long bookingId, Long userId) {
        var booking = bookingRepository.findById(bookingId)
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
    public List<BookingResponseDto> getAllByState(RequestBookingStatus requestBookingStatus,
                                                  Long userId,
                                                  int from,
                                                  int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
        Pageable pageable = PageRequest.of(from / size, size);

        switch (requestBookingStatus) {
            case ALL:
                return bookingRepository.findAllByUserIdOrderByStartDesc(userId, pageable).stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case PAST:
                return bookingRepository
                        .findAllByUserIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case FUTURE:
                return bookingRepository
                        .findAllByUserIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case CURRENT:
                return bookingRepository.findAllByUserIdAndStartBeforeAndEndAfterOrderByStartDesc(userId,
                                LocalDateTime.now(), LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case WAITING:
                return bookingRepository
                        .findAllByUserIdAndStatusOrderByStartDesc(userId, Status.WAITING, pageable).stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case REJECTED:
                return bookingRepository
                        .findAllByUserIdAndStatusOrderByStartDesc(userId, Status.REJECTED, pageable).stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            default:
                return new ArrayList<>();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getAllByStateForOwner(RequestBookingStatus requestBookingStatus,
                                                          Long userId,
                                                          int from,
                                                          int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
        Pageable pageable = PageRequest.of(from, size);

        switch (requestBookingStatus) {
            case ALL:
                return bookingRepository.findAllByItemOwnerIdOrderByStartDesc(userId, pageable).stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case PAST:
                return bookingRepository
                        .findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case FUTURE:
                return bookingRepository
                        .findAllByItemOwnerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case CURRENT:
                return bookingRepository
                        .findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId,
                                LocalDateTime.now(), LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case WAITING:
                return bookingRepository
                        .findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.WAITING, pageable).stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case REJECTED:
                return bookingRepository
                        .findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.REJECTED, pageable).stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            default:
                return new ArrayList<>();
        }
    }

    @Override
    @Transactional
    public BookingResponseDto create(BookingRequestDto bookingRequestDto, Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
        var item = itemRepository.findById(bookingRequestDto.getItemId())
                .orElseThrow(() -> new NotFoundException(ITEM_NOT_FOUND));

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь не доступна для бронирования");
        }

        if (userId.equals(item.getOwner().getId())) {
            throw new NotFoundException("Владелец не может бронировать свою вещь");
        }
        var booking = BookingMapper.fromDto(bookingRequestDto);
        booking.setUser(user);
        booking.setItem(item);
        booking.setStatus(Status.WAITING);

        return BookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingResponseDto approve(Long bookingId, boolean approved, Long userId) {
        var booking = bookingRepository.findById(bookingId)
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
