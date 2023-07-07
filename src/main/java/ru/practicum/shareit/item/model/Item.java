package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
public class Item {

    private Long id;                //уникальный идентификатор вещи
    @NotBlank
    private String name;            //краткое название
    @NotBlank
    private String description;     //развёрнутое описание
    @NotNull
    private Boolean available;      //статус о том, доступна или нет вещь для аренды
    @NotNull
    private Long ownerId;           //владелец вещи
    private Long requestId;         //если вещь была создана по запросу другого пользователя, то в этом
                                    // поле хранится ссылка на соответствующий запрос
}
