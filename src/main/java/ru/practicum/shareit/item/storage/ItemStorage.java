package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemStorage {

    Item save(Item item);

    Optional<Item> findById(Long id);

    List<Item> findAll();

    Item update(Item item);

    void deleteById(Long id);

    List<Item> searchAvailableByText(String text);

    List<Item> findByOwnerId(Long ownerId);
}