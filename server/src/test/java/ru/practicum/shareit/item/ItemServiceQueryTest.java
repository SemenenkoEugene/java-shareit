package ru.practicum.shareit.item;

import jakarta.persistence.EntityManagerFactory;
import org.assertj.core.api.Assertions;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@DataJpaTest
@Import(ItemServiceImpl.class)
class ItemServiceQueryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private ItemService itemService;

    @ParameterizedTest
    @MethodSource("paginationArguments")
    void getItemsByOwnerIdShouldUseFromAsOffset(final int from, final int size,
                                                 final List<Integer> expectedIndexes) {
        final User owner = persistOwner("owner-items@test.ru");
        final List<Item> items = persistItems(owner, "Owner item");

        final List<ItemDto> result = itemService.getItemsByOwnerId(owner.getId(), from, size);

        Assertions.assertThat(result).extracting(ItemDto::getId)
                .containsExactlyElementsOf(idsAtIndexes(items, expectedIndexes));
    }

    @ParameterizedTest
    @MethodSource("paginationArguments")
    void getItemsBySearchQueryShouldUseFromAsOffset(final int from, final int size,
                                                     final List<Integer> expectedIndexes) {
        final User owner = persistOwner("owner-search@test.ru");
        final List<Item> items = persistItems(owner, "Searchable item");

        final List<ItemDto> result = itemService.getItemsBySearchQuery("Searchable", from, size);

        Assertions.assertThat(result).extracting(ItemDto::getId)
                .containsExactlyElementsOf(idsAtIndexes(items, expectedIndexes));
    }

    @Test
    void getItemsByOwnerIdShouldUseConstantNumberOfSqlStatements() {
        final User owner = testEntityManager.persist(User.builder()
                .name("Owner")
                .email("owner@test.ru")
                .build());
        final List<Item> items = new ArrayList<>();
        for (int index = 1; index <= 5; index++) {
            final ItemRequest itemRequest = testEntityManager.persist(ItemRequest.builder()
                    .description("Request " + index)
                    .requestor(owner)
                    .created(LocalDateTime.now())
                    .build());
            items.add(testEntityManager.persist(Item.builder()
                    .name("Item " + index)
                    .description("Description " + index)
                    .available(true)
                    .owner(owner)
                    .itemRequest(itemRequest)
                    .build()));
        }
        final User booker = testEntityManager.persist(User.builder()
                .name("Booker")
                .email("booker@test.ru")
                .build());
        testEntityManager.persist(Booking.builder()
                .start(LocalDateTime.now().minusHours(2))
                .end(LocalDateTime.now().minusHours(1))
                .item(items.getFirst())
                .user(booker)
                .status(Status.APPROVED)
                .build());
        testEntityManager.persist(Comment.builder()
                .text("Comment")
                .item(items.getFirst())
                .author(booker)
                .created(LocalDateTime.now())
                .build());
        testEntityManager.flush();

        final Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);

        final long statementsForOneItem = countStatements(statistics, owner.getId(), 1);
        final long statementsForThreeItems = countStatements(statistics, owner.getId(), 3);
        final long statementsForFiveItems = countStatements(statistics, owner.getId(), 5);

        Assertions.assertThat(List.of(statementsForOneItem, statementsForThreeItems, statementsForFiveItems))
                .containsExactly(3L, 3L, 3L);
    }

    private long countStatements(final Statistics statistics, final Long ownerId, final int size) {
        testEntityManager.clear();
        statistics.clear();

        final List<ItemDto> result = itemService.getItemsByOwnerId(ownerId, 0, size);

        Assertions.assertThat(result).hasSize(size);
        Assertions.assertThat(result).extracting(ItemDto::getId).isSorted();
        Assertions.assertThat(result).extracting(ItemDto::getRequestId).doesNotContainNull();

        return statistics.getPrepareStatementCount();
    }

    private User persistOwner(final String email) {
        return testEntityManager.persist(User.builder()
                .name("Owner")
                .email(email)
                .build());
    }

    private List<Item> persistItems(final User owner, final String namePrefix) {
        final List<Item> items = new ArrayList<>();
        for (int index = 1; index <= 5; index++) {
            items.add(testEntityManager.persist(Item.builder()
                    .name(namePrefix + " " + index)
                    .description("Description " + index)
                    .available(true)
                    .owner(owner)
                    .build()));
        }
        testEntityManager.flush();
        return items;
    }

    private List<Long> idsAtIndexes(final List<Item> items, final List<Integer> indexes) {
        return indexes.stream().map(index -> items.get(index).getId()).toList();
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
