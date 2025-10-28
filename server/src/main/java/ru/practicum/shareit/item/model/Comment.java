package ru.practicum.shareit.item.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Table(name = "comments")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
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
