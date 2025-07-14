package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static ru.practicum.shareit.util.HeaderConstants.USER_ID_HEADER;

/**
 * REST-контроллер для управления вещами (Items).
 * Обеспечивает CRUD операции и поиск вещей.
 */
@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    /**
     * Создает новую вещь.
     *
     * @param itemRequestDto DTO с данными вещи для создания.
     * @param ownerId        ID пользователя-владельца (из заголовка X-Sharer-User-Id).
     * @return DTO созданной вещи с деталями.
     */
    @PostMapping
    public ItemResponseDto create(@RequestBody final ItemRequestDto itemRequestDto,
                                  @RequestHeader(USER_ID_HEADER) Long ownerId) {
        log.info("POST /items - создание вещи пользователем ID={}", ownerId);
        ItemResponseDto created = itemService.create(itemRequestDto, ownerId);
        log.debug("Вещь создана: {}", created);
        return created;
    }

    /**
     * Обновляет данные существующей вещи.
     *
     * @param itemId    ID вещи для обновления.
     * @param updateDto DTO с обновленными полями вещи.
     * @param ownerId   ID владельца вещи (из заголовка X-Sharer-User-Id).
     * @return DTO обновленной вещи.
     */
    @PatchMapping("/{itemId}")
    public ItemResponseDto update(@PathVariable Long itemId,
                                  @RequestBody ItemRequestUpdateDto updateDto,
                                  @RequestHeader(USER_ID_HEADER) Long ownerId) {
        log.info("PATCH /items/{} - обновление вещи пользователем ID={}", itemId, ownerId);
        ItemResponseDto updated = itemService.update(itemId, updateDto, ownerId);
        log.debug("Вещь обновлена: {}", updated);
        return updated;
    }

    /**
     * Получает вещь по её ID.
     *
     * @param itemId ID вещи.
     * @param userId ID пользователя, запрашивающего вещь (из заголовка X-Sharer-User-Id).
     * @return DTO запрашиваемой вещи.
     */
    @GetMapping("/{itemId}")
    public ItemResponseDto getById(@PathVariable Long itemId,
                                   @RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("GET /items/{} - запрос вещи пользователем ID={}", itemId, userId);
        return itemService.getById(itemId, userId);
    }

    /**
     * Получает список всех вещей, принадлежащих указанному владельцу.
     *
     * @param ownerId ID владельца вещей (из заголовка X-Sharer-User-Id).
     * @return Список DTO вещей владельца.
     */
    @GetMapping
    public List<ItemResponseDto> getItemsByOwner(@RequestHeader(USER_ID_HEADER) Long ownerId) {
        log.info("GET /items - получение всех вещей пользователя ID={}", ownerId);
        return itemService.getItemsByOwner(ownerId);
    }

    /**
     * Ищет доступные вещи, содержащие в имени или описании указанный текст.
     *
     * @param text Текст для поиска.
     * @return Список DTO найденных вещей.
     */
    @GetMapping("/search")
    public List<ItemResponseDto> searchAvailable(@RequestParam String text) {
        log.info("GET /items/search - поиск вещей по тексту '{}'", text);
        return itemService.searchAvailable(text);
    }

    /**
     * Добавляет комментарий к вещи от имени пользователя.
     *
     * @param itemId     идентификатор вещи, к которой добавляется комментарий
     * @param userId     идентификатор пользователя, добавляющего комментарий (из заголовка USER_ID_HEADER )
     * @param commentDto тело запроса с текстом комментария
     * @return DTO с информацией о добавленном комментарии
     */
    @PostMapping("/{itemId}/comment")
    public CommentResponseDto addComment(@PathVariable Long itemId,
                                         @RequestHeader(USER_ID_HEADER) Long userId,
                                         @RequestBody CommentRequestDto commentDto) {
        log.info("POST /items/{}/comment - пользователь ID={} оставляет комментарий", itemId, userId);
        return itemService.addComment(itemId, userId, commentDto);
    }
}