package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder(toBuilder = true)
public class ItemRequestGetResponseDto {
    private Long id;
    private String description;
    private LocalDateTime created;
    private List<RequestedItem> items;

    @Setter
    @Getter
    @Builder(toBuilder = true)
    public static class RequestedItem {
        private Long id;
        private String name;
        private String description;
        private Boolean available;
        private Long requestId;
    }
}
