package ru.practicum.shareit.service;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

public class ServiceUtil {

    public static User getUserOrThrowNotFound(Long id, UserRepository userRepository) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
    }

    public static Item getItemOrThrowNotFound(Long id, ItemRepository itemRepository) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Вещь не найдена"));
    }

    public static Booking getBookingOrThrowNotFound(Long id, BookingRepository bookingRepository) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Бронирование не найдено"));
    }
}
