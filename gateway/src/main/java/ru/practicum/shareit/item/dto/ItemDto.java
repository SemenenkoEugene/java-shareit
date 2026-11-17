package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.validation.OnUpdate;

import java.util.List;

@Setter
@Getter
@Builder(toBuilder = true)
public class ItemDto {
    private Long id;
    @NotBlank
    @Pattern(regexp = ".*\\S.*", flags = Pattern.Flag.DOTALL, groups = OnUpdate.class)
    private String name;
    @NotBlank
    @Pattern(regexp = ".*\\S.*", flags = Pattern.Flag.DOTALL, groups = OnUpdate.class)
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
