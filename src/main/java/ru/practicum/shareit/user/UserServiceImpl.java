package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.UserAlreadyExistsException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Validation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String USER_ALREADY_EXISTS = "Пользователь с такими данными существует";
    private static final String INVALID_VALUE_FOR_UPDATE = "Некорректное значение для обновления";
    private static final String USER_NOT_FOUND = "Пользователь не найден";
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        try {
            return UserMapper.toUserDto(userRepository.save(user));
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException(USER_ALREADY_EXISTS);
        }
    }

    @Override
    @Transactional
    public UserDto update(UserDto userDto, Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));

        Optional.ofNullable(userDto.getName()).ifPresent(user::setName);
        Optional.ofNullable(userDto.getEmail()).ifPresent(user::setEmail);

        if (isValid(UserMapper.toUserDto(user))) {
            try {
                return UserMapper.toUserDto(userRepository.save(user));
            } catch (DataIntegrityViolationException e) {
                throw new UserAlreadyExistsException(USER_ALREADY_EXISTS);
            }
        } else {
            throw new ValidationException(INVALID_VALUE_FOR_UPDATE);
        }
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        userRepository.deleteById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private boolean isValid(UserDto userDto) {
        try {
            var validator = Validation.buildDefaultValidatorFactory().getValidator();
            var validate = validator.validate(userDto);
            return validate.isEmpty();
        } catch (ValidationException e) {
            throw new ValidationException(e.getMessage());
        }
    }
}
