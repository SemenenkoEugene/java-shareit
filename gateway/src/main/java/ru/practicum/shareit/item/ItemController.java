package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;


@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    public static final String X_SHARER_USER_ID = "X-Sharer-User-Id";
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(X_SHARER_USER_ID) final Long userId,
                                         @Valid @RequestBody final ItemDto itemDto) {
        log.debug("Получен POST-запрос к эндпоинту: '/items' на добавление вещи владельцем с ID={}", userId);
        return itemClient.create(userId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@RequestHeader(X_SHARER_USER_ID) final Long userId,
                                                @PathVariable final Long itemId,
                                                @Valid @RequestBody final CommentDto commentDto) {
        log.info("Получен POST-запрос к эндпоинту: '/items/comment' на добавление отзыва пользователем с ID={}", userId);
        return itemClient.createComment(userId, itemId, commentDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader(X_SHARER_USER_ID) final Long userId,
                                         @PathVariable final Long itemId,
                                         @RequestBody final ItemDto itemDto) {
        log.debug("Получен PATCH-запрос к эндпоинту: '/items' на обновление вещи с ID={}", itemId);
        return itemClient.update(userId, itemId, itemDto);
    }

    @DeleteMapping("/{itemId}")
    public void delete(@PathVariable final Long itemId) {
        log.debug("Получен DELETE-запрос к эндпоинту: '/items' на удаление вещи с ID={}", itemId);
        itemClient.delete(itemId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@RequestHeader(X_SHARER_USER_ID) final Long userId,
                                              @PathVariable final Long itemId) {
        log.info("Получен GET-запрос к эндпоинту: '/items' на получение вещи с ID={}", itemId);
        return itemClient.getById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getItemsByOwnerId(@RequestHeader(X_SHARER_USER_ID) final Long userId,
                                                    @RequestParam(required = false, defaultValue = "0") @Min(0) final int from,
                                                    @RequestParam(required = false, defaultValue = "20") @Min(1) final int size) {
        log.info("Получен GET-запрос к эндпоинту: '/items' на получение всех вещей владельца с ID={}", userId);
        return itemClient.getAllByOwnerId(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> getItemsBySearchQuery(@RequestHeader(X_SHARER_USER_ID) final Long userId,
                                                        @RequestParam(name = "text") final String text,
                                                        @RequestParam(required = false, defaultValue = "0") @Min(0) final int from,
                                                        @RequestParam(required = false, defaultValue = "20") @Min(1) final int size) {
        log.info("Получен GET-запрос к эндпоинту: '/items/search' на поиск вещи с текстом={}", text);
        return itemClient.getAllBySearchText(userId, text, from, size);
    }
}
