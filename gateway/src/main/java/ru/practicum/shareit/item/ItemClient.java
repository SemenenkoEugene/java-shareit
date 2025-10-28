package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    public ItemClient(@Value("${shareit-server.url}") final String serverUrl, final RestTemplateBuilder builder) {
        super(builder.uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build());
    }

    public ResponseEntity<Object> getById(final Long userId, final Long itemId) {
        return get("/" + itemId, userId);
    }

    public ResponseEntity<Object> getAllByOwnerId(final Long userId, final Integer from, final Integer size) {
        final Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get("?from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> getAllBySearchText(final Long userId, final String text, final Integer from, final Integer size) {
        final Map<String, Object> parameters = Map.of(
                "text", text,
                "from", from,
                "size", size
        );
        return get("/search?text={text}&from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> create(final Long userId, final ItemDto requestDto) {
        return post("", userId, requestDto);
    }

    public ResponseEntity<Object> createComment(final Long userId, final Long itemId, final CommentDto requestDto) {
        return post("/" + itemId + "/comment", userId, requestDto);
    }

    public ResponseEntity<Object> update(final Long userId, final Long itemId, final ItemDto itemDto) {
        return patch("/" + itemId, userId, null, itemDto);
    }

    public void delete(final Long itemId) {
        delete("/" + itemId);
    }
}
