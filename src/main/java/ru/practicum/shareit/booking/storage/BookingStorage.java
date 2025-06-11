package ru.practicum.shareit.booking.storage;


import ru.practicum.shareit.booking.Booking;

import java.util.List;
import java.util.Optional;

public interface BookingStorage {

    Booking save(Booking booking);

    Optional<Booking> findById(Long id);

    Booking update(Booking booking);

    List<Booking> findByBookerId(Long bookerId, String state, int from, int size);

    List<Booking> findByOwnerId(Long ownerId, String state, int from, int size);

    List<Booking> findAll();

    void deleteById(Long id);
}
