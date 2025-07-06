package ru.practicum.shareit.request.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.request.ItemRequest;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    List<ItemRequest> findByRequestorIdOrderByCreatedDesc(Long itemId);

    @Query("SELECT r FROM ItemRequest r " +
           "WHERE r.requestor.id <> :userId " +
           "ORDER BY r.created DESC")
    List<ItemRequest> findAllOtherUsersRequests(@Param("userId") Long userId, Pageable pageable);
}