package ru.practicum.shareit.booking;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Бронирования", description = "Операции по созданию и управлению бронированиями вещей")
public class BookingController {

    private static final String HEADER = "X-Sharer-User-Id";
    private final BookingService bookingService;

    @Operation(
            summary = "Создать бронирование",
            description = "Создаёт новое бронирование вещи для указанного пользователя",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Бронирование успешно создано",
                            content = @Content(schema = @Schema(implementation = BookingResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
                    @ApiResponse(responseCode = "404", description = "Пользователь или вещь не найдены")
            }
    )
    @PostMapping
    public BookingResponseDto create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для создания бронирования",
                    required = true,
                    content = @Content(schema = @Schema(implementation = BookingRequestDto.class))
            )
            @RequestBody final BookingRequestDto bookingRequestDto,
            @Parameter(description = "ID пользователя, совершающего бронирование", required = true)
            @RequestHeader(HEADER) final Long userId
    ) {
        log.info("POST /bookings — создание бронирования от пользователя ID={}", userId);
        return bookingService.create(bookingRequestDto, userId);
    }

    @Operation(
            summary = "Подтвердить или отклонить бронирование",
            description = "Позволяет владельцу вещи изменить статус бронирования",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Статус бронирования изменён"),
                    @ApiResponse(responseCode = "404", description = "Бронирование не найдено")
            }
    )
    @PatchMapping("/{bookingId}")
    public BookingResponseDto update(
            @Parameter(description = "ID бронирования", required = true)
            @PathVariable final Long bookingId,
            @Parameter(description = "ID владельца вещи", required = true)
            @RequestHeader(HEADER) final Long userId,
            @Parameter(description = "true — одобрить, false — отклонить", required = true)
            @RequestParam final Boolean approved
    ) {
        log.info("PATCH /bookings — обновление статуса бронирования ID={}", bookingId);
        return bookingService.approve(bookingId, approved, userId);
    }

    @Operation(
            summary = "Получить бронирование по ID",
            description = "Возвращает данные конкретного бронирования по его идентификатору",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Бронирование найдено"),
                    @ApiResponse(responseCode = "404", description = "Бронирование не найдено")
            }
    )
    @GetMapping("/{bookingId}")
    public BookingResponseDto getBookingById(
            @Parameter(description = "ID бронирования", required = true)
            @PathVariable final Long bookingId,
            @Parameter(description = "ID пользователя (автор или владелец)", required = true)
            @RequestHeader(HEADER) final Long userId
    ) {
        log.info("GET /bookings/{} — получение бронирования", bookingId);
        return bookingService.getById(bookingId, userId);
    }

    @Operation(
            summary = "Получить список бронирований пользователя",
            description = "Возвращает список всех бронирований, сделанных пользователем",
            responses = @ApiResponse(responseCode = "200", description = "Список успешно получен")
    )
    @GetMapping
    public List<BookingResponseDto> getAllByState(
            @Parameter(description = "Состояние бронирования (ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED)")
            @RequestParam(defaultValue = "ALL") final String state,
            @Parameter(description = "Номер первой записи (пагинация)", example = "0")
            @RequestParam(defaultValue = "0") final int from,
            @Parameter(description = "Количество записей на странице", example = "20")
            @RequestParam(defaultValue = "20") final int size,
            @Parameter(description = "ID пользователя", required = true)
            @RequestHeader(HEADER) final Long userId
    ) {
        log.info("GET /bookings — получение списка бронирований пользователя ID={}, state={}", userId, state);
        final RequestBookingStatus status = RequestBookingStatus.state(state)
                .orElseThrow(() -> new UnsupportedStatusException("Unknown state: " + state));
        return bookingService.getAllByState(status, userId, from, size);
    }

    @Operation(
            summary = "Получить бронирования вещей владельца",
            description = "Возвращает все бронирования вещей, принадлежащих пользователю",
            responses = @ApiResponse(responseCode = "200", description = "Список успешно получен")
    )
    @GetMapping("/owner")
    public List<BookingResponseDto> getBookingsOwner(
            @Parameter(description = "Состояние бронирования (ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED)")
            @RequestParam(required = false, defaultValue = "ALL") final String state,
            @Parameter(description = "Номер первой записи (пагинация)", example = "0")
            @RequestParam(defaultValue = "0") final int from,
            @Parameter(description = "Количество записей на странице", example = "20")
            @RequestParam(defaultValue = "20") final int size,
            @Parameter(description = "ID владельца", required = true)
            @RequestHeader(HEADER) final Long userId
    ) {
        log.info("GET /bookings/owner — получение бронирований вещей владельца ID={}, state={}", userId, state);
        final RequestBookingStatus status = RequestBookingStatus.state(state)
                .orElseThrow(() -> new UnsupportedStatusException("Unknown state: " + state));
        return bookingService.getAllByStateForOwner(status, userId, from, size);
    }
}
