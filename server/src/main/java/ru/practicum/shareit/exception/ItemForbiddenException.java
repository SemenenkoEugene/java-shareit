package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;

import java.io.Serial;

@Slf4j
public class ItemForbiddenException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ItemForbiddenException(final String message) {
        super(message);
        log.error(message);
    }
}
