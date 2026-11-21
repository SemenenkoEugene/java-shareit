package ru.practicum.shareit.item;

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
import ru.practicum.shareit.generated.item.dto.CreateItemRequest;
import ru.practicum.shareit.generated.item.dto.UpdateItemRequest;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemClient itemClient;

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"name\":\"\"}",
            "{\"name\":\"   \"}",
            "{\"description\":\"\"}",
            "{\"description\":\"   \"}",
            "{\"description\":\" \\t\\n \"}"
    })
    void updateShouldRejectBlankFields(final String body) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/items/1")
                        .header(ItemController.X_SHARER_USER_ID, 2L)
                        .contentType("application/json")
                        .content(body))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verifyNoInteractions(itemClient);
    }

    @Test
    void updateShouldAllowMultilineDescription() throws Exception {
        Mockito.when(itemClient.update(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(UpdateItemRequest.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(MockMvcRequestBuilders.patch("/items/1")
                        .header(ItemController.X_SHARER_USER_ID, 2L)
                        .contentType("application/json")
                        .content("{\"description\":\"First line\\nSecond line\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(itemClient).update(Mockito.eq(2L), Mockito.eq(1L), Mockito.argThat(item ->
                "First line\nSecond line".equals(item.description())));
    }

    @Test
    void updateShouldAllowSingleValidField() throws Exception {
        Mockito.when(itemClient.update(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(UpdateItemRequest.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(MockMvcRequestBuilders.patch("/items/1")
                        .header(ItemController.X_SHARER_USER_ID, 2L)
                        .contentType("application/json")
                        .content("{\"name\":\"Updated\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(itemClient).update(Mockito.eq(2L), Mockito.eq(1L), Mockito.argThat(item ->
                "Updated".equals(item.name()) && item.description() == null && item.available() == null));
    }

    @Test
    void updateShouldPreserveExplicitNullAsNoOp() throws Exception {
        Mockito.when(itemClient.update(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(UpdateItemRequest.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(MockMvcRequestBuilders.patch("/items/1")
                        .header(ItemController.X_SHARER_USER_ID, 2L)
                        .contentType("application/json")
                        .content("{\"name\":null}"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(itemClient).update(Mockito.eq(2L), Mockito.eq(1L), Mockito.argThat(item -> item.name() == null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"description\":\"Description\",\"available\":true}",
            "{\"name\":\"Name\",\"available\":true}",
            "{\"name\":\"Name\",\"description\":\"Description\"}",
            "{\"name\":\"\",\"description\":\"Description\",\"available\":true}",
            "{\"name\":\"Name\",\"description\":\"\",\"available\":true}",
            "{\"name\":\"   \",\"description\":\"Description\",\"available\":true}",
            "{\"name\":\"Name\",\"description\":\"   \",\"available\":true}"
    })
    void createShouldRejectInvalidFields(final String body) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/items")
                        .header(ItemController.X_SHARER_USER_ID, 2L)
                        .contentType("application/json")
                        .content(body))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verifyNoInteractions(itemClient);
    }

    @Test
    void createShouldAllowValidRequest() throws Exception {
        Mockito.when(itemClient.create(Mockito.anyLong(), Mockito.any(CreateItemRequest.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(MockMvcRequestBuilders.post("/items")
                        .header(ItemController.X_SHARER_USER_ID, 2L)
                        .contentType("application/json")
                        .content("{\"name\":\"Name\",\"description\":\"Description\",\"available\":true}"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(itemClient).create(Mockito.eq(2L), Mockito.any(CreateItemRequest.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"text\":\"\"}", "{\"text\":\"   \"}"})
    void createCommentShouldRejectInvalidText(final String body) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/items/1/comment")
                        .header(ItemController.X_SHARER_USER_ID, 2L)
                        .contentType("application/json")
                        .content(body))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verifyNoInteractions(itemClient);
    }
}
