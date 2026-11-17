package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Comment;

import java.util.Collection;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByItemId(Long itemId);

    @Query("SELECT c FROM Comment c " +
           "JOIN FETCH c.item " +
           "JOIN FETCH c.author " +
           "WHERE c.item.id IN :itemIds")
    List<Comment> findAllByItemIdIn(@Param("itemIds") Collection<Long> itemIds);
}
