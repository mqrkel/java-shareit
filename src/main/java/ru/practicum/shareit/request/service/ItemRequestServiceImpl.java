package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.storage.ItemRequestStorage;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestStorage requestStorage;
    private final UserStorage userStorage;

    @Override
    public ItemRequestResponseDto create(Long userId, ItemRequestDto dto) {
        log.info("Создание запроса от пользователя ID={}, описание='{}'", userId, dto.getDescription());

        User user = getUserOrThrow(userId);

        ItemRequest request = ItemRequestMapper.toEntity(dto, user);
        ItemRequest saved = requestStorage.save(request);

        log.debug("Запрос успешно сохранён: {}", saved);
        return ItemRequestMapper.toDto(saved);
    }

    @Override
    public List<ItemRequestResponseDto> getOwnRequests(Long userId) {
        log.info("Получение собственных запросов пользователя ID={}", userId);

        getUserOrThrow(userId);

        List<ItemRequest> requests = requestStorage.findAllByRequestorId(userId);
        log.debug("Найдено {} собственных запросов для пользователя ID={}", requests.size(), userId);

        return requests.stream()
                .map(ItemRequestMapper::toDto)
                .toList();
    }

    @Override
    public List<ItemRequestResponseDto> getAllRequests(Long userId) {
        log.info("Получение всех чужих запросов для пользователя ID={}", userId);

        getUserOrThrow(userId);

        List<ItemRequest> allRequests = requestStorage.findAll();
        List<ItemRequestResponseDto> result = allRequests.stream()
                .filter(req -> !req.getRequestor().getId().equals(userId))
                .map(ItemRequestMapper::toDto)
                .toList();

        log.debug("Найдено {} чужих запросов для пользователя ID={}", result.size(), userId);
        return result;
    }

    private User getUserOrThrow(Long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь ID={} не найден", userId);
                    return new NotFoundException("Пользователь не найден");
                });
    }
}