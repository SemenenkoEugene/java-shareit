package ru.practicum.shareit.request;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.generated.request.dto.CreateItemRequestRequest;
import ru.practicum.shareit.generated.request.dto.ItemRequestCreateResponse;
import ru.practicum.shareit.generated.request.dto.ItemRequestResponse;
import ru.practicum.shareit.generated.request.dto.RequestedItemResponse;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
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

    private static final CreateItemRequestRequest ITEM_REQUEST_CREATE_DTO =
            CreateItemRequestRequest.builder().build();

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
                        .findAllByRequestorIdOrderByCreatedDescIdDesc(Mockito.anyLong(), Mockito.any()))
                .thenReturn(List.of(itemRequest1));
        Mockito.when(itemRepository.findAllByItemRequestIn(Mockito.any())).thenReturn(List.of(item));

        final List<ItemRequestResponse> resultDtoList =
                itemRequestService.getAllByRequestorId(REQUESTOR.getId(), 0, 10);

        Assertions.assertThat(resultDtoList).hasSize(1);

        final ItemRequestResponse actual = resultDtoList.getFirst();

        Assertions.assertThat(actual.id()).isEqualTo(itemRequest1.getId());
        Assertions.assertThat(actual.description()).isEqualTo(itemRequest1.getDescription());
        Assertions.assertThat(actual.items())
                .hasSize(1)
                .first()
                .satisfies(i -> {
                    Assertions.assertThat(i.id()).isEqualTo(item.getId());
                    Assertions.assertThat(i.name()).isEqualTo(item.getName());
                    Assertions.assertThat(i.description()).isEqualTo(item.getDescription());
                    Assertions.assertThat(i.available()).isEqualTo(item.getAvailable());
                    Assertions.assertThat(i.requestId()).isEqualTo(item.getItemRequest().getId());
                });

        Mockito.verify(itemRequestRepository)
                .findAllByRequestorIdOrderByCreatedDescIdDesc(
                        Mockito.eq(REQUESTOR.getId()), Mockito.any(Pageable.class));
        Mockito.verify(itemRepository).findAllByItemRequestIn(List.of(itemRequest1));
        Mockito.verifyNoMoreInteractions(itemRequestRepository, userRepository, itemRepository);
    }

    @Test
    void getAllShouldLoadItemsWithSingleRepositoryCall() {
        final ItemRequest itemRequest1 = ITEM_REQUEST.toBuilder()
                .requestor(REQUESTOR).build();
        final ItemRequest itemRequest2 = ITEM_REQUEST.toBuilder()
                .id(ID_ITEM_REQUEST + 1)
                .requestor(REQUESTOR).build();

        final Item item1 = ITEM.toBuilder()
                .owner(OWNER)
                .itemRequest(itemRequest1)
                .build();
        final Item item2 = ITEM.toBuilder()
                .id(ID_ITEM + 1)
                .owner(OWNER)
                .itemRequest(itemRequest2)
                .build();
        final Item item3 = ITEM.toBuilder()
                .id(ID_ITEM + 2)
                .owner(OWNER)
                .itemRequest(itemRequest1)
                .build();

        Mockito.when(userRepository.findById(OWNER.getId())).thenReturn(Optional.of(OWNER));
        Mockito.when(itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDescIdDesc(
                        Mockito.anyLong(), Mockito.any()))
                .thenReturn(List.of(itemRequest1, itemRequest2));
        Mockito.when(itemRepository.findAllByItemRequestIn(Mockito.any()))
                .thenReturn(List.of(item1, item2, item3));

        final List<ItemRequestResponse> actual = itemRequestService.getAll(OWNER.getId(), 0, 10);

        Assertions.assertThat(actual).extracting(ItemRequestResponse::id)
                .containsExactly(itemRequest1.getId(), itemRequest2.getId());
        Assertions.assertThat(actual.get(0).items()).extracting(RequestedItemResponse::id)
                .containsExactly(item1.getId(), item3.getId());
        Assertions.assertThat(actual.get(1).items()).extracting(RequestedItemResponse::id)
                .containsExactly(item2.getId());

        Mockito.verify(itemRequestRepository)
                .findAllByRequestorIdNotOrderByCreatedDescIdDesc(
                        Mockito.eq(OWNER.getId()), Mockito.any(Pageable.class));
        Mockito.verify(userRepository).findById(OWNER.getId());
        Mockito.verify(itemRepository).findAllByItemRequestIn(List.of(itemRequest1, itemRequest2));
        Mockito.verifyNoMoreInteractions(itemRequestRepository, userRepository, itemRepository);
    }

    @Test
    void getAllShouldReturnEmptyItemsForRequestWithoutItems() {
        final ItemRequest itemRequest = ITEM_REQUEST.toBuilder().requestor(REQUESTOR).build();
        Mockito.when(userRepository.findById(OWNER.getId())).thenReturn(Optional.of(OWNER));
        Mockito.when(itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDescIdDesc(
                        Mockito.anyLong(), Mockito.any()))
                .thenReturn(List.of(itemRequest));
        Mockito.when(itemRepository.findAllByItemRequestIn(Mockito.any())).thenReturn(List.of());

        final List<ItemRequestResponse> actual = itemRequestService.getAll(OWNER.getId(), 0, 10);

        Assertions.assertThat(actual).singleElement()
                .satisfies(dto -> Assertions.assertThat(dto.items()).isEmpty());
        Mockito.verify(itemRepository).findAllByItemRequestIn(List.of(itemRequest));
    }

    @Test
    void getAllShouldNotLoadItemsForEmptyPage() {
        Mockito.when(userRepository.findById(OWNER.getId())).thenReturn(Optional.of(OWNER));
        Mockito.when(itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDescIdDesc(
                        Mockito.anyLong(), Mockito.any()))
                .thenReturn(List.of());

        final List<ItemRequestResponse> actual = itemRequestService.getAll(OWNER.getId(), 0, 10);

        Assertions.assertThat(actual).isEmpty();
        Mockito.verify(userRepository).findById(OWNER.getId());
        Mockito.verify(itemRequestRepository).findAllByRequestorIdNotOrderByCreatedDescIdDesc(
                Mockito.eq(OWNER.getId()), Mockito.any(Pageable.class));
        Mockito.verifyNoInteractions(itemRepository);
    }

    @Test
    void getAllShouldRejectUnknownUserWithoutLoadingRequests() {
        Mockito.when(userRepository.findById(OWNER.getId())).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> itemRequestService.getAll(OWNER.getId(), 0, 10))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь не найден");

        Mockito.verify(userRepository).findById(OWNER.getId());
        Mockito.verifyNoInteractions(itemRequestRepository, itemRepository);
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getAllShouldPreserveOffsetPaginationAndRepositorySorting() {
        final int from = 3;
        final int size = 2;
        Mockito.when(userRepository.findById(OWNER.getId())).thenReturn(Optional.of(OWNER));
        Mockito.when(itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDescIdDesc(
                        Mockito.eq(OWNER.getId()), Mockito.any(Pageable.class)))
                .thenReturn(List.of());

        final List<ItemRequestResponse> actual = itemRequestService.getAll(OWNER.getId(), from, size);

        Assertions.assertThat(actual).isEmpty();
        final org.mockito.ArgumentCaptor<Pageable> pageableCaptor =
                org.mockito.ArgumentCaptor.forClass(Pageable.class);
        Mockito.verify(itemRequestRepository).findAllByRequestorIdNotOrderByCreatedDescIdDesc(
                Mockito.eq(OWNER.getId()), pageableCaptor.capture());
        Assertions.assertThat(pageableCaptor.getValue().getOffset()).isEqualTo(from);
        Assertions.assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(size);
        Mockito.verify(userRepository).findById(OWNER.getId());
        Mockito.verifyNoInteractions(itemRepository);
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

        final ItemRequestResponse actual = itemRequestService.getById(REQUESTOR.getId(), ITEM_REQUEST.getId());

        Assertions.assertThat(actual.id()).isEqualTo(ITEM_REQUEST.getId());
        Assertions.assertThat(actual.description()).isEqualTo(ITEM_REQUEST.getDescription());
        Assertions.assertThat(actual.items())
                .hasSize(1)
                .first()
                .satisfies(i -> {
                    Assertions.assertThat(i.id()).isEqualTo(item.getId());
                    Assertions.assertThat(i.name()).isEqualTo(item.getName());
                    Assertions.assertThat(i.description()).isEqualTo(item.getDescription());
                    Assertions.assertThat(i.available()).isEqualTo(item.getAvailable());
                    Assertions.assertThat(i.requestId()).isEqualTo(item.getItemRequest().getId());
                });

        Mockito.verify(itemRequestRepository).findById(ITEM_REQUEST.getId());
        Mockito.verify(itemRepository).findAllByItemRequestId(ITEM_REQUEST.getId());
        Mockito.verifyNoMoreInteractions(itemRequestRepository, userRepository, itemRepository);
    }

    @Test
    void createTest() {
        Mockito.when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(OWNER));
        Mockito.when(itemRequestRepository.save(Mockito.any())).thenReturn(ITEM_REQUEST);

        final ItemRequestCreateResponse actual = itemRequestService.create(ITEM_REQUEST_CREATE_DTO, OWNER.getId());

        Assertions.assertThat(actual.id()).isEqualTo(ITEM_REQUEST.getId());
        Assertions.assertThat(actual.description()).isEqualTo(ITEM_REQUEST.getDescription());

        Mockito.verify(userRepository).findById(OWNER.getId());
        Mockito.verify(itemRequestRepository).save(Mockito.any(ItemRequest.class));
        Mockito.verifyNoMoreInteractions(itemRequestRepository, userRepository, itemRepository);
    }
}
