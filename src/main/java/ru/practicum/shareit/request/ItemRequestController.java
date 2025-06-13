package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

/**
 * REST-контроллер для управления запросами на предметы (Item Requests).
 * Позволяет создавать запросы, получать собственные запросы и просматривать запросы других пользователей.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/requests")
@Validated
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
    public ItemRequestResponseDto createRequest(@Positive @RequestHeader("X-Sharer-User-Id") Long userId,
                                                @Valid @RequestBody final ItemRequestDto dto) {
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
    public List<ItemRequestResponseDto> getOwnRequests(@Positive @RequestHeader("X-Sharer-User-Id") Long userId) {
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
    public List<ItemRequestResponseDto> getAllRequests(@Positive @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("GET /requests/all by userId {}", userId);
        return requestService.getAllRequests(userId);
    }
}