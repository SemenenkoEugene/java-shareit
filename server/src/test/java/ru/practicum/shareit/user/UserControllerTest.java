package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.shareit.generated.user.dto.CreateUserRequest;
import ru.practicum.shareit.generated.user.dto.UpdateUserRequest;
import ru.practicum.shareit.generated.user.dto.UserResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    private static final UserResponse USER_RESPONSE = UserResponse.builder()
            .id(1L)
            .name("User 1")
            .email("Email1@test.ru")
            .build();
    private static final CreateUserRequest CREATE_USER_REQUEST = CreateUserRequest.builder()
            .id(USER_RESPONSE.id())
            .name(USER_RESPONSE.name())
            .email(USER_RESPONSE.email())
            .build();


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @SneakyThrows
    @Test
    void createUserTest() {
        Mockito.when(userService.create(Mockito.any())).thenReturn(USER_RESPONSE);

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .content(objectMapper.writeValueAsString(CREATE_USER_REQUEST))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(USER_RESPONSE.id()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(USER_RESPONSE.name()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(USER_RESPONSE.email()));

        Mockito.verify(userService).create(CREATE_USER_REQUEST);
        Mockito.verifyNoMoreInteractions(userService);
    }

    @SneakyThrows
    @Test
    void createUserShouldNotApplyGeneratedValidation() {
        Mockito.when(userService.create(Mockito.any())).thenReturn(USER_RESPONSE);

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .content("{}")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(userService).create(CreateUserRequest.builder().build());
        Mockito.verifyNoMoreInteractions(userService);
    }

    @SneakyThrows
    @Test
    void updateUserTest() {
        Mockito.when(userService.update(Mockito.any(), Mockito.anyLong())).thenReturn(USER_RESPONSE);

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/" + USER_RESPONSE.id())
                        .content("{\"id\":999,\"name\":\"Updated\",\"email\":\"updated@test.ru\"}")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(USER_RESPONSE.id()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(USER_RESPONSE.name()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(USER_RESPONSE.email()));

        Mockito.verify(userService).update(
                UpdateUserRequest.builder()
                        .id(999L)
                        .name("Updated")
                        .email("updated@test.ru")
                        .build(),
                USER_RESPONSE.id()
        );
        Mockito.verifyNoMoreInteractions(userService);
    }

    @SneakyThrows
    @Test
    void updateUserShouldAcceptPartialAndUnvalidatedPayloads() {
        Mockito.when(userService.update(Mockito.any(), Mockito.anyLong())).thenReturn(USER_RESPONSE);

        final List<String> payloads = List.of(
                "{}",
                "{\"name\":null,\"email\":null}",
                "{\"name\":\"Updated\"}",
                "{\"email\":\"updated@test.ru\"}",
                "{\"name\":\"\"}",
                "{\"email\":\"not-an-email\"}"
        );

        for (final String payload : payloads) {
            mockMvc.perform(MockMvcRequestBuilders.patch("/users/" + USER_RESPONSE.id())
                            .content(payload)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        }

        Mockito.verify(userService, Mockito.times(payloads.size()))
                .update(Mockito.any(UpdateUserRequest.class), Mockito.eq(USER_RESPONSE.id()));
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    @SneakyThrows
    void deleteUserTest() {
        mockMvc.perform(MockMvcRequestBuilders.delete("/users/1"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(userService).delete(1L);
        Mockito.verifyNoMoreInteractions(userService);
    }

    @SneakyThrows
    @Test
    void getUsersTest() {

        Mockito.when(userService.getUsers()).thenReturn(List.of(USER_RESPONSE));

        mockMvc.perform(MockMvcRequestBuilders.get("/users"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(USER_RESPONSE.id()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(USER_RESPONSE.name()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].email").value(USER_RESPONSE.email()));

        Mockito.verify(userService).getUsers();
        Mockito.verifyNoMoreInteractions(userService);
    }

    @SneakyThrows
    @Test
    void getUserByIdTest() {

        Mockito.when(userService.getUserById(Mockito.anyLong())).thenReturn(USER_RESPONSE);

        mockMvc.perform(MockMvcRequestBuilders.get("/users/" + USER_RESPONSE.id()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(USER_RESPONSE.id()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(USER_RESPONSE.name()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(USER_RESPONSE.email()));

        Mockito.verify(userService).getUserById(USER_RESPONSE.id());
        Mockito.verifyNoMoreInteractions(userService);
    }

}
