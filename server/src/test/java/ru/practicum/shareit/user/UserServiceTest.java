package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.exception.UserAlreadyExistsException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_Ok() {
        UserDto userDto = UserDto.builder().build();

        User user = getUser(1L);

        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto createUserDto = userService.create(userDto);

        assertThat(createUserDto.getId(), equalTo(user.getId()));
        assertThat(createUserDto.getName(), equalTo(user.getName()));
        assertThat(createUserDto.getEmail(), equalTo(user.getEmail()));

        verify(userRepository, times(1)).save(any(User.class));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void createUser_Conflict() {
        var userDto = UserDto.builder().build();

        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("Пользователь с такими данными существует"));

        var userAlreadyExistsException = assertThrows(UserAlreadyExistsException.class,
                () -> userService.create(userDto));
        assertThat(userAlreadyExistsException.getMessage(), equalTo("Пользователь с такими данными существует"));

        verify(userRepository, times(1)).save(any(User.class));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getUserById_Ok() {
        User user = getUser(1L);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        UserDto getUserByIdDto = userService.getUserById(user.getId());

        assertThat(getUserByIdDto.getId(), equalTo(user.getId()));
        assertThat(getUserByIdDto.getName(), equalTo(user.getName()));
        assertThat(getUserByIdDto.getEmail(), equalTo(user.getEmail()));

        verify(userRepository, times(1)).findById(user.getId());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getAllUsers_Ok() {
        User user1 = getUser(1L);
        User user2 = getUser(2L);

        List<User> userList = Arrays.asList(
                user1,
                user2
        );

        when(userRepository.findAll())
                .thenReturn(userList);

        List<UserDto> getUsersListDto = userService.getUsers();

        assertThat(getUsersListDto.get(0).getId(), equalTo(user1.getId()));
        assertThat(getUsersListDto.get(0).getName(), equalTo(user1.getName()));
        assertThat(getUsersListDto.get(0).getEmail(), equalTo(user1.getEmail()));

        assertThat(getUsersListDto.get(1).getId(), equalTo(user2.getId()));
        assertThat(getUsersListDto.get(1).getName(), equalTo(user2.getName()));
        assertThat(getUsersListDto.get(1).getEmail(), equalTo(user2.getEmail()));

        verify(userRepository, times(1)).findAll();
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateUser_Ok() {
        UserDto userDto = UserDto.builder().build();

        User user = getUser(1L);

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        UserDto updateUserDto = userService.update(userDto, user.getId());

        assertThat(updateUserDto.getId(), equalTo(user.getId()));
        assertThat(updateUserDto.getName(), equalTo(user.getName()));
        assertThat(updateUserDto.getEmail(), equalTo(user.getEmail()));

        verify(userRepository, times(1)).findById(user.getId());
        verify(userRepository, times(1)).save(any(User.class));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateUser_Conflict() {
        UserDto userDto = UserDto.builder().build();

        User user = getUser(1L);

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("Пользователь с такими данными существует"));

        var userAlreadyExistsException = assertThrows(UserAlreadyExistsException.class,
                () -> userService.update(userDto, user.getId()));

        assertThat(userAlreadyExistsException.getMessage(), equalTo("Пользователь с такими данными существует"));

        verify(userRepository, times(1)).findById(user.getId());
        verify(userRepository, times(1)).save(any(User.class));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void deleteTest() {
        userService.delete(1L);

        verify(userRepository, times(1)).deleteById(1L);
        verifyNoMoreInteractions(userRepository);
    }

    private User getUser(Long id) {
        return User.builder()
                .id(1L)
                .name("User " + id)
                .email("Email" + id + "@test.ru")
                .build();
    }
}