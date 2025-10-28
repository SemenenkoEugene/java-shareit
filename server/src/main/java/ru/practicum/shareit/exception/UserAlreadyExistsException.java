package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;

import java.io.Serial;

@Slf4j
public class UserAlreadyExistsException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public UserAlreadyExistsException(final String message, final Exception error) {
        super(message);
        log.error(message);
    }
}
