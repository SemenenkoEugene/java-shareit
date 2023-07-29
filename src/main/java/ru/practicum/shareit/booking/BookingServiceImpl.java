package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.RequestBookingStatus;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.service.ServiceUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ServiceUtil serviceUtil;

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDto getById(Long bookingId, Long userId) {
        var booking = serviceUtil.getBookingOrThrowNotFound(bookingId);
        serviceUtil.getUserOrThrowNotFound(userId);
//
        if (!(Objects.equals(booking.getUser().getId(), userId) || Objects.equals(booking.getItem().getOwner().getId(), userId))) {
            throw new BookingNotFoundException("Не найдено подходящих бронирований для пользователя " + userId);
        }
        return BookingMapper.toDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getAllByState(RequestBookingStatus requestBookingStatus, Long userId) {
        serviceUtil.getUserOrThrowNotFound(userId);
        switch (requestBookingStatus) {
            case ALL:
                return bookingRepository.findAllByUserIdOrderByStartDesc(userId).stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case PAST:
                return bookingRepository.findAllByUserIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now()).stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case FUTURE:
                return bookingRepository.findAllByUserIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now()).stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case CURRENT:
                return bookingRepository.findAllByUserIdAndStartBeforeAndEndAfterOrderByStartDesc(userId,
                                LocalDateTime.now(), LocalDateTime.now()).stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case WAITING:
                return bookingRepository.findAllByUserIdAndStatusOrderByStartDesc(userId, Status.WAITING).stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case REJECTED:
                return bookingRepository.findAllByUserIdAndStatusOrderByStartDesc(userId, Status.REJECTED).stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            default:
                return new ArrayList<>();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getAllByStateForOwner(RequestBookingStatus requestBookingStatus, Long userId) {
        serviceUtil.getUserOrThrowNotFound(userId);

        switch (requestBookingStatus) {
            case ALL:
                return bookingRepository.findAllByItemOwnerIdOrderByStartDesc(userId).stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case PAST:
                return bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now()).stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case FUTURE:
                return bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now()).stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case CURRENT:
                return bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId,
                                LocalDateTime.now(), LocalDateTime.now()).stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case WAITING:
                return bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.WAITING).stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case REJECTED:
                return bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.REJECTED).stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            default:
                return new ArrayList<>();
        }
    }

    @Override
    @Transactional
    public BookingResponseDto create(BookingRequestDto bookingRequestDto, Long userId) {
        var user = serviceUtil.getUserOrThrowNotFound(userId);
        var item = serviceUtil.getItemOrThrowNotFound(bookingRequestDto.getItemId());

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь не доступна для бронирования");
        }

        if (userId.equals(item.getOwner().getId())) {
            throw new BookingNotFoundException("Владелец не может бронировать свою вещь");
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
        var booking = serviceUtil.getBookingOrThrowNotFound(bookingId);
        serviceUtil.getUserOrThrowNotFound(userId);

        if (!Objects.equals(booking.getItem().getOwner().getId(), userId)) {
            throw new BookingNotFoundException("Подтверждение доступно только для владельца вещи");
        }

        if (booking.getStatus() != Status.WAITING) {
            throw new ValidationException("Вещь не ожидает подтверждения");
        }

        booking.setStatus(approved ? Status.APPROVED : Status.REJECTED);
        return BookingMapper.toDto(bookingRepository.save(booking));
    }
}
