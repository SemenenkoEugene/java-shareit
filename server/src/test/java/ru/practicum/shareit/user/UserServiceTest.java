package ru.practicum.shareit.user;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.practicum.shareit.exception.UserAlreadyExistsException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
class UserServiceTest {

    private static final Long USER_ID = 1L;
    private static final User USER = User.builder()
            .id(USER_ID)
            .name("User " + USER_ID)
            .email("Email" + USER_ID + "@test.ru")
            .build();

    private static final UserDto USER_DTO = UserDto.builder().build();

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_Ok() {

        Mockito.when(userRepository.save(Mockito.any())).thenReturn(USER);

        final UserDto createUserDto = userService.create(USER_DTO);

        Assertions.assertThat(createUserDto)
                .isNotNull()
                .satisfies(dto -> {
                    Assertions.assertThat(dto.getId()).isEqualTo(USER.getId());
                    Assertions.assertThat(dto.getName()).isEqualTo(USER.getName());
                    Assertions.assertThat(dto.getEmail()).isEqualTo(USER.getEmail());
                });

        Mockito.verify(userRepository).save(Mockito.any(User.class));
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    void createUser_Conflict() {

        Mockito.when(userRepository.save(Mockito.any()))
                .thenThrow(new DataIntegrityViolationException("Пользователь с такими данными существует"));

        Assertions.assertThatThrownBy(() -> userService.create(USER_DTO))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Пользователь с такими данными существует");

        Mockito.verify(userRepository).save(Mockito.any(User.class));
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getUserById_Ok() {

        Mockito.when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(USER));

        final UserDto getUserByIdDto = userService.getUserById(USER.getId());

        Assertions.assertThat(getUserByIdDto)
                .isNotNull()
                .satisfies(dto -> {
                    Assertions.assertThat(dto.getId()).isEqualTo(USER.getId());
                    Assertions.assertThat(dto.getName()).isEqualTo(USER.getName());
                    Assertions.assertThat(dto.getEmail()).isEqualTo(USER.getEmail());
                });

        Mockito.verify(userRepository).findById(USER.getId());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getAllUsers_Ok() {

        Mockito.when(userRepository.findAll()).thenReturn(List.of(USER));

        final List<UserDto> getUsersListDto = userService.getUsers();

        Assertions.assertThat(getUsersListDto)
                .hasSize(1)
                .first()
                .satisfies(dto -> {
                    Assertions.assertThat(dto.getId()).isEqualTo(USER.getId());
                    Assertions.assertThat(dto.getName()).isEqualTo(USER.getName());
                    Assertions.assertThat(dto.getEmail()).isEqualTo(USER.getEmail());
                });

        Mockito.verify(userRepository).findAll();
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateUser_Ok() {

        Mockito.when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(USER));
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(USER);

        final UserDto updateUserDto = userService.update(USER_DTO, USER.getId());

        Assertions.assertThat(updateUserDto)
                .isNotNull()
                .satisfies(dto -> {
                    Assertions.assertThat(dto.getId()).isEqualTo(USER.getId());
                    Assertions.assertThat(dto.getName()).isEqualTo(USER.getName());
                    Assertions.assertThat(dto.getEmail()).isEqualTo(USER.getEmail());
                });

        Mockito.verify(userRepository).findById(USER.getId());
        Mockito.verify(userRepository).save(Mockito.any(User.class));
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateUser_Conflict() {

        Mockito.when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(USER));
        Mockito.when(userRepository.save(Mockito.any()))
                .thenThrow(new DataIntegrityViolationException("Пользователь с такими данными существует"));

        Assertions.assertThatThrownBy(() -> userService.update(USER_DTO, USER.getId()))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Пользователь с такими данными существует");

        Mockito.verify(userRepository).findById(USER.getId());
        Mockito.verify(userRepository).save(Mockito.any(User.class));
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    void deleteTest() {

        userService.delete(USER_ID);

        Mockito.verify(userRepository).deleteById(USER_ID);
        Mockito.verifyNoMoreInteractions(userRepository);
    }
}
