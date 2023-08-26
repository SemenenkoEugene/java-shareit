package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

@RestController
@RequestMapping("/items")
@Slf4j
@RequiredArgsConstructor
public class ItemController {

    private static final String HEADER = "X-Sharer-User-Id";
    private final ItemService itemService;

    @PostMapping
    public ItemDto create(@RequestBody ItemDto itemDto,
                          @RequestHeader(HEADER) Long userId) {
        log.debug("Получен POST-запрос к эндпоинту: '/items' на добавление вещи владельцем с ID={}", userId);
        return itemService.create(itemDto, userId);
    }

    @PostMapping("{itemId}/comment")
    public CommentDto createComment(@RequestBody CommentDto commentDto,
                                    @RequestHeader(HEADER) Long userId,
                                    @PathVariable Long itemId) {
        log.info("Получен POST-запрос к эндпоинту: '/items/comment' на добавление отзыва пользователем с ID={}", userId);
        return itemService.createComment(commentDto, userId, itemId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@PathVariable Long itemId,
                          @RequestHeader(HEADER) Long userId,
                          @RequestBody ItemDto itemDto) {
        log.debug("Получен PATCH-запрос к эндпоинту: '/items' на обновление вещи с ID={}", itemId);
        return itemService.update(itemDto, itemId, userId);
    }

    @DeleteMapping("/{itemId}")
    public void delete(@PathVariable Long itemId) {
        log.debug("Получен DELETE-запрос к эндпоинту: '/items' на удаление вещи с ID={}", itemId);
        itemService.delete(itemId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@RequestHeader(HEADER) Long userId,
                               @PathVariable Long itemId) {
        log.info("Получен GET-запрос к эндпоинту: '/items' на получение вещи с ID={}", itemId);
        return itemService.getItemById(userId, itemId);
    }

    @GetMapping
    public List<ItemDto> getItemsByOwnerId(@RequestHeader(HEADER) Long userId,
                                           @RequestParam(required = false, defaultValue = "0") int from,
                                           @RequestParam(required = false, defaultValue = "20") int size) {
        log.info("Получен GET-запрос к эндпоинту: '/items' на получение всех вещей владельца с ID={}", userId);
        return itemService.getItemsByOwnerId(userId, from, size);
    }

    @GetMapping("/search")
    public List<ItemDto> getItemsBySearchQuery(@RequestParam(name = "text") String searchText,
                                               @RequestParam(required = false, defaultValue = "0") int from,
                                               @RequestParam(required = false, defaultValue = "20") int size) {
        log.info("Получен GET-запрос к эндпоинту: '/items/search' на поиск вещи с текстом={}", searchText);
        return itemService.getItemsBySearchQuery(searchText, from, size);
    }
}
