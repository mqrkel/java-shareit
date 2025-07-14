package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

import static ru.practicum.shareit.util.HeaderConstants.USER_ID_HEADER;

/**
 * Контроллер для обработки запросов, связанных с бронированием вещей.
 */
@Slf4j
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /**
     * Создание нового бронирования пользователем.
     *
     * @param bookingDto Данные бронирования.
     * @param userId     Идентификатор пользователя (бронирующего).
     * @return Информация о созданном бронировании.
     */
    @PostMapping
    public BookingResponseDto createBooking(@RequestBody BookingRequestDto bookingDto,
                                            @RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("POST /bookings by userId {}", userId);
        return bookingService.createBooking(bookingDto, userId);
    }

    /**
     * Подтверждение или отклонение бронирования владельцем вещи.
     *
     * @param bookingId Идентификатор бронирования.
     * @param ownerId   Идентификатор владельца вещи.
     * @param approved  Флаг подтверждения (true - одобрить, false - отклонить).
     * @return Обновлённая информация о бронировании.
     */
    @PatchMapping("/{bookingId}")
    public BookingResponseDto approveBooking(@PathVariable Long bookingId,
                                             @RequestHeader(USER_ID_HEADER) Long ownerId,
                                             @RequestParam boolean approved) {
        log.info("PATCH /bookings/{}?approved={} by ownerId {}", bookingId, approved, ownerId);
        return bookingService.approveBooking(bookingId, ownerId, approved);
    }

    /**
     * Получение информации о конкретном бронировании.
     * Доступно бронирующему или владельцу вещи.
     *
     * @param bookingId Идентификатор бронирования.
     * @param userId    Идентификатор пользователя.
     * @return Информация о бронировании.
     */
    @GetMapping("/{bookingId}")
    public BookingResponseDto getBookingById(@PathVariable Long bookingId,
                                             @RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("GET /bookings/{} by userId {}", bookingId, userId);
        return bookingService.getBookingById(bookingId, userId);
    }

    /**
     * Получение списка бронирований, сделанных пользователем (бронирующим), по заданному статусу.
     *
     * @param userId Идентификатор пользователя, сделавшего бронирование.
     * @param state  Статус бронирования, по которому производится фильтрация.
     *               Возможные значения:
     *               <ul>
     *                 <li>{@code WAITING} — бронирование ожидает одобрения владельцем</li>
     *                 <li>{@code APPROVED} — бронирование подтверждено владельцем</li>
     *                 <li>{@code REJECTED} — бронирование отклонено владельцем</li>
     *                 <li>{@code CANCELED} — бронирование отменено создателем</li>
     *               </ul>
     * @param from   Индекс первого элемента для постраничного вывода (неотрицательное число).
     * @param size   Количество элементов на странице (положительное число).
     * @return Список бронирований, соответствующих фильтру.
     */
    @GetMapping
    public List<BookingResponseDto> getBookingsByBooker(@RequestHeader(USER_ID_HEADER) Long userId,
                                                        @RequestParam(name = "state", defaultValue = "ALL") String state,
                                                        @RequestParam(defaultValue = "0") int from,
                                                        @RequestParam(defaultValue = "10") int size) {
        log.info("GET /bookings by bookerId {}, state={}, from={}, size={}", userId, state, from, size);
        return bookingService.getBookingsByBooker(userId, state, from, size);
    }

    /**
     * Получение списка бронирований для всех вещей, принадлежащих пользователю (владельцу).
     *
     * @param ownerId Идентификатор владельца.
     * @param state   Фильтр по статусу бронирования.
     * @param from    Индекс первого элемента (для пагинации).
     * @param size    Количество элементов на странице.
     * @return Список бронирований для вещей владельца.
     */
    @GetMapping("/owner")
    public List<BookingResponseDto> getBookingsByOwner(@RequestHeader(USER_ID_HEADER) Long ownerId,
                                                       @RequestParam(name = "state", defaultValue = "ALL") String state,
                                                       @RequestParam(defaultValue = "0") int from,
                                                       @RequestParam(defaultValue = "10") int size) {
        log.info("GET /bookings/ownerId by ownerId {}, state={}, from={}, size={}", ownerId, state, from, size);
        return bookingService.getBookingsByOwner(ownerId, state, from, size);
    }
}