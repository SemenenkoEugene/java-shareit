package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;
    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(@Qualifier("InMemoryUserStorage") UserStorage userStorage, UserMapper userMapper) {
        this.userStorage = userStorage;
        this.userMapper = userMapper;
    }

    @Override
    public UserDto create(UserDto userDto) {
        return userMapper.toUserDto(userStorage.createUser(UserMapper.toUser(userDto)));
    }

    @Override
    public UserDto update(UserDto userDto, Long id) {
        if (userDto.getId() == null) {
            userDto.setId(id);
        }
        return userMapper.toUserDto(userStorage.updateUser(UserMapper.toUser(userDto)));
    }

    @Override
    public UserDto delete(Long userId) {
        return userMapper.toUserDto(userStorage.deleteUser(userId));
    }

    @Override
    public UserDto getUserById(Long id) {
        return userMapper.toUserDto(userStorage.getUserById(id));
    }

    @Override
    public List<UserDto> getUsers() {
        return userStorage.getUsers().stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }
}
