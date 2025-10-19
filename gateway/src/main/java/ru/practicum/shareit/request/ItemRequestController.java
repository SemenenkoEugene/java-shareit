package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;


@Validated
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    public static final String X_SHARER_USER_ID = "X-Sharer-User-Id";
    private final ItemRequestClient itemRequestClient;

    @GetMapping
    public ResponseEntity<Object> getAllByOwnerId(@RequestHeader(X_SHARER_USER_ID) Long userId,
                                                  @RequestParam(required = false, defaultValue = "0") @Min(0) int from,
                                                  @RequestParam(required = false, defaultValue = "20") @Min(1) int size) {
        return itemRequestClient.getAllByOwnerId(userId, from, size);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAll(@RequestHeader(X_SHARER_USER_ID) Long userId,
                                         @RequestParam(required = false, defaultValue = "0") @Min(0) int from,
                                         @RequestParam(required = false, defaultValue = "20") @Min(1) int size) {
        return itemRequestClient.getAll(userId, from, size);
    }

    @GetMapping("/{itemRequestId}")
    public ResponseEntity<Object> getById(@RequestHeader(X_SHARER_USER_ID) Long userId,
                                          @PathVariable Long itemRequestId) {
        return itemRequestClient.getById(userId, itemRequestId);
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(X_SHARER_USER_ID) Long userId,
                                         @Valid @RequestBody ItemRequestCreateDto itemRequestCreateDto) {
        return itemRequestClient.create(userId, itemRequestCreateDto);
    }
}
