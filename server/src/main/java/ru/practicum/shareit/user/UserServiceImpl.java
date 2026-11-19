package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UserAlreadyExistsException;
import ru.practicum.shareit.generated.user.dto.CreateUserRequest;
import ru.practicum.shareit.generated.user.dto.UpdateUserRequest;
import ru.practicum.shareit.generated.user.dto.UserResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String USER_ALREADY_EXISTS = "Пользователь с такими данными существует";
    private static final String USER_NOT_FOUND = "Пользователь не найден";

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserResponse create(final CreateUserRequest userDto) {
        final User user = UserMapper.toUser(userDto);
        try {
            return UserMapper.toUserResponse(userRepository.save(user));
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException(USER_ALREADY_EXISTS, e);
        }
    }

    @Override
    @Transactional
    public UserResponse update(final UpdateUserRequest userDto, final Long id) {
        final User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        Optional.ofNullable(userDto.name()).ifPresent(user::setName);
        Optional.ofNullable(userDto.email()).ifPresent(user::setEmail);

        try {
            return UserMapper.toUserResponse(userRepository.save(user));
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException(USER_ALREADY_EXISTS, e);
        }
    }

    @Override
    @Transactional
    public void delete(final Long userId) {
        userRepository.deleteById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(final Long id) {
        final User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
        return UserMapper.toUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserResponse)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
