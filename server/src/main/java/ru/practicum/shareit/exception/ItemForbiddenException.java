package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ItemForbiddenException extends RuntimeException {
    public ItemForbiddenException(String message) {
        super(message);
        log.error(message);
    }
}
