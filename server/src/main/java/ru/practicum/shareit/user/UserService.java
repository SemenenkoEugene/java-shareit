package ru.practicum.shareit.user;

import ru.practicum.shareit.generated.user.dto.CreateUserRequest;
import ru.practicum.shareit.generated.user.dto.UpdateUserRequest;
import ru.practicum.shareit.generated.user.dto.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse create(CreateUserRequest userDto);

    UserResponse update(UpdateUserRequest userDto, Long id);

    void delete(Long userId);

    UserResponse getUserById(Long id);

    List<UserResponse> getUsers();
}
