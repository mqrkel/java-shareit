package ru.practicum.shareit.request.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.request.ItemRequest;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryItemRequestStorage implements ItemRequestStorage {
    private final Map<Long, ItemRequest> requests = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    @Override
    public ItemRequest save(ItemRequest itemRequest) {
        if (itemRequest.getId() == null) {
            itemRequest.setId(idGenerator.incrementAndGet());
        }
        requests.put(itemRequest.getId(), itemRequest);
        return itemRequest;
    }

    @Override
    public Optional<ItemRequest> findById(Long id) {
        return Optional.ofNullable(requests.get(id));
    }

    @Override
    public List<ItemRequest> findAllByRequestorId(Long requestorId) {
        List<ItemRequest> result = new ArrayList<>();
        for (ItemRequest request : requests.values()) {
            if (request.getRequestor() != null && request.getRequestor().getId().equals(requestorId)) {
                result.add(request);
            }
        }
        return result;
    }

    @Override
    public List<ItemRequest> findAll() {
        return new ArrayList<>(requests.values());
    }
}
