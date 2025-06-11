package ru.practicum.shareit.booking.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.Booking;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static ru.practicum.shareit.booking.Booking.BookingStatus;

/**
 * In-memory implementation of {@link BookingStorage} interface.
 * Хранит бронирования в оперативной памяти с уникальной генерацией идентификаторов.
 * Поддерживает поиск, обновление, удаление и фильтрацию бронирований по состоянию и пользователям.
 */
@Repository
public class InMemoryBookingStorage implements BookingStorage {
    private final Map<Long, Booking> bookings = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    /**
     * Сохраняет новое бронирование, присваивая ему уникальный идентификатор.
     *
     * @param booking объект бронирования для сохранения
     * @return сохранённый объект бронирования с установленным id
     */
    @Override
    public Booking save(Booking booking) {
        long id = idGenerator.incrementAndGet();
        booking.setId(id);
        bookings.put(id, booking);
        return booking;
    }

    /**
     * Ищет бронирование по идентификатору.
     *
     * @param id идентификатор бронирования
     * @return Optional с найденным бронированием или пустой Optional, если бронирование не найдено
     */
    @Override
    public Optional<Booking> findById(Long id) {
        return Optional.ofNullable(bookings.get(id));
    }

    /**
     * Обновляет существующее бронирование.
     *
     * @param booking объект бронирования с обновлёнными данными
     * @return обновлённый объект бронирования
     * @throws NoSuchElementException если бронирование с указанным id не найдено
     */
    @Override
    public Booking update(Booking booking) {
        Long id = booking.getId();
        if (id == null || !bookings.containsKey(id)) {
            throw new NoSuchElementException("Booking not found with id: " + id);
        }
        bookings.put(id, booking);
        return booking;
    }

    /**
     * Возвращает список бронирований, сделанных конкретным пользователем (booker),
     * с фильтрацией по состоянию и пагинацией.
     *
     * @param bookerId идентификатор пользователя, сделавшего бронирования
     * @param state    состояние бронирования (например, ALL, WAITING, APPROVED и т.д.)
     * @param from     индекс первого элемента для пагинации (начинается с 0)
     * @param size     максимальное количество элементов в ответе
     * @return список бронирований, удовлетворяющих фильтрам и пагинации
     */
    @Override
    public List<Booking> findByBookerId(Long bookerId, String state, int from, int size) {
        List<Booking> filtered = bookings.values().stream()
                .filter(b -> b.getBooker().getId().equals(bookerId))
                .filter(b -> filterByState(b, state))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .toList();
        return paginate(filtered, from, size);
    }

    /**
     * Возвращает список бронирований для всех вещей, принадлежащих конкретному владельцу,
     * с фильтрацией по состоянию и пагинацией.
     *
     * @param ownerId идентификатор владельца вещей
     * @param state   состояние бронирования (например, ALL, WAITING, APPROVED и т.д.)
     * @param from    индекс первого элемента для пагинации (начинается с 0)
     * @param size    максимальное количество элементов в ответе
     * @return список бронирований, удовлетворяющих фильтрам и пагинации
     */
    @Override
    public List<Booking> findByOwnerId(Long ownerId, String state, int from, int size) {
        List<Booking> filtered = bookings.values().stream()
                .filter(b -> b.getItem().getOwner().getId().equals(ownerId))
                .filter(b -> filterByState(b, state))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .toList();
        return paginate(filtered, from, size);
    }

    /**
     * Фильтрует бронирование по заданному состоянию.
     *
     * @param booking бронирование для проверки
     * @param state   строковое представление состояния бронирования
     * @return true, если бронирование соответствует состоянию, иначе false
     */
    private boolean filterByState(Booking booking, String state) {
        if (state == null || state.equalsIgnoreCase("ALL")) {
            return true;
        }
        BookingStatus status = booking.getStatus();
        LocalDateTime now = LocalDateTime.now();
        return switch (state.toUpperCase()) {
            case "WAITING" -> status == BookingStatus.WAITING;
            case "REJECTED" -> status == BookingStatus.REJECTED;
            case "APPROVED" -> status == BookingStatus.APPROVED;
            case "CANCELED" -> status == BookingStatus.CANCELED;
            case "CURRENT" -> booking.getStart().isBefore(now) && booking.getEnd().isAfter(now);
            case "PAST" -> booking.getEnd().isBefore(now);
            case "FUTURE" -> booking.getStart().isAfter(now);
            default -> false;
        };
    }

    /**
     * Возвращает подсписок элементов для пагинации.
     *
     * @param list исходный список элементов
     * @param from индекс первого элемента (начинается с 0)
     * @param size количество элементов на странице
     * @return подсписок с элементами для текущей страницы
     */
    private List<Booking> paginate(List<Booking> list, int from, int size) {
        int start = Math.min(from, list.size());
        int end = Math.min(from + size, list.size());
        return list.subList(start, end);
    }

    /**
     * Возвращает список всех бронирований.
     *
     * @return список всех бронирований
     */
    @Override
    public List<Booking> findAll() {
        return new ArrayList<>(bookings.values());
    }

    /**
     * Удаляет бронирование по идентификатору.
     *
     * @param id идентификатор бронирования для удаления
     */
    @Override
    public void deleteById(Long id) {
        bookings.remove(id);
    }
}