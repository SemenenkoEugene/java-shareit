package ru.practicum.shareit.item;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;

import java.util.Collection;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findAllByOwnerId(Long ownerId, Pageable pageable);

    @Query("select i from Item i " +
           "where upper(i.name) like upper(concat('%', ?1, '%')) " +
           "or upper(i.description) like upper(concat('%', ?1, '%'))" +
           " and i.available = true ")
    List<Item> findBySearchText(String searchText, Pageable pageable);

    List<Item> findAllByItemRequestId(Long userid);

    List<Item> findAllByItemRequestIn(Collection<ItemRequest> itemRequests);
}
