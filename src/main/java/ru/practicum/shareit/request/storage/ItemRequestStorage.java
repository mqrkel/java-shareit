package ru.practicum.shareit.request.storage;

import ru.practicum.shareit.request.ItemRequest;

import java.util.List;
import java.util.Optional;

public interface ItemRequestStorage {
    ItemRequest save(ItemRequest itemRequest);

    Optional<ItemRequest> findById(Long id);

    List<ItemRequest> findAllByRequestorId(Long requestorId);

    List<ItemRequest> findAll();
}