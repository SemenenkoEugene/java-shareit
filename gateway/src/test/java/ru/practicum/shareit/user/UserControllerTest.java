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
import ru.practicum.shareit.generated.user.dto.CreateUserRequest;
import ru.practicum.shareit.generated.user.dto.UpdateUserRequest;

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
        Mockito.when(userClient.update(Mockito.anyLong(), Mockito.any(UpdateUserRequest.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/1")
                        .contentType("application/json")
                        .content("{\"name\":\"First line\\nSecond line\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(userClient).update(Mockito.eq(1L), Mockito.argThat(user ->
                "First line\nSecond line".equals(user.name()) && user.email() == null));
    }

    @Test
    void updateShouldAllowSingleValidField() throws Exception {
        Mockito.when(userClient.update(Mockito.anyLong(), Mockito.any(UpdateUserRequest.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/1")
                        .contentType("application/json")
                        .content("{\"email\":\"updated@example.com\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(userClient).update(Mockito.eq(1L), Mockito.argThat(user ->
                user.name() == null && "updated@example.com".equals(user.email())));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"name\":null}",
            "{\"email\":null}",
            "{\"name\":null,\"email\":null}"
    })
    void updateShouldPreserveExplicitNullAsNoOp(final String body) throws Exception {
        Mockito.when(userClient.update(Mockito.anyLong(), Mockito.any(UpdateUserRequest.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/1")
                        .contentType("application/json")
                        .content(body))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(userClient).update(Mockito.eq(1L), Mockito.argThat(user ->
                user.name() == null && user.email() == null));
    }

    @Test
    void updateShouldAllowBothFieldsToBeAbsent() throws Exception {
        Mockito.when(userClient.update(Mockito.anyLong(), Mockito.any(UpdateUserRequest.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/1")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(userClient).update(Mockito.eq(1L), Mockito.argThat(user ->
                user.name() == null && user.email() == null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",
            "{\"email\":\"user@example.com\"}",
            "{\"name\":\"User\"}",
            "{\"name\":null,\"email\":\"user@example.com\"}",
            "{\"name\":\"User\",\"email\":null}",
            "{\"name\":\"\",\"email\":\"user@example.com\"}",
            "{\"name\":\"   \",\"email\":\"user@example.com\"}",
            "{\"name\":\"User\",\"email\":\"\"}",
            "{\"name\":\"User\",\"email\":\"   \"}",
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
        Mockito.when(userClient.create(Mockito.any(CreateUserRequest.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType("application/json")
                        .content("{\"name\":\"User\",\"email\":\"user@example.com\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(userClient).create(Mockito.argThat(user ->
                "User".equals(user.name())
                        && "user@example.com".equals(user.email())
                        && user.id() == null));
    }
}
