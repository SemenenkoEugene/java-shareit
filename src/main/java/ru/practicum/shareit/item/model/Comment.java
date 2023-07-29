package ru.practicum.shareit.item.model;

import lombok.Data;
import ru.practicum.shareit.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;            //уникальный идентификатор комментария

    @Column(name = "comment_text")
    private String text;        //содержимое комментария

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;          //вещь, к которой относится комментарий

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;        //автор комментария

    @Column(name = "created_date")
    private LocalDateTime created;  //дата создания комментария
}
