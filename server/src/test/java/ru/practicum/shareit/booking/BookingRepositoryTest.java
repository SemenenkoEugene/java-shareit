package ru.practicum.shareit.booking;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@DataJpaTest
@Transactional
class BookingRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void findByItemIdAndUserIdAndExpiredEndDateAndApprovedStatusTest() {
        final LocalDateTime timestamp = LocalDateTime.now();

        final User owner = testEntityManager.persist(User.builder()
                .name("Owner")
                .email("owner@user.ru")
                .build());

        final User booker1 = testEntityManager.persist(User.builder()
                .name("Booker1")
                .email("booker1@user.ru")
                .build());

        final User booker2 = testEntityManager.persist(User.builder()
                .name("Booker2")
                .email("booker2@user.ru")
                .build());

        final Item item1 = testEntityManager.persist(Item.builder()
                .name("Item1")
                .description("Item1")
                .available(true)
                .owner(owner)
                .build());

        final Item item2 = testEntityManager.persist(Item.builder()
                .name("Item2")
                .description("Item2")
                .available(true)
                .owner(owner)
                .build());

        testEntityManager.persist(Booking.builder()
                .start(timestamp.minusDays(1))
                .end(timestamp.minusHours(1))
                .item(item1)
                .user(booker1)
                .status(Status.APPROVED)
                .build());

        Assertions.assertThat(bookingRepository.findByItemIdAndUserIdAndExpiredEndDateAndApprovedStatus(item1.getId(), booker1.getId(), timestamp).size()).isEqualTo(1);
        Assertions.assertThat(bookingRepository.findByItemIdAndUserIdAndExpiredEndDateAndApprovedStatus(item1.getId(), booker2.getId(), timestamp).size()).isEqualTo(0);

        testEntityManager.persist(Booking.builder()
                .start(timestamp.minusDays(1))
                .end(timestamp.minusHours(1))
                .item(item2)
                .user(booker1)
                .status(Status.APPROVED)
                .build());

        Assertions.assertThat(bookingRepository.findByItemIdAndUserIdAndExpiredEndDateAndApprovedStatus(item1.getId(), booker1.getId(), timestamp).size()).isEqualTo(1);

        testEntityManager.persist(Booking.builder()
                .start(timestamp.minusDays(1))
                .end(timestamp.minusHours(1))
                .item(item1)
                .user(booker1)
                .status(Status.REJECTED)
                .build());

        Assertions.assertThat(bookingRepository.findByItemIdAndUserIdAndExpiredEndDateAndApprovedStatus(item1.getId(), booker1.getId(), timestamp).size()).isEqualTo(1);

        testEntityManager.persist(Booking.builder()
                .start(timestamp.minusDays(2))
                .end(timestamp.minusHours(2))
                .item(item1)
                .user(booker1)
                .status(Status.APPROVED)
                .build());

        Assertions.assertThat(bookingRepository
                .findByItemIdAndUserIdAndExpiredEndDateAndApprovedStatus(item1.getId(), booker1.getId(), timestamp).size()).isEqualTo(2);

        testEntityManager.persist(Booking.builder()
                .start(timestamp.minusDays(2))
                .end(timestamp.plusHours(2))
                .item(item1)
                .user(booker1)
                .status(Status.APPROVED)
                .build());

        Assertions.assertThat(bookingRepository
                .findByItemIdAndUserIdAndExpiredEndDateAndApprovedStatus(item1.getId(), booker1.getId(), timestamp).size()).isEqualTo(2);
    }
}
