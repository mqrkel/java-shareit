package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

/**
 * REST-контроллер для управления вещами (Items).
 * Обеспечивает CRUD операции и поиск вещей.
 */
@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {

    private final ItemService itemService;

    /**
     * Создает новую вещь.
     *
     * @param itemDto DTO с данными вещи для создания.
     * @param ownerId ID пользователя-владельца (из заголовка X-Sharer-User-Id).
     * @return DTO созданной вещи с деталями.
     */
    @PostMapping
    public ItemResponseDto create(@RequestBody @Valid final ItemDto itemDto,
                                  @Positive @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("POST /items - создание вещи пользователем ID={}", ownerId);
        ItemResponseDto created = itemService.create(itemDto, ownerId);
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
    public ItemResponseDto update(@Positive @PathVariable Long itemId,
                                  @Valid @RequestBody ItemUpdateDto updateDto,
                                  @Positive @RequestHeader("X-Sharer-User-Id") Long ownerId) {
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
    public ItemResponseDto getById(@Positive @PathVariable Long itemId,
                                   @RequestHeader("X-Sharer-User-Id") Long userId) {
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
    public List<ItemResponseDto> getItemsByOwner(@Positive @RequestHeader("X-Sharer-User-Id") Long ownerId) {
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
}