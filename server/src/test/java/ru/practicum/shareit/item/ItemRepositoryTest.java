package ru.practicum.shareit.item;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

@DataJpaTest
@Transactional
class ItemRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void testFindBySearchText() {
        final User owner = testEntityManager.persist(User.builder()
                .name("Owner")
                .email("owner@user.ru")
                .build());

        testEntityManager.persist(Item.builder()
                .name("Гимнастический обруч")
                .description("Обычный")
                .available(true)
                .owner(owner)
                .build());

        testEntityManager.persist(Item.builder()
                .name("Гимнастический обруч ПРО")
                .description("Профессиональный")
                .available(false)
                .owner(owner)
                .build());

        testEntityManager.persist(Item.builder()
                .name("Гимнастический обруч ПРО+")
                .description("Очень профессиональный")
                .available(true)
                .owner(owner)
                .build());

        testEntityManager.persist(Item.builder()
                .name("Гимнастический обруч ПРО++")
                .description("Обруч для очень супер профессионалов")
                .available(true)
                .owner(owner)
                .build());

        final Pageable pageable = PageRequest.of(0, 10);

        Assertions.assertThat(itemRepository.findBySearchText("МяЧ", pageable)).size().isEqualTo(0);
        Assertions.assertThat(itemRepository.findBySearchText("Про", pageable)).size().isEqualTo(3);
        Assertions.assertThat(itemRepository.findBySearchText("профессионал", pageable)).size().isEqualTo(2);
    }
}