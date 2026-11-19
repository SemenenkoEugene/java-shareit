package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.shareit.generated.request.dto.CreateItemRequestRequest;
import ru.practicum.shareit.generated.request.dto.ItemRequestCreateResponse;
import ru.practicum.shareit.generated.request.dto.ItemRequestResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    private static final Long USER_ID = 1L;
    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";

    private static final ItemRequestResponse ITEM_REQUEST_RESPONSE = ItemRequestResponse.builder()
            .id(10L)
            .build();
    private static final List<ItemRequestResponse> ITEM_REQUEST_RESPONSE_LIST = List.of(ITEM_REQUEST_RESPONSE);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ItemRequestService itemRequestService;

    @SneakyThrows
    @Test
    void getAllByOwnerIdTest() {

        Mockito.when(itemRequestService.getAllByRequestorId(Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(ITEM_REQUEST_RESPONSE_LIST);

        mockMvc.perform(MockMvcRequestBuilders.get("/requests")
                        .header(X_SHARER_USER_ID, USER_ID))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(ITEM_REQUEST_RESPONSE.id()));

        Mockito.verify(itemRequestService).getAllByRequestorId(Mockito.eq(USER_ID), Mockito.anyInt(), Mockito.anyInt());
        Mockito.verifyNoMoreInteractions(itemRequestService);
    }

    @SneakyThrows
    @Test
    void getAllTest() {

        Mockito.when(itemRequestService.getAll(Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(ITEM_REQUEST_RESPONSE_LIST);

        mockMvc.perform(MockMvcRequestBuilders.get("/requests/all")
                        .header(X_SHARER_USER_ID, USER_ID))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(ITEM_REQUEST_RESPONSE.id()));

        Mockito.verify(itemRequestService).getAll(Mockito.eq(USER_ID), Mockito.anyInt(), Mockito.anyInt());
        Mockito.verifyNoMoreInteractions(itemRequestService);
    }

    @SneakyThrows
    @Test
    void getByIdTest() {

        Mockito.when(itemRequestService.getById(Mockito.anyLong(), Mockito.anyLong())).thenReturn(ITEM_REQUEST_RESPONSE);

        mockMvc.perform(MockMvcRequestBuilders.get("/requests/" + ITEM_REQUEST_RESPONSE.id())
                        .header(X_SHARER_USER_ID, USER_ID))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(ITEM_REQUEST_RESPONSE.id()));

        Mockito.verify(itemRequestService).getById(Mockito.eq(USER_ID), Mockito.anyLong());
        Mockito.verifyNoMoreInteractions(itemRequestService);
    }

    @SneakyThrows
    @Test
    void createTest() {

        final CreateItemRequestRequest requestDto = new CreateItemRequestRequest("Test item request");

        final ItemRequestCreateResponse responseDto = ItemRequestCreateResponse.builder()
                .id(1L)
                .build();

        Mockito.when(itemRequestService.create(Mockito.any(), Mockito.anyLong())).thenReturn(responseDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/requests")
                        .header(X_SHARER_USER_ID, USER_ID)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(responseDto.id()));

        Mockito.verify(itemRequestService).create(Mockito.eq(requestDto), Mockito.eq(USER_ID));
        Mockito.verifyNoMoreInteractions(itemRequestService);
    }

}
