package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestStorage;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;
    private final ItemRequestStorage itemRequestStorage;

    @Override
    public ItemResponseDto create(ItemDto itemDto, Long ownerId) {
        log.info("Создание новой вещи: {}, владелец ID={}", itemDto.getName(), ownerId);
        User owner = getUserOrThrow(ownerId);
        ItemRequest request = itemDto.getRequestId() == null ? null :
                itemRequestStorage.findById(itemDto.getRequestId()).orElse(null);

        Item item = ItemMapper.toEntity(itemDto, owner, request);
        Item savedItem = itemStorage.save(item);
        log.debug("Вещь успешно создана: {}", savedItem);
        return ItemMapper.toDto(savedItem);
    }


    @Override
    public ItemResponseDto update(Long itemId, ItemUpdateDto itemDto, Long ownerId) {
        log.info("Обновление вещи ID={}, запрошено пользователем ID={}", itemId, ownerId);
        Item item = getItemOrThrow(itemId);

        if (!item.getOwner().getId().equals(ownerId)) {
            log.warn("Пользователь ID={} попытался обновить вещь, которой не владеет", ownerId);
            throw new ForbiddenException("Редактировать вещь может только её владелец");
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemStorage.update(item);
        log.debug("Вещь успешно обновлена: {}", updatedItem);
        return ItemMapper.toDto(updatedItem);
    }

    @Override
    public ItemResponseDto getById(Long itemId, Long userId) {
        log.info("Получение вещи ID={} пользователем ID={}", itemId, userId);
        Item item = getItemOrThrow(itemId);
        return ItemMapper.toDto(item);
    }

    @Override
    public List<ItemResponseDto> getItemsByOwner(Long ownerId) {
        log.info("Получение всех вещей пользователя ID={}", ownerId);
        getUserOrThrow(ownerId);
        List<Item> items = itemStorage.findByOwnerId(ownerId);
        log.debug("Найдено {} вещей для пользователя ID={}", items.size(), ownerId);
        return items.stream().map(ItemMapper::toDto).toList();
    }

    @Override
    public List<ItemResponseDto> searchAvailable(String text) {
        log.info("Поиск доступных вещей по тексту: '{}'", text);
        if (text == null || text.isBlank()) {
            log.debug("Пустой текст поиска, возвращён пустой список");
            return List.of();
        }
        List<Item> items = itemStorage.searchAvailableByText(text);
        log.debug("Найдено {} вещей по запросу '{}'", items.size(), text);
        return items.stream().map(ItemMapper::toDto).toList();
    }

    private User getUserOrThrow(Long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь ID={} не найден", userId);
                    return new NotFoundException("Пользователь не найден");
                });
    }

    private Item getItemOrThrow(Long itemId) {
        return itemStorage.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Вещь ID={} не найдена", itemId);
                    return new NotFoundException("Вещь не найдена");
                });
    }
}