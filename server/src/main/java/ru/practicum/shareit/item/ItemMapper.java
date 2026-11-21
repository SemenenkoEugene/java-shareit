package ru.practicum.shareit.item;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.generated.item.dto.CreateItemRequest;
import ru.practicum.shareit.generated.item.dto.ItemResponse;
import ru.practicum.shareit.item.model.Item;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemMapper {

    public static ItemResponse toItemResponse(final Item item) {
        return ItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId((item.getItemRequest() != null) ? item.getItemRequest().getId() : null)
                .build();
    }

    public static Item toItem(final CreateItemRequest itemDto) {
        final Item item = new Item();
        item.setName(itemDto.name());
        item.setDescription(itemDto.description());
        item.setAvailable(itemDto.available());
        return item;
    }
}
