package ru.practicum.shareit.user;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.UserAlreadyExistsException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.*;

@Component("InMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private Long currentId = 0L;

    @Override
    public User createUser(User user) {
        verificationEmail(user);
        var nextId = ++currentId;
        user.setId(nextId);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        user.setId(user.getId());
        verificationEmail(user);
        var userUpdate = users.get(user.getId());
        if (user.getId() == null) {
            throw new ValidationException("Передан пустой аргумент!");
        }
        if (!users.containsKey(user.getId())) {
            throw new UserNotFoundException("Пользователь с ID=" + user.getId() + " не найден!");
        }
        if (user.getName() == null) {
            user.setName(userUpdate.getName());
        }
        if (user.getEmail() == null) {
            user.setEmail(userUpdate.getEmail());
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User deleteUser(Long userId) {
        if (userId == null) {
            throw new ValidationException("Передан пустой аргумент!");
        }
        if (!users.containsKey(userId)) {
            throw new UserNotFoundException("Пользователь с ID=" + userId + " не найден!");
        }
        return users.remove(userId);
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUserById(Long userId) {
        if (!users.containsKey(userId)) {
            throw new UserNotFoundException("Пользователь с ID=" + userId + " не найден!");
        }
        return users.get(userId);
    }

    private void verificationEmail(User user) {
        boolean verification = users.values().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(user.getEmail())
                               && !Objects.equals(u.getId(), user.getId()));
        if (verification) {
            throw new UserAlreadyExistsException("Пользователь  с таким E-mail " + user.getEmail() + " уже существует!");
        }
    }
}
