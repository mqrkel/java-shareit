package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

import static ru.practicum.shareit.util.HeaderConstants.USER_ID_HEADER;

/**
 * REST-контроллер для управления запросами на предметы (Item Requests).
 * Позволяет создавать запросы, получать собственные запросы и просматривать запросы других пользователей.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/requests")
public class ItemRequestController {

    private final ItemRequestService requestService;

    /**
     * Создает новый запрос на предмет от пользователя.
     *
     * @param userId ID пользователя, создающего запрос (из заголовка X-Sharer-User-Id).
     * @param dto    DTO с данными запроса.
     * @return DTO созданного запроса с деталями.
     */
    @PostMapping
    public ItemRequestResponseDto createRequest(@RequestHeader(USER_ID_HEADER) Long userId,
                                                @RequestBody final ItemRequestDto dto) {
        log.info("POST /requests by userId {}", userId);
        return requestService.create(userId, dto);
    }

    /**
     * Получает список запросов, созданных пользователем.
     *
     * @param userId ID пользователя, чьи запросы запрашиваются (из заголовка X-Sharer-User-Id).
     * @return Список DTO собственных запросов пользователя.
     */
    @GetMapping
    public List<ItemRequestResponseDto> getOwnRequests(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("GET /requests (own) by userId {}", userId);
        return requestService.getOwnRequests(userId);
    }

    /**
     * Получает список всех запросов других пользователей, исключая запросы указанного пользователя.
     *
     * @param userId ID пользователя, для которого запрашиваются чужие запросы (из заголовка X-Sharer-User-Id).
     * @return Список DTO чужих запросов.
     */
    @GetMapping("/all")
    public List<ItemRequestResponseDto> getAllRequests(@RequestHeader(USER_ID_HEADER) Long userId,
                                                       @RequestParam(defaultValue = "0") int from,
                                                       @RequestParam(defaultValue = "10") int size) {
        log.info("GET /requests/all by userId {}", userId);
        return requestService.getAllRequests(userId, from, size);
    }

    /**
     * Получает информацию о конкретном запросе вещи по его ID вместе с ответами на него.
     *
     * @param userId    ID пользователя, делающего запрос (из заголовка X-Sharer-User-Id).
     *                  Проверяется, что такой пользователь существует.
     * @param requestId ID запрашиваемого предмета (Item Request).
     * @return DTO с полной информацией о запросе и списком связанных вещей.
     * @throws NotFoundException если пользователь или запрос не найден.
     */
    @GetMapping("/{requestId}")
    public ItemRequestResponseDto getRequestById(@RequestHeader(USER_ID_HEADER) Long userId,
                                                 @PathVariable Long requestId) {
        log.info("GET /requests/{} by userId {}", requestId, userId);
        return requestService.getRequestById(userId, requestId);
    }

}