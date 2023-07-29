package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByUserIdOrderByStartDesc(Long userId);

    List<Booking> findAllByUserIdAndEndBeforeOrderByStartDesc(Long userId, LocalDateTime endDateTime);

    List<Booking> findAllByUserIdAndStartAfterOrderByStartDesc(Long userId, LocalDateTime startDateTime);

    List<Booking> findAllByUserIdAndStartBeforeAndEndAfterOrderByStartDesc(Long userId, LocalDateTime startDateTime, LocalDateTime endDateTime);

    List<Booking> findAllByUserIdAndStatusOrderByStartDesc(Long userId, Status status);

    List<Booking> findAllByItemOwnerIdOrderByStartDesc(Long ownerId);

    List<Booking> findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(Long ownerId, LocalDateTime endDateTime);

    List<Booking> findAllByItemOwnerIdAndStartAfterOrderByStartDesc(Long ownerId, LocalDateTime startDateTime);

    List<Booking> findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long ownerId, LocalDateTime startDateTime,
                                                                                LocalDateTime endDateTime);

    List<Booking> findAllByItemOwnerIdAndStatusOrderByStartDesc(Long ownerId, Status status);

    List<Booking> findAllByItemId(Long itemId);

    @Query("SELECT b FROM Booking b " +
           "WHERE b.item.id = :itemId " +
           "AND b.user.id = :userId " +
           "AND b.status = ru.practicum.shareit.booking.Status.APPROVED " +
           "AND b.end < :currentTime")
    List<Booking> findByItemIdAndUserIdAndExpiredEndDateAndApprovedStatus(Long itemId, Long userId, LocalDateTime currentTime);
}
