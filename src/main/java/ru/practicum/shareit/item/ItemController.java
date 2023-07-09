package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
@Slf4j
public class ItemController {

    private final ItemService itemService;
    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @ResponseBody
    @PostMapping
    public ItemDto create(@Valid @RequestBody ItemDto itemDto, @RequestHeader(HEADER) Long ownerId) {
        log.debug("Получен POST-запрос к эндпоинту: '/items' на добавление вещи владельцем с ID={}", ownerId);
        return itemService.create(itemDto, ownerId);
    }

    @ResponseBody
    @PatchMapping("/{itemId}")
    public ItemDto update(@PathVariable Long itemId, @RequestHeader(HEADER) Long ownerId, @RequestBody ItemDto itemDto) {
        log.debug("Получен PATCH-запрос к эндпоинту: '/items' на обновление вещи с ID={}", itemId);
        return itemService.update(itemId, ownerId, itemDto);
    }

    @DeleteMapping("/{itemId}")
    public ItemDto delete(@PathVariable Long itemId, @RequestHeader(HEADER) Long ownerId) {
        log.debug("Получен DELETE-запрос к эндпоинту: '/items' на удаление вещи с ID={}", itemId);
        return itemService.delete(itemId, ownerId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable Long itemId) {
        log.info("Получен GET-запрос к эндпоинту: '/items' на получение вещи с ID={}", itemId);
        return itemService.getItemById(itemId);
    }

    @GetMapping
    public List<ItemDto> getItemsByOwner(@RequestHeader(HEADER) Long ownerId) {
        log.info("Получен GET-запрос к эндпоинту: '/items' на получение всех вещей владельца с ID={}", ownerId);
        return itemService.getItemsByOwner(ownerId);
    }

    @GetMapping("/search")
    public List<ItemDto> getItemsBySearchQuery(@RequestParam String text) {
        log.info("Получен GET-запрос к эндпоинту: '/items/search' на поиск вещи с текстом={}", text);
        return itemService.getItemsBySearchQuery(text);
    }
}
