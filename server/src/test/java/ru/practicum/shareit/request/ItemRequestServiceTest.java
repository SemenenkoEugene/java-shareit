package ru.practicum.shareit.request;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestGetResponseDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceTest {

    private static final Long ID_OWNER = 1L;
    private static final Long ID_REQUESTOR = 2L;
    private static final Long ID_ITEM_REQUEST = 3L;
    private static final Long ID_ITEM = 100L;

    private static final User OWNER = User.builder()
            .id(ID_OWNER)
            .name("User " + ID_OWNER)
            .email("user" + ID_OWNER + "@user.ru")
            .build();

    private static final User REQUESTOR = User.builder()
            .id(ID_REQUESTOR)
            .name("User " + ID_REQUESTOR)
            .email("user" + ID_REQUESTOR + "@user.ru")
            .build();

    private static final Item ITEM = Item.builder()
            .id(ID_ITEM)
            .name("Item " + ID_ITEM)
            .description("ItemDescription " + ID_ITEM)
            .available(true)
            .build();

    private static final ItemRequest ITEM_REQUEST = ItemRequest.builder()
            .id(ID_ITEM_REQUEST)
            .description("Request " + ID_ITEM_REQUEST)
            .build();

    private static final ItemRequestCreateDto ITEM_REQUEST_CREATE_DTO = ItemRequestCreateDto.builder().build();

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
        final ItemRequest itemRequest1 = ITEM_REQUEST.toBuilder()
                .requestor(REQUESTOR).build();

        final Item item = ITEM.toBuilder()
                .owner(OWNER)
                .itemRequest(itemRequest1)
                .build();

        Mockito.when(itemRequestRepository
                        .findAllByRequestorIdOrderByCreatedDesc(Mockito.anyLong(), Mockito.any()))
                .thenReturn(List.of(itemRequest1));
        Mockito.when(itemRepository.findAllByItemRequestIn(Mockito.any())).thenReturn(List.of(item));

        final List<ItemRequestGetResponseDto> resultDtoList = itemRequestService.getAllByRequestorId(REQUESTOR.getId(), 0, 10);

        Assertions.assertThat(resultDtoList).hasSize(1);

        final ItemRequestGetResponseDto actual = resultDtoList.getFirst();

        Assertions.assertThat(actual.getId()).isEqualTo(itemRequest1.getId());
        Assertions.assertThat(actual.getDescription()).isEqualTo(itemRequest1.getDescription());
        Assertions.assertThat(actual.getItems())
                .hasSize(1)
                .first()
                .satisfies(i -> {
                    Assertions.assertThat(i.getId()).isEqualTo(item.getId());
                    Assertions.assertThat(i.getName()).isEqualTo(item.getName());
                    Assertions.assertThat(i.getDescription()).isEqualTo(item.getDescription());
                    Assertions.assertThat(i.getAvailable()).isEqualTo(item.getAvailable());
                    Assertions.assertThat(i.getRequestId()).isEqualTo(item.getItemRequest().getId());
                });

        Mockito.verify(itemRequestRepository)
                .findAllByRequestorIdOrderByCreatedDesc(Mockito.eq(REQUESTOR.getId()), Mockito.any(Pageable.class));
        Mockito.verify(itemRepository).findAllByItemRequestIn(List.of(itemRequest1));
        Mockito.verifyNoMoreInteractions(itemRequestRepository, userRepository, itemRepository);
    }

    @Test
    void getAllTest() {
        final ItemRequest itemRequest1 = ITEM_REQUEST.toBuilder()
                .requestor(REQUESTOR).build();

        final Item item = ITEM.toBuilder()
                .owner(OWNER)
                .itemRequest(itemRequest1)
                .build();

        Mockito.when(itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(Mockito.anyLong(), Mockito.any()))
                .thenReturn(List.of(itemRequest1));
        Mockito.when(itemRepository.findAllByItemRequestId(Mockito.anyLong()))
                .thenReturn(List.of(item));

        final List<ItemRequestGetResponseDto> actual = itemRequestService.getAll(OWNER.getId(), 0, 10);

        Assertions.assertThat(actual).hasSize(1);

        final ItemRequestGetResponseDto dto = actual.getFirst();

        Assertions.assertThat(dto.getId()).isEqualTo(itemRequest1.getId());
        Assertions.assertThat(dto.getDescription()).isEqualTo(itemRequest1.getDescription());
        Assertions.assertThat(dto.getItems())
                .hasSize(1)
                .first()
                .satisfies(i -> {
                    Assertions.assertThat(i.getId()).isEqualTo(item.getId());
                    Assertions.assertThat(i.getName()).isEqualTo(item.getName());
                    Assertions.assertThat(i.getDescription()).isEqualTo(item.getDescription());
                    Assertions.assertThat(i.getAvailable()).isEqualTo(item.getAvailable());
                    Assertions.assertThat(i.getRequestId()).isEqualTo(item.getItemRequest().getId());
                });

        Mockito.verify(itemRequestRepository)
                .findAllByRequestorIdNotOrderByCreatedDesc(Mockito.eq(OWNER.getId()), Mockito.any(Pageable.class));
        Mockito.verify(itemRepository).findAllByItemRequestId(itemRequest1.getId());
        Mockito.verifyNoMoreInteractions(itemRequestRepository, userRepository, itemRepository);
    }

    @Test
    void getByIdTest() {
        final ItemRequest itemRequest1 = ITEM_REQUEST.toBuilder()
                .requestor(REQUESTOR).build();

        final Item item = ITEM.toBuilder()
                .owner(OWNER)
                .itemRequest(itemRequest1)
                .build();

        Mockito.when(itemRequestRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(ITEM_REQUEST));
        Mockito.when(itemRepository.findAllByItemRequestId(Mockito.anyLong())).thenReturn(List.of(item));

        final ItemRequestGetResponseDto actual = itemRequestService.getById(REQUESTOR.getId(), ITEM_REQUEST.getId());

        Assertions.assertThat(actual.getId()).isEqualTo(ITEM_REQUEST.getId());
        Assertions.assertThat(actual.getDescription()).isEqualTo(ITEM_REQUEST.getDescription());
        Assertions.assertThat(actual.getItems())
                .hasSize(1)
                .first()
                .satisfies(i -> {
                    Assertions.assertThat(i.getId()).isEqualTo(item.getId());
                    Assertions.assertThat(i.getName()).isEqualTo(item.getName());
                    Assertions.assertThat(i.getDescription()).isEqualTo(item.getDescription());
                    Assertions.assertThat(i.getAvailable()).isEqualTo(item.getAvailable());
                    Assertions.assertThat(i.getRequestId()).isEqualTo(item.getItemRequest().getId());
                });

        Mockito.verify(itemRequestRepository).findById(ITEM_REQUEST.getId());
        Mockito.verify(itemRepository).findAllByItemRequestId(ITEM_REQUEST.getId());
        Mockito.verifyNoMoreInteractions(itemRequestRepository, userRepository, itemRepository);
    }

    @Test
    void createTest() {
        Mockito.when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(OWNER));
        Mockito.when(itemRequestRepository.save(Mockito.any())).thenReturn(ITEM_REQUEST);

        final ItemRequestCreateResponseDto actual = itemRequestService.create(ITEM_REQUEST_CREATE_DTO, OWNER.getId());

        Assertions.assertThat(actual.getId()).isEqualTo(ITEM_REQUEST.getId());
        Assertions.assertThat(actual.getDescription()).isEqualTo(ITEM_REQUEST.getDescription());

        Mockito.verify(userRepository).findById(OWNER.getId());
        Mockito.verify(itemRequestRepository).save(Mockito.any(ItemRequest.class));
        Mockito.verifyNoMoreInteractions(itemRequestRepository, userRepository, itemRepository);
    }
}
