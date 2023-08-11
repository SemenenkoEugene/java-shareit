package ru.practicum.shareit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

@Component
@RequiredArgsConstructor
public class ServiceUtil {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final ItemRequestRepository itemRequestRepository;

    public User getUserOrThrowNotFound(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
    }

    public Item getItemOrThrowNotFound(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Вещь не найдена"));
    }

    public Booking getBookingOrThrowNotFound(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Бронирование не найдено"));
    }

    public ItemRequest getItemRequestOrThrowNotFound(Long id) {
        return itemRequestRepository.findById(id)
                .orElseThrow(() -> new ItemRequestNotFoundException("Запрос не найден"));
    }
}
