package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
class BookingIntegrationTest {
    @Autowired
    EntityManager entityManager;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Order(1)
    @Transactional
    @Rollback(false)
    void createBookingTest() {
        User owner = getUser(1L);
        User booker = getUser(2L);

        TypedQuery<Long> userCountQuery = entityManager.createQuery(
                "SELECT COUNT(u) FROM User u", Long.class
        );

        Long userCount = userCountQuery.getSingleResult();
        assertThat(userCount, equalTo(2L));

        Item item = getItem(1L, owner);

        TypedQuery<Long> itemCountQuery = entityManager.createQuery(
                "SELECT COUNT(i) FROM Item i", Long.class
        );

        Long itemCount = itemCountQuery.getSingleResult();
        assertThat(itemCount, equalTo(1L));

        Booking booking1 = getBooking(1L, booker, item, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        Booking booking2 = getBooking(2L, booker, item, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4));
        Booking booking3 = getBooking(3L, booker, item, LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(6));
        Booking booking4 = getBooking(4L, booker, item, LocalDateTime.now().plusDays(7), LocalDateTime.now().plusDays(8));
        Booking booking5 = getBooking(5L, booker, item, LocalDateTime.now().plusDays(9), LocalDateTime.now().plusDays(10));
        Booking booking6 = getBooking(6L, booker, item, LocalDateTime.now().plusDays(11), LocalDateTime.now().plusDays(12));
        Booking booking7 = getBooking(7L, booker, item, LocalDateTime.now().plusDays(13), LocalDateTime.now().plusDays(14));

        TypedQuery<Long> bookingCountQuery = entityManager.createQuery(
                "SELECT COUNT(b) FROM Booking b", Long.class
        );

        Long bookingCount = bookingCountQuery.getSingleResult();
        assertThat(bookingCount, equalTo(7L));
    }

    private User getUser(Long id) {
        User user = User.builder()
                .name("User " + id)
                .email("user" + id + "@user.com")
                .build();

        entityManager.persist(user);

        return user;
    }

    private Item getItem(Long id, User owner) {
        Item item = Item.builder()
                .name("Item " + id)
                .description("ItemDescr " + id)
                .available(true)
                .owner(owner)
                .build();

        entityManager.persist(item);

        return item;
    }

    private Booking getBooking(Long id, User booker, Item item, LocalDateTime start, LocalDateTime end) {
        Booking booking = Booking.builder()
                .status(Status.APPROVED)
                .user(booker)
                .item(item)
                .start(start)
                .end(end)
                .build();

        entityManager.persist(booking);

        return booking;
    }
}
