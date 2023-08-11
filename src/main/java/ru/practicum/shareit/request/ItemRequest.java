package ru.practicum.shareit.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "request")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column(name = "request_description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "request_id")
    private User requestor;

    @Column(name = "create_date")
    private LocalDateTime created;

}
