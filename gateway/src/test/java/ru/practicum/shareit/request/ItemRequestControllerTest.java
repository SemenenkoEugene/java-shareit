package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {
    private static final long USER_ID = 2L;
    private static final long REQUEST_ID = 7L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemRequestClient itemRequestClient;

    @Test
    void createShouldForwardValidRequestAndReturnClientResponse() throws Exception {
        Mockito.when(itemRequestClient.create(Mockito.eq(USER_ID), Mockito.any(ItemRequestCreateDto.class)))
                .thenReturn(ResponseEntity.status(201)
                        .body(Map.of("id", REQUEST_ID, "description", "Need a drill")));

        mockMvc.perform(post("/requests")
                        .header(ItemRequestController.X_SHARER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Need a drill\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(REQUEST_ID))
                .andExpect(jsonPath("$.description").value("Need a drill"));

        Mockito.verify(itemRequestClient).create(Mockito.eq(USER_ID), Mockito.argThat(request ->
                "Need a drill".equals(request.getDescription())));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"description\":\"\"}", "{\"description\":\"   \"}"})
    void createShouldRejectMissingOrBlankDescription(final String body) throws Exception {
        mockMvc.perform(post("/requests")
                        .header(ItemRequestController.X_SHARER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(itemRequestClient);
    }

    @Test
    void getOwnRequestsShouldPassPaginationWithoutConversion() throws Exception {
        Mockito.when(itemRequestClient.getAllByOwnerId(USER_ID, 1, 2))
                .thenReturn(ResponseEntity.ok(Map.of("scope", "own")));

        mockMvc.perform(get("/requests")
                        .header(ItemRequestController.X_SHARER_USER_ID, USER_ID)
                        .param("from", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scope").value("own"));

        Mockito.verify(itemRequestClient).getAllByOwnerId(USER_ID, 1, 2);
    }

    @Test
    void getOwnRequestsShouldUsePaginationDefaults() throws Exception {
        Mockito.when(itemRequestClient.getAllByOwnerId(USER_ID, 0, 20))
                .thenReturn(ResponseEntity.ok(Map.of("scope", "own")));

        mockMvc.perform(get("/requests")
                        .header(ItemRequestController.X_SHARER_USER_ID, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scope").value("own"));

        Mockito.verify(itemRequestClient).getAllByOwnerId(USER_ID, 0, 20);
    }

    @Test
    void getOtherRequestsShouldPassPaginationWithoutConversion() throws Exception {
        Mockito.when(itemRequestClient.getAll(USER_ID, 1, 2))
                .thenReturn(ResponseEntity.ok(Map.of("scope", "all")));

        mockMvc.perform(get("/requests/all")
                        .header(ItemRequestController.X_SHARER_USER_ID, USER_ID)
                        .param("from", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scope").value("all"));

        Mockito.verify(itemRequestClient).getAll(USER_ID, 1, 2);
    }

    @Test
    void getOtherRequestsShouldUsePaginationDefaults() throws Exception {
        Mockito.when(itemRequestClient.getAll(USER_ID, 0, 20))
                .thenReturn(ResponseEntity.ok(Map.of("scope", "all")));

        mockMvc.perform(get("/requests/all")
                        .header(ItemRequestController.X_SHARER_USER_ID, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scope").value("all"));

        Mockito.verify(itemRequestClient).getAll(USER_ID, 0, 20);
    }

    @Test
    void getByIdShouldForwardUserAndRequestIds() throws Exception {
        Mockito.when(itemRequestClient.getById(USER_ID, REQUEST_ID))
                .thenReturn(ResponseEntity.ok(Map.of("id", REQUEST_ID, "description", "Need a drill")));

        mockMvc.perform(get("/requests/{id}", REQUEST_ID)
                        .header(ItemRequestController.X_SHARER_USER_ID, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(REQUEST_ID))
                .andExpect(jsonPath("$.description").value("Need a drill"));

        Mockito.verify(itemRequestClient).getById(USER_ID, REQUEST_ID);
    }

    @ParameterizedTest
    @CsvSource({
            "/requests,-1,20",
            "/requests,0,0",
            "/requests,0,-1",
            "/requests/all,-1,20",
            "/requests/all,0,0",
            "/requests/all,0,-1"
    })
    void getRequestsShouldRejectInvalidPagination(final String path, final int from, final int size)
            throws Exception {
        mockMvc.perform(get(path)
                        .header(ItemRequestController.X_SHARER_USER_ID, USER_ID)
                        .param("from", Integer.toString(from))
                        .param("size", Integer.toString(size)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isNotEmpty());

        Mockito.verifyNoInteractions(itemRequestClient);
    }
}
