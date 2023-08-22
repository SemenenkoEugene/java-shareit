package ru.practicum.shareit.booking.dto;

import java.util.Optional;

public enum RequestBookingStatus {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED;

    public static Optional<RequestBookingStatus> from(String stringState) {
        for (RequestBookingStatus state : values()) {
            if (state.name().equalsIgnoreCase(stringState)) {
                return Optional.of(state);
            }
        }
        return Optional.empty();
    }
}
