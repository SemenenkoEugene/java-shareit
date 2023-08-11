package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.RequestBookingStatus;
import ru.practicum.shareit.exception.UnsupportedStatusException;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@Validated
@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private static final String HEADER = "X-Sharer-User-Id";
    private final BookingService bookingService;

    @PostMapping
    public BookingResponseDto create(@Valid @RequestBody BookingRequestDto bookingRequestDto,
                                     @RequestHeader(HEADER) Long userId) {
        log.info("Получен POST-запрос к эндпоинту: '/bookings' " +
                 "на создание бронирования от пользователя с ID={}", userId);
        return bookingService.create(bookingRequestDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto update(@PathVariable Long bookingId,
                                     @RequestHeader(HEADER) Long userId,
                                     @RequestParam Boolean approved) {
        log.info("Получен PATCH-запрос к эндпоинту: '/bookings' на обновление статуса бронирования с ID={}", bookingId);
        return bookingService.approve(bookingId, approved, userId);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getBookingById(@PathVariable Long bookingId,
                                             @RequestHeader(HEADER) Long userId) {
        log.info("Получен GET-запрос к эндпоинту: '/bookings' на получение бронирования с ID={}", bookingId);
        return bookingService.getById(bookingId, userId);
    }

    @GetMapping
    public List<BookingResponseDto> getAllByState(@RequestParam(defaultValue = "ALL") String state,
                                                  @RequestParam(defaultValue = "0") @Min(0) int from,
                                                  @RequestParam(defaultValue = "20") @Min(1) int size,
                                                  @RequestHeader(HEADER) Long userId) {
        log.info("Получен GET-запрос к эндпоинту: '/bookings' на получение " +
                 "списка всех бронирований пользователя с ID={} с параметром STATE={}", userId, state);
        var status = RequestBookingStatus.state(state)
                .orElseThrow(() -> new UnsupportedStatusException("Unknown state: " + state));
        return bookingService.getAllByState(status, userId, from, size);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getBookingsOwner(@RequestParam(required = false, defaultValue = "ALL") String state,
                                                     @RequestParam(defaultValue = "0") @Min(0) int from,
                                                     @RequestParam(defaultValue = "20") @Min(1) int size,
                                                     @RequestHeader(HEADER) Long userId) {
        log.info("Получен GET-запрос к эндпоинту: '/bookings/owner' на получение " +
                 "списка всех бронирований вещей пользователя с ID={} с параметром STATE={}", userId, state);
        var status = RequestBookingStatus.state(state)
                .orElseThrow(() -> new UnsupportedStatusException("Unknown state: " + state));
        return bookingService.getAllByStateForOwner(status, userId, from, size);
    }
}
