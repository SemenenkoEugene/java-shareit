package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UserAlreadyExistsException;
import ru.practicum.shareit.user.dto.UserDto;

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
    public UserDto create(final UserDto userDto) {
        final User user = UserMapper.toUser(userDto);
        try {
            return UserMapper.toUserDto(userRepository.save(user));
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException(USER_ALREADY_EXISTS, e);
        }
    }

    @Override
    @Transactional
    public UserDto update(final UserDto userDto, final Long id) {
        final User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        Optional.ofNullable(userDto.getName()).ifPresent(user::setName);
        Optional.ofNullable(userDto.getEmail()).ifPresent(user::setEmail);

        try {
            return UserMapper.toUserDto(userRepository.save(user));
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
    public UserDto getUserById(final Long id) {
        final User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
