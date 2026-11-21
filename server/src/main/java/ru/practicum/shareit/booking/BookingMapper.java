package ru.practicum.shareit.booking;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.generated.booking.dto.BookingResponse;
import ru.practicum.shareit.generated.booking.dto.BookingResponseItem;
import ru.practicum.shareit.generated.booking.dto.BookingResponseUser;
import ru.practicum.shareit.generated.booking.dto.BookingStatus;
import ru.practicum.shareit.generated.booking.dto.CreateBookingRequest;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookingMapper {
    public static BookingResponse toDto(final Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(BookingStatus.valueOf(booking.getStatus().name()))
                .booker(BookingResponseUser.builder()
                        .id(booking.getUser().getId())
                        .build())
                .item(BookingResponseItem.builder()
                        .id(booking.getItem().getId())
                        .name(booking.getItem().getName())
                        .build())
                .build();
    }

    public static Booking fromDto(final CreateBookingRequest bookingRequestDto) {
        final Booking booking = new Booking();
        booking.setStart(bookingRequestDto.start());
        booking.setEnd(bookingRequestDto.end());
        return booking;
    }
}
