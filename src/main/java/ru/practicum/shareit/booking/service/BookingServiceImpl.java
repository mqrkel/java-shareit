package ru.practicum.shareit.booking.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.shareit.booking.Booking.BookingStatus.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;

    /**
     * Создаёт новое бронирование вещи.
     *
     * @param bookingDto данные бронирования
     * @param bookerId   ID пользователя, делающего бронирование
     * @return объект BookingResponseDto с данными созданного бронирования
     * @throws ValidationException если вещь недоступна, даты некорректны или пользователь — владелец вещи
     * @throws NotFoundException   если пользователь или вещь не найдены
     */
    @Override
    @Transactional
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
        Booking booking = bookingMapper.toEntity(bookingDto, item, booker);
        booking.setStatus(WAITING);

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Бронирование создано: bookingId={}", savedBooking.getId());
        return bookingMapper.toDto(savedBooking);
    }

    /**
     * Подтверждает или отклоняет бронирование владельцем вещи.
     *
     * @param bookingId ID бронирования
     * @param ownerId   ID владельца вещи
     * @param approved  true — подтвердить, false — отклонить
     * @return обновлённый BookingResponseDto
     * @throws ValidationException если пользователь не владелец вещи или бронирование уже обработано
     * @throws NotFoundException   если бронирование не найдено
     */
    @Override
    @Transactional
    public BookingResponseDto approveBooking(Long bookingId, Long ownerId, boolean approved) {
        Booking booking = getBookingOrThrow(bookingId);

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            log.warn("Пользователь с id={} не является владельцем вещи, связанной с бронированием id={}", ownerId, bookingId);
            throw new ValidationException("Подтвердить бронирование может только владелец вещи");
        }

        if (booking.getStatus() != WAITING) {
            log.warn("Попытка повторной обработки бронирования: bookingId={}, текущий статус={}",
                    bookingId, booking.getStatus());
            throw new ConflictException("Бронирование уже обработано");
        }

        log.info("Подтверждение бронирования: bookingId={}, ownerId={}, approved={}",
                bookingId, ownerId, approved);
        booking.setStatus(approved ? APPROVED : REJECTED);
        log.info("Статус бронирования обновлён: bookingId={}, новый статус={}", booking.getId(), booking.getStatus());
        return bookingMapper.toDto(booking);
    }

    /**
     * Получает бронирование по его ID, если пользователь является автором бронирования или владельцем вещи.
     *
     * @param bookingId ID бронирования
     * @param userId    ID пользователя, делающего запрос
     * @return BookingResponseDto с данными бронирования
     * @throws ValidationException если пользователь не автор бронирования и не владелец вещи
     * @throws NotFoundException   если бронирование не найдено
     */
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
        return bookingMapper.toDto(booking);
    }

    /**
     * Получает список бронирований пользователя по его роли "бронирующий" с фильтрацией по состоянию.
     *
     * @param bookerId ID пользователя-бронирующего
     * @param state    состояние бронирований ("ALL", "CURRENT", "PAST", "FUTURE", "WAITING", "REJECTED")
     * @param from     индекс первого элемента для пагинации
     * @param size     количество элементов на страницу
     * @return список BookingResponseDto, соответствующих фильтру
     * @throws ValidationException при неизвестном значении состояния
     */
    @Override
    public List<BookingResponseDto> getBookingsByBooker(Long bookerId, String state, int from, int size) {
        getUserOrThrow(bookerId);
        Pageable pageable = PageRequest.of(from / size, size);
        LocalDateTime now = LocalDateTime.now();


        List<Booking> bookings = switch (state.toUpperCase()) {
            case "ALL" -> bookingRepository.findByBookerIdOrderByStartDesc(bookerId, pageable);
            case "CURRENT" -> bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                    bookerId, now, now, pageable);
            case "PAST" -> bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(bookerId, now, pageable);
            case "FUTURE" -> bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(bookerId, now, pageable);
            case "WAITING", "REJECTED" -> bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                    bookerId, valueOf(state.toUpperCase()), pageable);
            default -> throw new ValidationException("Unknown state: " + state);
        };

        log.info("Получение бронирований пользователя: bookerId={}, state={}, from={}, size={}",
                bookerId, state, from, size);
        return bookings.stream()
                .map(bookingMapper::toDto)
                .toList();
    }

    /**
     * Получает список бронирований пользователя по его роли "владелец вещи" с фильтрацией по состоянию.
     *
     * @param ownerId ID владельца вещи
     * @param state   состояние бронирований ("ALL", "CURRENT", "PAST", "FUTURE", "WAITING", "REJECTED")
     * @param from    индекс первого элемента для пагинации
     * @param size    количество элементов на страницу
     * @return список BookingResponseDto, соответствующих фильтру
     * @throws ValidationException при неизвестном значении состояния
     */
    @Override
    public List<BookingResponseDto> getBookingsByOwner(Long ownerId, String state, int from, int size) {
        getUserOrThrow(ownerId);
        Pageable pageable = PageRequest.of(from / size, size);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state.toUpperCase()) {
            case "ALL" -> bookingRepository.findByOwnerId(ownerId, pageable);
            case "CURRENT" -> bookingRepository.findCurrentByOwnerId(ownerId, now, pageable);
            case "PAST" -> bookingRepository.findPastByOwnerId(ownerId, now, pageable);
            case "FUTURE" -> bookingRepository.findFutureByOwnerId(ownerId, now, pageable);
            case "WAITING", "REJECTED" -> bookingRepository.findByOwnerIdAndStatus(
                    ownerId, valueOf(state.toUpperCase()), pageable);
            default -> throw new ValidationException("Unknown state: " + state);
        };

        log.info("Получение бронирований владельца: ownerId={}, state={}, from={}, size={}",
                ownerId, state, from, size);
        return bookings.stream()
                .map(bookingMapper::toDto)
                .toList();
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь не найден: id={}", userId);
                    return new NotFoundException("Пользователь не найден");
                });
    }

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Вещь не найдена: id={}", itemId);
                    return new NotFoundException("Вещь не найдена");
                });
    }

    private Booking getBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.warn("Бронирование не найдено: id={}", bookingId);
                    return new NotFoundException("Бронирование не найдено");
                });
    }
}