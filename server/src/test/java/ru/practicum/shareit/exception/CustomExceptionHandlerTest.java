package ru.practicum.shareit.exception;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CustomExceptionHandlerTest {

    private CustomExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CustomExceptionHandler();
    }

    @Test
    void shouldHandleValidationException() {
        final ValidationException exception = new ValidationException("Validation failed");
        final CustomExceptionHandler.ResponseError response = handler.validationHandle(exception);

        Assertions.assertThat(response.getMessage()).isEqualTo("HttpStatus.BAD_REQUEST");
        Assertions.assertThat(response.getError()).isEqualTo("Validation failed");
        Assertions.assertThat(response.getLocalDateTime()).isNotNull();
    }

    @Test
    void shouldHandleNotFoundException() {
        final NotFoundException exception = new NotFoundException("Entity not found");
        final CustomExceptionHandler.ResponseError response = handler.notFoundHandle(exception);

        Assertions.assertThat(response.getMessage()).isEqualTo("HttpStatus.NOT_FOUND");
        Assertions.assertThat(response.getError()).isEqualTo("Entity not found");
    }

    @Test
    void shouldHandleUserAlreadyExistsException() {
        final UserAlreadyExistsException exception =
                new UserAlreadyExistsException("User already exists", new RuntimeException("duplicate"));
        final CustomExceptionHandler.ResponseError response = handler.userAlreadyExistsHandler(exception);

        Assertions.assertThat(response.getMessage()).isEqualTo("HttpStatus.CONFLICT");
        Assertions.assertThat(response.getError()).isEqualTo("User already exists");
    }

    @Test
    void shouldHandleUnsupportedStatusException() {
        final UnsupportedStatusException exception = new UnsupportedStatusException("Unsupported status");
        final CustomExceptionHandler.ResponseError response = handler.unsupportedStatusHandler(exception);

        Assertions.assertThat(response.getMessage()).isEqualTo("HttpStatus.BAD_REQUEST");
        Assertions.assertThat(response.getError()).isEqualTo("Unsupported status");
    }

    @Test
    void shouldHandleItemForbiddenException() {
        final ItemForbiddenException exception = new ItemForbiddenException("Access denied");
        final CustomExceptionHandler.ResponseError response = handler.itemForbiddenStatusHandler(exception);

        Assertions.assertThat(response.getMessage()).isEqualTo("HttpStatus.FORBIDDEN");
        Assertions.assertThat(response.getError()).isEqualTo("Access denied");
    }

    @Test
    void shouldHandleItemAlreadyExistsException() {
        final ItemAlreadyExistsException exception = new ItemAlreadyExistsException("Item already exists", new RuntimeException("duplicate"));
        final CustomExceptionHandler.ResponseError response = handler.itemAlreadyExistsHandler(exception);

        Assertions.assertThat(response.getMessage()).isEqualTo("HttpStatus.CONFLICT");
        Assertions.assertThat(response.getError()).isEqualTo("Item already exists");
    }
}
