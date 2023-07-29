package ru.practicum.shareit.user;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;    //уникальный идентификатор пользователя

    @Column(name = "user_name")
    private String name; //имя или логин пользователя

    private String email; // адрес электронной почты
}
