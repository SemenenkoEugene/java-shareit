package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;

import java.io.Serial;

@Slf4j
public class ItemAlreadyExistsException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ItemAlreadyExistsException(final String message, final Throwable cause) {
        super(message, cause);
        log.error(message, cause);
    }
}
