package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.RequestBookingStatus;
import ru.practicum.shareit.exception.UnsupportedStatusException;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private static final String HEADER = "X-Sharer-User-Id";
    private final BookingService bookingService;

    @PostMapping
    public BookingResponseDto create(@RequestBody final BookingRequestDto bookingRequestDto,
                                     @RequestHeader(HEADER) final Long userId) {
        log.info("Получен POST-запрос к эндпоинту: '/bookings' " +
                 "на создание бронирования от пользователя с ID={}", userId);
        return bookingService.create(bookingRequestDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto update(@PathVariable final Long bookingId,
                                     @RequestHeader(HEADER) final Long userId,
                                     @RequestParam final Boolean approved) {
        log.info("Получен PATCH-запрос к эндпоинту: '/bookings' на обновление статуса бронирования с ID={}", bookingId);
        return bookingService.approve(bookingId, approved, userId);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getBookingById(@PathVariable final Long bookingId,
                                             @RequestHeader(HEADER) final Long userId) {
        log.info("Получен GET-запрос к эндпоинту: '/bookings' на получение бронирования с ID={}", bookingId);
        return bookingService.getById(bookingId, userId);
    }

    @GetMapping
    public List<BookingResponseDto> getAllByState(@RequestParam(defaultValue = "ALL") final String state,
                                                  @RequestParam(defaultValue = "0") final int from,
                                                  @RequestParam(defaultValue = "20") final int size,
                                                  @RequestHeader(HEADER) final Long userId) {
        log.info("Получен GET-запрос к эндпоинту: '/bookings' на получение " +
                 "списка всех бронирований пользователя с ID={} с параметром STATE={}", userId, state);
        final RequestBookingStatus status = RequestBookingStatus.state(state)
                .orElseThrow(() -> new UnsupportedStatusException("Unknown state: " + state));
        return bookingService.getAllByState(status, userId, from, size);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getBookingsOwner(@RequestParam(required = false, defaultValue = "ALL") final String state,
                                                     @RequestParam(defaultValue = "0") final int from,
                                                     @RequestParam(defaultValue = "20") final int size,
                                                     @RequestHeader(HEADER) final Long userId) {
        log.info("Получен GET-запрос к эндпоинту: '/bookings/owner' на получение " +
                 "списка всех бронирований вещей пользователя с ID={} с параметром STATE={}", userId, state);
        final RequestBookingStatus status = RequestBookingStatus.state(state)
                .orElseThrow(() -> new UnsupportedStatusException("Unknown state: " + state));
        return bookingService.getAllByStateForOwner(status, userId, from, size);
    }
}
