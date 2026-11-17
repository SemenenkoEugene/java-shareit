package ru.practicum.shareit.request;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.request.dto.ItemRequestGetResponseDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@DataJpaTest
@Import(ItemRequestServiceImpl.class)
class ItemRequestServiceQueryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private ItemRequestService itemRequestService;

    @ParameterizedTest
    @MethodSource("paginationArguments")
    void getAllByRequestorIdShouldUseFromAsOffset(final int from, final int size,
                                                   final List<Integer> expectedIndexes) {
        final User requestor = persistUser("requestor@test.ru");
        final List<ItemRequest> requests = persistRequests(requestor);

        final List<ItemRequestGetResponseDto> result =
                itemRequestService.getAllByRequestorId(requestor.getId(), from, size);

        Assertions.assertThat(result).extracting(ItemRequestGetResponseDto::getId)
                .containsExactlyElementsOf(idsAtIndexes(requests, expectedIndexes));
    }

    @ParameterizedTest
    @MethodSource("paginationArguments")
    void getAllShouldUseFromAsOffset(final int from, final int size,
                                     final List<Integer> expectedIndexes) {
        final User requestor = persistUser("another-requestor@test.ru");
        final User requester = persistUser("requester@test.ru");
        persistRequest(requester, "Own request", LocalDateTime.of(2040, 1, 1, 12, 0));
        final List<ItemRequest> requests = persistRequests(requestor);

        final List<ItemRequestGetResponseDto> result = itemRequestService.getAll(requester.getId(), from, size);

        Assertions.assertThat(result).extracting(ItemRequestGetResponseDto::getId)
                .containsExactlyElementsOf(idsAtIndexes(requests, expectedIndexes));
    }

    private User persistUser(final String email) {
        return testEntityManager.persist(User.builder()
                .name("User")
                .email(email)
                .build());
    }

    private List<ItemRequest> persistRequests(final User requestor) {
        final LocalDateTime created = LocalDateTime.of(2030, 1, 1, 12, 0);
        final List<ItemRequest> requests = new ArrayList<>();
        for (int index = 0; index < 5; index++) {
            requests.add(persistRequest(requestor, "Request " + index, created.minusMinutes(index)));
        }
        testEntityManager.flush();
        return requests;
    }

    private ItemRequest persistRequest(final User requestor, final String description,
                                       final LocalDateTime created) {
        return testEntityManager.persist(ItemRequest.builder()
                .description(description)
                .requestor(requestor)
                .created(created)
                .build());
    }

    private List<Long> idsAtIndexes(final List<ItemRequest> requests, final List<Integer> indexes) {
        return indexes.stream().map(index -> requests.get(index).getId()).toList();
    }

    private static Stream<Arguments> paginationArguments() {
        return Stream.of(
                Arguments.of(0, 2, List.of(0, 1)),
                Arguments.of(1, 2, List.of(1, 2)),
                Arguments.of(3, 2, List.of(3, 4)),
                Arguments.of(10, 2, List.of())
        );
    }
}
