package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryItemStorage implements ItemStorage {
    private final Map<Long, Item> items = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    @Override
    public Item save(Item item) {
        long id = idGenerator.incrementAndGet();
        item.setId(id);
        items.put(id, item);
        return item;
    }

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public List<Item> findAll() {
        return new ArrayList<>(items.values());
    }

    @Override
    public Item update(Item item) {
        Long id = item.getId();
        if (id == null || !items.containsKey(id)) {
            throw new NotFoundException("Вещь не найдена");
        }
        items.put(id, item);
        return item;
    }

    @Override
    public void deleteById(Long id) {
        items.remove(id);
    }

    @Override
    public List<Item> searchAvailableByText(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        String lowerText = text.toLowerCase();
        List<Item> result = new ArrayList<>();

        for (Item item : items.values()) {
            if (Boolean.TRUE.equals(item.getAvailable())
                && (item.getName().toLowerCase().contains(lowerText)
                    || item.getDescription().toLowerCase().contains(lowerText))) {
                result.add(item);
            }
        }
        return result;
    }

    @Override
    public List<Item> findByOwnerId(Long ownerId) {
        List<Item> result = new ArrayList<>();
        for (Item item : items.values()) {
            if (item.getOwner() != null && ownerId.equals(item.getOwner().getId())) {
                result.add(item);
            }
        }
        return result;
    }
}