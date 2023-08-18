package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestGetResponseDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;
    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @Test
    void getAllByRequestorIdTest() {
        User owner = getUser(1L);
        User requestor = getUser(2L);

        ItemRequest itemRequest1 = getItemRequest(10L);
        itemRequest1.setRequestor(requestor);

        ItemRequest itemRequest2 = getItemRequest(11L);
        itemRequest2.setRequestor(requestor);

        Item item1 = getItem(100L);
        item1.setOwner(owner);
        item1.setItemRequest(itemRequest1);

        Item item2 = getItem(101L);
        item2.setOwner(owner);
        item2.setItemRequest(itemRequest2);

        List<ItemRequest> itemRequestList = Arrays.asList(
                itemRequest1,
                itemRequest2
        );
        List<Item> items = Arrays.asList(
                item1,
                item2
        );

        when(userRepository.findById(requestor.getId()))
                .thenReturn(Optional.of(requestor));
        when(itemRequestRepository
                .findAllByRequestorIdOrderByCreatedDesc(eq(requestor.getId()), any(Pageable.class)))
                .thenReturn(itemRequestList);
        when(itemRepository.findAllByItemRequestIn(itemRequestList))
                .thenReturn(items);

        List<ItemRequestGetResponseDto> resultDtoList = itemRequestService
                .getAllByRequestorId(requestor.getId(), 0, 10);

        assertThat(resultDtoList.size(), equalTo(2));

        assertThat(resultDtoList.get(0).getId(), equalTo(itemRequest1.getId()));
        assertThat(resultDtoList.get(0).getDescription(), equalTo(itemRequest1.getDescription()));
        assertThat(resultDtoList.get(0).getItems().size(), equalTo(1));
        assertThat(resultDtoList.get(0).getItems().get(0).getId(), equalTo(item1.getId()));
        assertThat(resultDtoList.get(0).getItems().get(0).getName(), equalTo(item1.getName()));
        assertThat(resultDtoList.get(0).getItems().get(0).getDescription(), equalTo(item1.getDescription()));
        assertThat(resultDtoList.get(0).getItems().get(0).getAvailable(), equalTo(item1.getAvailable()));
        assertThat(resultDtoList.get(0).getItems().get(0).getRequestId(), equalTo(item1.getItemRequest().getId()));

        assertThat(resultDtoList.get(1).getId(), equalTo(itemRequest2.getId()));
        assertThat(resultDtoList.get(1).getDescription(), equalTo(itemRequest2.getDescription()));
        assertThat(resultDtoList.get(1).getItems().size(), equalTo(1));
        assertThat(resultDtoList.get(1).getItems().get(0).getId(), equalTo(item2.getId()));
        assertThat(resultDtoList.get(1).getItems().get(0).getName(), equalTo(item2.getName()));
        assertThat(resultDtoList.get(1).getItems().get(0).getDescription(), equalTo(item2.getDescription()));
        assertThat(resultDtoList.get(1).getItems().get(0).getAvailable(), equalTo(item2.getAvailable()));
        assertThat(resultDtoList.get(1).getItems().get(0).getRequestId(), equalTo(item2.getItemRequest().getId()));

        verify(userRepository, times(1)).findById(requestor.getId());
        verify(itemRequestRepository, times(1))
                .findAllByRequestorIdOrderByCreatedDesc(eq(requestor.getId()), any(Pageable.class));
        verify(itemRepository, times(1)).findAllByItemRequestIn(itemRequestList);
        verifyNoMoreInteractions(itemRequestRepository, userRepository, itemRepository);
    }

    @Test
    void getAllTest() {
        User owner = getUser(1L);
        User requestor = getUser(2L);

        ItemRequest itemRequest1 = getItemRequest(10L);
        itemRequest1.setRequestor(requestor);

        ItemRequest itemRequest2 = getItemRequest(11L);
        itemRequest2.setRequestor(requestor);

        Item item1 = getItem(100L);
        item1.setOwner(owner);
        item1.setItemRequest(itemRequest1);

        Item item2 = getItem(101L);
        item2.setOwner(owner);
        item2.setItemRequest(itemRequest2);

        List<ItemRequest> itemRequestList = Arrays.asList(
                itemRequest1,
                itemRequest2
        );

        when(itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(eq(owner.getId()), any(Pageable.class)))
                .thenReturn(itemRequestList);
        when(itemRepository.findAllByItemRequestId(itemRequest1.getId()))
                .thenReturn(List.of(item1));
        when(itemRepository.findAllByItemRequestId(itemRequest2.getId()))
                .thenReturn(List.of(item2));

        List<ItemRequestGetResponseDto> resultDtoList = itemRequestService.getAll(owner.getId(), 0, 10);

        assertThat(resultDtoList.size(), equalTo(2));

        assertThat(resultDtoList.get(0).getId(), equalTo(itemRequest1.getId()));
        assertThat(resultDtoList.get(0).getDescription(), equalTo(itemRequest1.getDescription()));
        assertThat(resultDtoList.get(0).getItems().size(), equalTo(1));
        assertThat(resultDtoList.get(0).getItems().get(0).getId(), equalTo(item1.getId()));
        assertThat(resultDtoList.get(0).getItems().get(0).getName(), equalTo(item1.getName()));
        assertThat(resultDtoList.get(0).getItems().get(0).getDescription(), equalTo(item1.getDescription()));
        assertThat(resultDtoList.get(0).getItems().get(0).getAvailable(), equalTo(item1.getAvailable()));
        assertThat(resultDtoList.get(0).getItems().get(0).getRequestId(), equalTo(item1.getItemRequest().getId()));

        assertThat(resultDtoList.get(1).getId(), equalTo(itemRequest2.getId()));
        assertThat(resultDtoList.get(1).getDescription(), equalTo(itemRequest2.getDescription()));
        assertThat(resultDtoList.get(1).getItems().size(), equalTo(1));
        assertThat(resultDtoList.get(1).getItems().get(0).getId(), equalTo(item2.getId()));
        assertThat(resultDtoList.get(1).getItems().get(0).getName(), equalTo(item2.getName()));
        assertThat(resultDtoList.get(1).getItems().get(0).getDescription(), equalTo(item2.getDescription()));
        assertThat(resultDtoList.get(1).getItems().get(0).getAvailable(), equalTo(item2.getAvailable()));
        assertThat(resultDtoList.get(1).getItems().get(0).getRequestId(), equalTo(item2.getItemRequest().getId()));

        verify(itemRequestRepository, times(1))
                .findAllByRequestorIdNotOrderByCreatedDesc(eq(owner.getId()), any(Pageable.class));
        verify(itemRepository, times(1)).findAllByItemRequestId(itemRequest1.getId());
        verify(itemRepository, times(1)).findAllByItemRequestId(itemRequest2.getId());
        verifyNoMoreInteractions(itemRequestRepository, userRepository, itemRepository);
    }

    @Test
    void getByIdTest() {
        User owner = getUser(1L);
        User requestor = getUser(2L);

        ItemRequest itemRequest = getItemRequest(10L);
        itemRequest.setRequestor(requestor);

        Item item = getItem(100L);
        item.setOwner(owner);
        item.setItemRequest(itemRequest);

        when(userRepository.findById(requestor.getId()))
                .thenReturn(Optional.of(requestor));
        when(itemRequestRepository.findById(itemRequest.getId()))
                .thenReturn(Optional.of(itemRequest));
        when(itemRepository.findAllByItemRequestId(itemRequest.getId()))
                .thenReturn(List.of(item));

        ItemRequestGetResponseDto resultDto = itemRequestService.getById(requestor.getId(), itemRequest.getId());

        assertThat(resultDto.getId(), equalTo(itemRequest.getId()));
        assertThat(resultDto.getDescription(), equalTo(itemRequest.getDescription()));
        assertThat(resultDto.getItems().size(), equalTo(1));
        assertThat(resultDto.getItems().get(0).getId(), equalTo(item.getId()));
        assertThat(resultDto.getItems().get(0).getName(), equalTo(item.getName()));
        assertThat(resultDto.getItems().get(0).getDescription(), equalTo(item.getDescription()));
        assertThat(resultDto.getItems().get(0).getAvailable(), equalTo(item.getAvailable()));
        assertThat(resultDto.getItems().get(0).getRequestId(), equalTo(item.getItemRequest().getId()));

        verify(userRepository, times(1)).findById(requestor.getId());
        verify(itemRequestRepository, times(1)).findById(itemRequest.getId());
        verify(itemRepository, times(1)).findAllByItemRequestId(itemRequest.getId());
        verifyNoMoreInteractions(itemRequestRepository, userRepository, itemRepository);
    }

    @Test
    void createTest() {
        User user = getUser(1L);
        ItemRequest itemRequest = getItemRequest(10L);

        ItemRequestCreateDto itemRequestCreateDto = ItemRequestCreateDto.builder().build();

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(itemRequestRepository.save(any(ItemRequest.class)))
                .thenReturn(itemRequest);

        ItemRequestCreateResponseDto resultDto = itemRequestService.create(itemRequestCreateDto, user.getId());

        assertThat(resultDto.getId(), equalTo(itemRequest.getId()));
        assertThat(resultDto.getDescription(), equalTo(itemRequest.getDescription()));

        verify(userRepository, times(1)).findById(user.getId());
        verify(itemRequestRepository, times(1)).save(any(ItemRequest.class));
        verifyNoMoreInteractions(itemRequestRepository, userRepository, itemRepository);
    }

    private ItemRequest getItemRequest(Long id) {
        return ItemRequest.builder()
                .id(id)
                .description("Request " + id)
                .build();
    }

    private User getUser(Long id) {
        return User.builder()
                .id(id)
                .name("User " + id)
                .email("user" + id + "@user.ru")
                .build();
    }

    private Item getItem(Long id) {
        return Item.builder()
                .id(id)
                .name("Item " + id)
                .description("ItemDescription " + id)
                .available(true)
                .build();
    }
}