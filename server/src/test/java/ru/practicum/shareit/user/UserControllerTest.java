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
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.util.List;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    private static final UserDto USER_DTO = UserDto.builder()
            .id(1L)
            .name("User 1")
            .email("Email1@test.ru")
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
        Mockito.when(userService.create(Mockito.any())).thenReturn(USER_DTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .content(objectMapper.writeValueAsString(USER_DTO))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(USER_DTO.getId()));

        Mockito.verify(userService).create(Mockito.any(UserDto.class));
        Mockito.verifyNoMoreInteractions(userService);
    }

    @SneakyThrows
    @Test
    void updateUserTest() {

        Mockito.when(userService.update(Mockito.any(), Mockito.anyLong())).thenReturn(USER_DTO);

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/" + USER_DTO.getId())
                        .content(objectMapper.writeValueAsString(USER_DTO))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(USER_DTO.getId()));

        Mockito.verify(userService).update(Mockito.any(UserDto.class), Mockito.eq(USER_DTO.getId()));
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

        Mockito.when(userService.getUsers()).thenReturn(List.of(USER_DTO));

        mockMvc.perform(MockMvcRequestBuilders.get("/users"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(USER_DTO.getId()));

        Mockito.verify(userService).getUsers();
        Mockito.verifyNoMoreInteractions(userService);
    }

    @SneakyThrows
    @Test
    void getUserByIdTest() {

        Mockito.when(userService.getUserById(Mockito.anyLong())).thenReturn(USER_DTO);

        mockMvc.perform(MockMvcRequestBuilders.get("/users/" + USER_DTO.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(USER_DTO.getId()));

        Mockito.verify(userService).getUserById(USER_DTO.getId());
        Mockito.verifyNoMoreInteractions(userService);
    }

}