package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Builder(toBuilder = true)
public class ItemDto {
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @NotNull
    private Boolean available;

    private ItemBooking nextBooking;
    private ItemBooking lastBooking;

    private List<CommentDto> comments;

    private Long requestId;

    @Setter
    @Getter
    @Builder(toBuilder = true)
    public static class ItemBooking {
        private Long id;
        private Long bookerId;
    }
}
