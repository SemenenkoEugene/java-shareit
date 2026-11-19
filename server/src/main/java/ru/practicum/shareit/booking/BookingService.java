package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.RequestBookingStatus;
import ru.practicum.shareit.generated.booking.dto.BookingResponse;
import ru.practicum.shareit.generated.booking.dto.CreateBookingRequest;

import java.util.List;

public interface BookingService {

    BookingResponse getById(Long bookingId, Long userId);

    List<BookingResponse> getAllByState(RequestBookingStatus requestBookingStatus, Long userId, int from, int size);

    List<BookingResponse> getAllByStateForOwner(RequestBookingStatus requestBookingStatus, Long userId, int from, int size);

    BookingResponse create(CreateBookingRequest bookingRequestDto, Long userId);

    BookingResponse approve(Long bookingId, boolean approved, Long userId);
}
