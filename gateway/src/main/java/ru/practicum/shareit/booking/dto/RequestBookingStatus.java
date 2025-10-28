package ru.practicum.shareit.booking.dto;

import java.util.Arrays;
import java.util.Optional;

public enum RequestBookingStatus {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED;

    public static Optional<RequestBookingStatus> from(final String state) {
        return Arrays.stream(values())
                .filter(v -> v.name().equalsIgnoreCase(state))
                .findFirst();
    }
}
