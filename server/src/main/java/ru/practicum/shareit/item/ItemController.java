package ru.practicum.shareit.item;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

@RestController
@RequestMapping("/items")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Вещи", description = "Операции с вещами и комментариями пользователей")
public class ItemController {

    private static final String HEADER = "X-Sharer-User-Id";
    private final ItemService itemService;

    @Operation(
            summary = "Создать новую вещь",
            description = "Добавляет новую вещь для аренды от имени владельца",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Вещь успешно добавлена",
                            content = @Content(schema = @Schema(implementation = ItemDto.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
            }
    )
    @PostMapping
    public ItemDto create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные новой вещи",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ItemDto.class))
            )
            @RequestBody final ItemDto itemDto,
            @Parameter(description = "ID владельца вещи", required = true)
            @RequestHeader(HEADER) final Long userId
    ) {
        log.debug("POST /items — добавление вещи владельцем ID={}", userId);
        return itemService.create(itemDto, userId);
    }

    @Operation(
            summary = "Добавить комментарий к вещи",
            description = "Позволяет пользователю оставить отзыв о вещи после аренды",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Комментарий добавлен",
                            content = @Content(schema = @Schema(implementation = CommentDto.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректные данные или пользователь не арендовал вещь"),
                    @ApiResponse(responseCode = "404", description = "Вещь не найдена")
            }
    )
    @PostMapping("{itemId}/comment")
    public CommentDto createComment(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Текст комментария",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CommentDto.class))
            )
            @RequestBody final CommentDto commentDto,
            @Parameter(description = "ID пользователя, оставляющего комментарий", required = true)
            @RequestHeader(HEADER) final Long userId,
            @Parameter(description = "ID вещи", required = true)
            @PathVariable final Long itemId
    ) {
        log.info("POST /items/{}/comment — отзыв от пользователя ID={}", itemId, userId);
        return itemService.createComment(commentDto, userId, itemId);
    }

    @Operation(
            summary = "Обновить данные вещи",
            description = "Позволяет владельцу изменить описание, доступность или название вещи",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Информация о вещи обновлена",
                            content = @Content(schema = @Schema(implementation = ItemDto.class))),
                    @ApiResponse(responseCode = "404", description = "Вещь не найдена")
            }
    )
    @PatchMapping("/{itemId}")
    public ItemDto update(
            @Parameter(description = "ID вещи", required = true)
            @PathVariable final Long itemId,
            @Parameter(description = "ID владельца вещи", required = true)
            @RequestHeader(HEADER) final Long userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Обновлённые данные вещи",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ItemDto.class))
            )
            @RequestBody final ItemDto itemDto
    ) {
        log.debug("PATCH /items/{} — обновление вещи", itemId);
        return itemService.update(itemDto, itemId, userId);
    }

    @Operation(
            summary = "Удалить вещь",
            description = "Удаляет вещь по её идентификатору",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Вещь удалена"),
                    @ApiResponse(responseCode = "404", description = "Вещь не найдена")
            }
    )
    @DeleteMapping("/{itemId}")
    public void delete(
            @Parameter(description = "ID вещи", required = true)
            @PathVariable final Long itemId
    ) {
        log.debug("DELETE /items/{} — удаление вещи", itemId);
        itemService.delete(itemId);
    }

    @Operation(
            summary = "Получить вещь по ID",
            description = "Возвращает данные вещи, включая комментарии и информацию о бронировании",
            responses = @ApiResponse(responseCode = "200", description = "Вещь найдена",
                    content = @Content(schema = @Schema(implementation = ItemDto.class)))
    )
    @GetMapping("/{itemId}")
    public ItemDto getItemById(
            @Parameter(description = "ID пользователя, запрашивающего вещь", required = true)
            @RequestHeader(HEADER) final Long userId,
            @Parameter(description = "ID вещи", required = true)
            @PathVariable final Long itemId
    ) {
        log.info("GET /items/{} — получение вещи пользователем ID={}", itemId, userId);
        return itemService.getItemById(userId, itemId);
    }

    @Operation(
            summary = "Получить все вещи пользователя",
            description = "Возвращает список всех вещей, принадлежащих владельцу",
            responses = @ApiResponse(responseCode = "200", description = "Список вещей получен")
    )
    @GetMapping
    public List<ItemDto> getItemsByOwnerId(
            @Parameter(description = "ID владельца вещей", required = true)
            @RequestHeader(HEADER) final Long userId,
            @Parameter(description = "Номер первой записи (пагинация)", example = "0")
            @RequestParam(required = false, defaultValue = "0") final int from,
            @Parameter(description = "Количество записей на странице", example = "20")
            @RequestParam(required = false, defaultValue = "20") final int size
    ) {
        log.info("GET /items — получение всех вещей владельца ID={}", userId);
        return itemService.getItemsByOwnerId(userId, from, size);
    }

    @Operation(
            summary = "Поиск вещей по тексту",
            description = "Возвращает список вещей, соответствующих поисковому запросу в названии или описании",
            responses = @ApiResponse(responseCode = "200", description = "Результаты поиска получены")
    )
    @GetMapping("/search")
    public List<ItemDto> getItemsBySearchQuery(
            @Parameter(description = "Текст для поиска", required = true)
            @RequestParam(name = "text") final String searchText,
            @Parameter(description = "Номер первой записи (пагинация)", example = "0")
            @RequestParam(required = false, defaultValue = "0") final int from,
            @Parameter(description = "Количество записей на странице", example = "20")
            @RequestParam(required = false, defaultValue = "20") final int size
    ) {
        log.info("GET /items/search — поиск вещи по тексту '{}'", searchText);
        return itemService.getItemsBySearchQuery(searchText, from, size);
    }
}
