package ru.practicum.shareit.request;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findAllByRequestorIdNotOrderByCreatedDesc(long userId, Pageable pageable);

    List<ItemRequest> findAllByRequestorIdOrderByCreatedDesc(long requestorId, Pageable pageable);
}
