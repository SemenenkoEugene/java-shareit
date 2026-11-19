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
import ru.practicum.shareit.generated.user.dto.CreateUserRequest;
import ru.practicum.shareit.generated.user.dto.UpdateUserRequest;
import ru.practicum.shareit.generated.user.dto.UserResponse;

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

    private static final CreateUserRequest CREATE_USER_REQUEST = CreateUserRequest.builder().build();
    private static final UpdateUserRequest UPDATE_USER_REQUEST = UpdateUserRequest.builder().build();

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_Ok() {

        Mockito.when(userRepository.save(Mockito.any())).thenReturn(USER);

        final UserResponse createUserDto = userService.create(CREATE_USER_REQUEST);

        Assertions.assertThat(createUserDto)
                .isNotNull()
                .satisfies(dto -> {
                    Assertions.assertThat(dto.id()).isEqualTo(USER.getId());
                    Assertions.assertThat(dto.name()).isEqualTo(USER.getName());
                    Assertions.assertThat(dto.email()).isEqualTo(USER.getEmail());
                });

        Mockito.verify(userRepository).save(Mockito.any(User.class));
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    void createUser_Conflict() {

        Mockito.when(userRepository.save(Mockito.any()))
                .thenThrow(new DataIntegrityViolationException("Пользователь с такими данными существует"));

        Assertions.assertThatThrownBy(() -> userService.create(CREATE_USER_REQUEST))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Пользователь с такими данными существует");

        Mockito.verify(userRepository).save(Mockito.any(User.class));
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getUserById_Ok() {

        Mockito.when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(USER));

        final UserResponse getUserByIdDto = userService.getUserById(USER.getId());

        Assertions.assertThat(getUserByIdDto)
                .isNotNull()
                .satisfies(dto -> {
                    Assertions.assertThat(dto.id()).isEqualTo(USER.getId());
                    Assertions.assertThat(dto.name()).isEqualTo(USER.getName());
                    Assertions.assertThat(dto.email()).isEqualTo(USER.getEmail());
                });

        Mockito.verify(userRepository).findById(USER.getId());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getAllUsers_Ok() {

        Mockito.when(userRepository.findAll()).thenReturn(List.of(USER));

        final List<UserResponse> getUsersListDto = userService.getUsers();

        Assertions.assertThat(getUsersListDto)
                .hasSize(1)
                .first()
                .satisfies(dto -> {
                    Assertions.assertThat(dto.id()).isEqualTo(USER.getId());
                    Assertions.assertThat(dto.name()).isEqualTo(USER.getName());
                    Assertions.assertThat(dto.email()).isEqualTo(USER.getEmail());
                });

        Mockito.verify(userRepository).findAll();
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateUser_Ok() {

        Mockito.when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(USER));
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(USER);

        final UserResponse updateUserDto = userService.update(UPDATE_USER_REQUEST, USER.getId());

        Assertions.assertThat(updateUserDto)
                .isNotNull()
                .satisfies(dto -> {
                    Assertions.assertThat(dto.id()).isEqualTo(USER.getId());
                    Assertions.assertThat(dto.name()).isEqualTo(USER.getName());
                    Assertions.assertThat(dto.email()).isEqualTo(USER.getEmail());
                });

        Mockito.verify(userRepository).findById(USER.getId());
        Mockito.verify(userRepository).save(Mockito.any(User.class));
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateUserShouldApplyOnlyNonNullFieldsAndIgnoreRequestId() {
        final User user = User.builder()
                .id(USER_ID)
                .name("Old name")
                .email("old@test.ru")
                .build();
        final UpdateUserRequest updateRequest = UpdateUserRequest.builder()
                .id(999L)
                .name("Updated name")
                .build();
        Mockito.when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(user)).thenReturn(user);

        final UserResponse result = userService.update(updateRequest, USER_ID);

        Assertions.assertThat(user.getId()).isEqualTo(USER_ID);
        Assertions.assertThat(user.getName()).isEqualTo("Updated name");
        Assertions.assertThat(user.getEmail()).isEqualTo("old@test.ru");
        Assertions.assertThat(result.id()).isEqualTo(USER_ID);
        Mockito.verify(userRepository).findById(USER_ID);
        Mockito.verify(userRepository).save(user);
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateUserShouldKeepFieldsForNullValues() {
        final User user = User.builder()
                .id(USER_ID)
                .name("Old name")
                .email("old@test.ru")
                .build();
        Mockito.when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(user)).thenReturn(user);

        userService.update(UpdateUserRequest.builder().name(null).email(null).build(), USER_ID);

        Assertions.assertThat(user.getName()).isEqualTo("Old name");
        Assertions.assertThat(user.getEmail()).isEqualTo("old@test.ru");
        Mockito.verify(userRepository).findById(USER_ID);
        Mockito.verify(userRepository).save(user);
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateUserShouldApplyEmailOnly() {
        final User user = User.builder()
                .id(USER_ID)
                .name("Old name")
                .email("old@test.ru")
                .build();
        Mockito.when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(user)).thenReturn(user);

        userService.update(UpdateUserRequest.builder().email("updated@test.ru").build(), USER_ID);

        Assertions.assertThat(user.getName()).isEqualTo("Old name");
        Assertions.assertThat(user.getEmail()).isEqualTo("updated@test.ru");
        Mockito.verify(userRepository).findById(USER_ID);
        Mockito.verify(userRepository).save(user);
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateUser_Conflict() {

        Mockito.when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(USER));
        Mockito.when(userRepository.save(Mockito.any()))
                .thenThrow(new DataIntegrityViolationException("Пользователь с такими данными существует"));

        Assertions.assertThatThrownBy(() -> userService.update(UPDATE_USER_REQUEST, USER.getId()))
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
