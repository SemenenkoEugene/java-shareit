package ru.practicum.shareit.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class CustomExceptionHandler {

    private static final String HTTP_STATUS_NOT_FOUND = "HttpStatus.NOT_FOUND";
    private static final String HTTP_STATUS_BAD_REQUEST = "HttpStatus.BAD_REQUEST";

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseError validationHandle(ValidationException e) {
        log.error(e.getMessage());
        return new ResponseError(HTTP_STATUS_BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseError itemNotFoundHandle(ItemNotFoundException e) {
        log.error(e.getMessage());
        return new ResponseError(HTTP_STATUS_NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseError userNotFoundHandle(UserNotFoundException e) {
        log.error(e.getMessage());
        return new ResponseError(HTTP_STATUS_NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseError userAlreadyExists(UserAlreadyExistsException e) {
        log.error(e.getMessage());
        return new ResponseError(HTTP_STATUS_BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseError bookingNotFoundHandler(BookingNotFoundException e) {
        log.error(e.getMessage());
        return new ResponseError(HTTP_STATUS_NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseError unsupportedStatusHandler(UnsupportedStatusException e) {
        log.error(e.getMessage());
        return new ResponseError(HTTP_STATUS_BAD_REQUEST, e.getMessage());
    }

    @Getter
    @RequiredArgsConstructor
    private static class ResponseError {
        private final String message;
        private final String error;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        private final LocalDateTime localDateTime = LocalDateTime.now();
    }
}
