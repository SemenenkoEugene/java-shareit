package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.RequestBookingStatus;


@RestController
@Validated
@Slf4j
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    public static final String X_SHARER_USER_ID = "X-Sharer-User-Id";
    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(X_SHARER_USER_ID) final Long userId,
                                         @Valid @RequestBody final BookingRequestDto bookingRequestDto) {
        log.info("Получен POST-запрос к эндпоинту: '/bookings' " +
                 "на создание бронирования от пользователя с ID={}", userId);
        return bookingClient.create(userId, bookingRequestDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> update(@RequestHeader(X_SHARER_USER_ID) final Long userId,
                                         @PathVariable final Long bookingId,
                                         @RequestParam final boolean approved) {
        log.info("Получен PATCH-запрос к эндпоинту: '/bookings' на обновление статуса бронирования с ID={}", bookingId);
        return bookingClient.approve(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(@RequestHeader(X_SHARER_USER_ID) final Long userId,
                                                 @PathVariable final Long bookingId) {
        log.info("Получен GET-запрос к эндпоинту: '/bookings' на получение бронирования с ID={}", bookingId);
        return bookingClient.getById(userId, bookingId);
    }

    @GetMapping()
    public ResponseEntity<Object> getAllByState(@RequestHeader(X_SHARER_USER_ID) final Long userId,
                                                @RequestParam(defaultValue = "ALL") final String state,
                                                @Valid @RequestParam(name = "from", defaultValue = "0") @Min(0) final int from,
                                                @Valid @RequestParam(name = "size", defaultValue = "20") @Min(1) final int size) {
        final RequestBookingStatus status = RequestBookingStatus.from(state)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + state));
        log.info("Получен GET-запрос к эндпоинту: '/bookings' на получение " +
                 "списка всех бронирований пользователя с ID={} с параметром STATE={}", userId, status);
        return bookingClient.getAllByState(userId, status, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingsOwner(@RequestHeader(X_SHARER_USER_ID) final Long userId,
                                                   @RequestParam(required = false, defaultValue = "ALL") final String state,
                                                   @Valid @RequestParam(value = "from", defaultValue = "0") @Min(0) final int from,
                                                   @Valid @RequestParam(value = "size", defaultValue = "20") @Min(1) final int size) {
        final RequestBookingStatus status = RequestBookingStatus.from(state)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + state));
        log.info("Получен GET-запрос к эндпоинту: '/bookings/owner' на получение " +
                 "списка всех бронирований вещей пользователя с ID={} с параметром STATE={}", userId, status);
        return bookingClient.getAllByStateForOwner(userId, status, from, size);
    }
}
