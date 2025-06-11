package ru.practicum.shareit.booking.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

import static ru.practicum.shareit.booking.Booking.BookingStatus;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingStorage bookingStorage;
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Override
    public BookingResponseDto createBooking(BookingDto bookingDto, Long bookerId) {
        User booker = getUserOrThrow(bookerId);
        Item item = getItemOrThrow(bookingDto.getItemId());

        if (Boolean.FALSE.equals(item.getAvailable())) {
            log.warn("Попытка бронирования недоступной вещи: itemId={}", item.getId());
            throw new ValidationException("Вещь недоступна для бронирования");
        }

        if (item.getOwner().getId().equals(bookerId)) {
            log.warn("Владелец пытается забронировать свою вещь: userId={}, itemId={}", bookerId, item.getId());
            throw new ValidationException("Владелец вещи не может бронировать собственную вещь");
        }

        if (bookingDto.getStart() == null || bookingDto.getEnd() == null
            || !bookingDto.getEnd().isAfter(bookingDto.getStart())) {
            log.warn("Некорректные даты бронирования: start={}, end={}", bookingDto.getStart(), bookingDto.getEnd());
            throw new ValidationException("Некорректные даты бронирования");
        }

        log.info("Создание бронирования: userId={}, itemId={}, start={}, end={}",
                bookerId, bookingDto.getItemId(), bookingDto.getStart(), bookingDto.getEnd());
        Booking booking = BookingMapper.toEntity(bookingDto, item, booker);
        booking.setStatus(BookingStatus.WAITING);

        Booking savedBooking = bookingStorage.save(booking);
        log.info("Бронирование создано: bookingId={}", savedBooking.getId());
        return BookingMapper.toDto(savedBooking);
    }

    @Override
    public BookingResponseDto approveBooking(Long bookingId, Long ownerId, boolean approved) {
        Booking booking = getBookingOrThrow(bookingId);

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            log.warn("Пользователь с id={} не является владельцем вещи, связанной с бронированием id={}", ownerId, bookingId);
            throw new ValidationException("Подтвердить бронирование может только владелец вещи");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            log.warn("Попытка повторной обработки бронирования: bookingId={}, текущий статус={}",
                    bookingId, booking.getStatus());
            throw new ConflictException("Бронирование уже обработано");
        }

        log.info("Подтверждение бронирования: bookingId={}, ownerId={}, approved={}",
                bookingId, ownerId, approved);
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updatedBooking = bookingStorage.update(booking);
        log.info("Статус бронирования обновлён: bookingId={}, новый статус={}", booking.getId(), booking.getStatus());
        return BookingMapper.toDto(updatedBooking);
    }

    @Override
    public BookingResponseDto getBookingById(Long bookingId, Long userId) {
        Booking booking = getBookingOrThrow(bookingId);

        if (!booking.getBooker().getId().equals(userId)
            && !booking.getItem().getOwner().getId().equals(userId)) {
            log.warn("Доступ запрещён: userId={} не является ни автором, ни владельцем вещи bookingId={}",
                    userId, bookingId);
            throw new ValidationException("Доступ запрещён");
        }
        log.info("Получение бронирования по id: bookingId={}, запрашивает userId={}", bookingId, userId);
        return BookingMapper.toDto(booking);
    }

    @Override
    public List<BookingResponseDto> getBookingsByBooker(Long bookerId, String state, int from, int size) {
        getUserOrThrow(bookerId);

        List<Booking> bookings = bookingStorage.findByBookerId(bookerId, state, from, size);
        log.info("Получение бронирований пользователя: bookerId={}, state={}, from={}, size={}",
                bookerId, state, from, size);
        return bookings.stream()
                .map(BookingMapper::toDto)
                .toList();
    }

    @Override
    public List<BookingResponseDto> getBookingsByOwner(Long ownerId, String state, int from, int size) {
        getUserOrThrow(ownerId);
        List<Booking> bookings = bookingStorage.findByOwnerId(ownerId, state, from, size);

        log.info("Получение бронирований владельца: ownerId={}, state={}, from={}, size={}",
                ownerId, state, from, size);
        return bookings.stream()
                .map(BookingMapper::toDto)
                .toList();
    }

    private User getUserOrThrow(Long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь не найден: id={}", userId);
                    return new NotFoundException("Пользователь не найден");
                });
    }

    private Item getItemOrThrow(Long itemId) {
        return itemStorage.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Вещь не найдена: id={}", itemId);
                    return new NotFoundException("Вещь не найдена");
                });
    }

    private Booking getBookingOrThrow(Long bookingId) {
        return bookingStorage.findById(bookingId)
                .orElseThrow(() -> {
                    log.warn("Бронирование не найдено: id={}", bookingId);
                    return new NotFoundException("Бронирование не найдено");
                });
    }
}