package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.RequestBookingStatus;

import java.util.List;

public interface BookingService {

    BookingResponseDto getById(Long bookingId, Long userId);

    List<BookingResponseDto> getAllByState(RequestBookingStatus requestBookingStatus, Long userId);

    List<BookingResponseDto> getAllByStateForOwner(RequestBookingStatus requestBookingStatus, Long userId);

    BookingResponseDto create(BookingRequestDto bookingRequestDto, Long userId);

    BookingResponseDto approve(Long bookingId, boolean approved, Long userId);
}
