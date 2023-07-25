package ru.practicum.shareit.item.model;

import lombok.Data;
import ru.practicum.shareit.user.User;

import javax.persistence.*;

@Data
@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;                //уникальный идентификатор вещи

    @Column(name = "item_name")
    private String name;            //краткое название

    private String description;     //развёрнутое описание
    private Boolean available;      //статус о том, доступна или нет вещь для аренды

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;           //владелец вещи
    private Long requestId;         //если вещь была создана по запросу другого пользователя, то в этом
    // поле хранится ссылка на соответствующий запрос
}
