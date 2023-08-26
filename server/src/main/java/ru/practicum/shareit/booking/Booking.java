package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "bookings")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long id;        //уникальный идентификатор бронирования

    @Column(name = "start_date")
    private LocalDateTime start;        //дата начала бронирования

    @Column(name = "end_date")
    private LocalDateTime end;          //дата окончания бронирования

    @ManyToOne()
    @JoinColumn(name = "item_id")
    private Item item;                  //вещь, которую бронируют

    @ManyToOne()
    @JoinColumn(name = "booker_id")
    private User user;                //пользователь, который осуществляет бронирование

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;              //статус бронирования

}
