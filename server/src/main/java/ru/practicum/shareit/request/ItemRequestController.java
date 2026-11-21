package ru.practicum.shareit.request;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.generated.request.dto.CreateItemRequestRequest;
import ru.practicum.shareit.generated.request.dto.ItemRequestCreateResponse;
import ru.practicum.shareit.generated.request.dto.ItemRequestResponse;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Tag(name = "Запросы вещей", description = "Операции для создания и получения запросов на вещи")
public class ItemRequestController {

    private static final String HEADER = "X-Sharer-User-Id";
    private final ItemRequestService itemRequestService;

    @Operation(
            summary = "Создать запрос на вещь",
            description = "Позволяет пользователю создать новый запрос на добавление вещи, которой пока нет в системе",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Запрос успешно создан",
                            content = @Content(schema = @Schema(implementation = ItemRequestCreateResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
            }
    )
    @PostMapping
    public ItemRequestCreateResponse create(
            @Parameter(description = "ID пользователя, создающего запрос", required = true)
            @RequestHeader(HEADER) final Long userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные нового запроса на вещь",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateItemRequestRequest.class))
            )
            @RequestBody final CreateItemRequestRequest itemRequestCreateDto
    ) {
        return itemRequestService.create(itemRequestCreateDto, userId);
    }

    @Operation(
            summary = "Получить список собственных запросов пользователя",
            description = "Возвращает список всех запросов, созданных текущим пользователем",
            responses = @ApiResponse(responseCode = "200", description = "Список запросов успешно получен")
    )
    @GetMapping
    public List<ItemRequestResponse> getAllByOwnerId(
            @Parameter(description = "ID пользователя, чьи запросы нужно получить", required = true)
            @RequestHeader(HEADER) final Long userId,
            @Parameter(description = "Номер первой записи (пагинация)", example = "0")
            @RequestParam(required = false, defaultValue = "0") final int from,
            @Parameter(description = "Количество записей на странице", example = "20")
            @RequestParam(required = false, defaultValue = "20") final int size
    ) {
        return itemRequestService.getAllByRequestorId(userId, from, size);
    }

    @Operation(
            summary = "Получить список всех запросов других пользователей",
            description = "Позволяет просмотреть запросы на вещи, созданные другими пользователями",
            responses = @ApiResponse(responseCode = "200", description = "Список запросов успешно получен")
    )
    @GetMapping("/all")
    public List<ItemRequestResponse> getAll(
            @Parameter(description = "ID пользователя, выполняющего запрос", required = true)
            @RequestHeader(HEADER) final Long userId,
            @Parameter(description = "Номер первой записи (пагинация)", example = "0")
            @RequestParam(required = false, defaultValue = "0") final int from,
            @Parameter(description = "Количество записей на странице", example = "20")
            @RequestParam(required = false, defaultValue = "20") final int size
    ) {
        return itemRequestService.getAll(userId, from, size);
    }

    @Operation(
            summary = "Получить запрос по ID",
            description = "Возвращает детальную информацию о запросе, включая вещи, созданные по нему",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Запрос найден",
                            content = @Content(schema = @Schema(implementation = ItemRequestResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Запрос не найден")
            }
    )
    @GetMapping("/{itemRequestId}")
    public ItemRequestResponse getById(
            @Parameter(description = "ID пользователя, выполняющего запрос", required = true)
            @RequestHeader(HEADER) final Long userId,
            @Parameter(description = "ID запроса на вещь", required = true)
            @PathVariable final Long itemRequestId
    ) {
        return itemRequestService.getById(userId, itemRequestId);
    }
}
