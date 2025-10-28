package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;

import java.io.Serial;

@Slf4j
public class UnsupportedStatusException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public UnsupportedStatusException(final String message) {
        super(message);
        log.error(message);
    }
}
