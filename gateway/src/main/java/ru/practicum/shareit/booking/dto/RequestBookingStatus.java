package ru.practicum.shareit.booking.dto;

import java.util.Optional;

public enum RequestBookingStatus {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED;

    public static Optional<RequestBookingStatus> from(String state) {
        for (RequestBookingStatus value : values()) {
            if (value.name().equalsIgnoreCase(state)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }
}
