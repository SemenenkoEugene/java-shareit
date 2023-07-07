package ru.practicum.shareit.user;

import java.util.List;

public interface UserStorage {

    User createUser(User user);

    User updateUser(User user);

    User deleteUser(Long userId);

    List<User> getUsers();

    User getUserById(Long userId);
}
