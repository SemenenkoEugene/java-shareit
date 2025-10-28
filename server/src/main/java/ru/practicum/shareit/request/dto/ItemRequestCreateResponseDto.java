package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder(toBuilder = true)
public class ItemRequestCreateResponseDto {
    private Long id;
    private String description;
    private LocalDateTime created;
}
