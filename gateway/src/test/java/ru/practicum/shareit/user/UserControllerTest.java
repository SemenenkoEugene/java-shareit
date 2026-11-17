package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.shareit.user.dto.UserDto;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserClient userClient;

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"name\":\"\"}",
            "{\"name\":\"   \"}",
            "{\"email\":\"\"}",
            "{\"email\":\"   \"}",
            "{\"email\":\"invalid\"}",
            "{\"name\":\" \\t\\n \"}"
    })
    void updateShouldRejectInvalidFields(final String body) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/users/1")
                        .contentType("application/json")
                        .content(body))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verifyNoInteractions(userClient);
    }

    @Test
    void updateShouldAllowMultilineName() throws Exception {
        Mockito.when(userClient.update(Mockito.anyLong(), Mockito.any(UserDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/1")
                        .contentType("application/json")
                        .content("{\"name\":\"First line\\nSecond line\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(userClient).update(Mockito.eq(1L), Mockito.argThat(user ->
                "First line\nSecond line".equals(user.getName())));
    }

    @Test
    void updateShouldAllowSingleValidField() throws Exception {
        Mockito.when(userClient.update(Mockito.anyLong(), Mockito.any(UserDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/1")
                        .contentType("application/json")
                        .content("{\"email\":\"updated@example.com\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(userClient).update(Mockito.eq(1L), Mockito.argThat(user ->
                user.getName() == null && "updated@example.com".equals(user.getEmail())));
    }

    @Test
    void updateShouldPreserveExplicitNullAsNoOp() throws Exception {
        Mockito.when(userClient.update(Mockito.anyLong(), Mockito.any(UserDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/1")
                        .contentType("application/json")
                        .content("{\"email\":null}"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(userClient).update(Mockito.eq(1L), Mockito.argThat(user -> user.getEmail() == null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"email\":\"user@example.com\"}",
            "{\"name\":\"User\"}",
            "{\"name\":\"\",\"email\":\"user@example.com\"}",
            "{\"name\":\"User\",\"email\":\"\"}",
            "{\"name\":\"User\",\"email\":\"invalid\"}"
    })
    void createShouldRejectInvalidFields(final String body) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType("application/json")
                        .content(body))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verifyNoInteractions(userClient);
    }

    @Test
    void createShouldAllowValidRequest() throws Exception {
        Mockito.when(userClient.create(Mockito.any(UserDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType("application/json")
                        .content("{\"name\":\"User\",\"email\":\"user@example.com\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(userClient).create(Mockito.any(UserDto.class));
    }
}
