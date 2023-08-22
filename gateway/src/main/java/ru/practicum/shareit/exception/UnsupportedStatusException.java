package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UnsupportedStatusException extends RuntimeException {

    public UnsupportedStatusException(String message) {
        super(message);
        log.error(message);
    }
}
