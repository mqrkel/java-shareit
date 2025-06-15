package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.User;

import java.util.*;

@Repository
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long currentId = 0L;

    private long generateId() {
        return ++currentId;
    }

    @Override
    public User save(User user) {
        if (findByEmail(user.getEmail()).isPresent()) {
            throw new ConflictException("Пользователь с email" + user.getEmail() + " уже существует");
        }
        long id = generateId();
        user.setId(id);
        users.put(id, user);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User update(User user) {
        Long id = user.getId();
        if (id == null || !users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
        for (User u : users.values()) {
            if (!u.getId().equals(id) && u.getEmail().equals(user.getEmail())) {
                throw new ConflictException("Email уже используется другим пользователем");
            }
        }
        users.put(id, user);
        return user;
    }

    @Override
    public void deleteById(Long id) {
        users.remove(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return users.values().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }
}