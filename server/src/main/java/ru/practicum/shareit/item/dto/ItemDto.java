package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Builder(toBuilder = true)
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
    @Builder(toBuilder = true)
    public static class ItemBooking {
        private Long id;
        private Long bookerId;
    }
}
