package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ItemDto {

    private Long id;
    private String name;
    private String description;
    private Boolean available;

    private ItemBooking nextBooking;
    private ItemBooking lastBooking;

    private List<CommentDto> comments;

    private Long requestId;

    @Data
    @Builder
    public static class ItemBooking {
        private Long id;
        private Long bookerId;
    }
}
