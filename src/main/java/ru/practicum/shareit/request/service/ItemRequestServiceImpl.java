package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRequestMapper itemRequestMapper;

    /**
     * Создаёт новый запрос на вещь от пользователя.
     *
     * @param userId идентификатор пользователя, создающего запрос
     * @param dto    DTO с описанием запроса
     * @return DTO с информацией о созданном запросе
     * @throws NotFoundException если пользователь не найден
     */
    @Override
    @Transactional
    public ItemRequestResponseDto create(Long userId, ItemRequestDto dto) {
        log.info("Создание запроса от пользователя ID={}, описание='{}'", userId, dto.getDescription());

        User user = getUserOrThrow(userId);

        ItemRequest request = itemRequestMapper.toEntity(dto, user);
        request.setCreated(LocalDateTime.now());
        ItemRequest saved = requestRepository.save(request);

        log.debug("Запрос успешно сохранён: {}", saved);
        return itemRequestMapper.toDto(saved);
    }

    /**
     * Получает все собственные запросы пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список запросов, созданных пользователем
     * @throws NotFoundException если пользователь не найден
     */
    @Override
    public List<ItemRequestResponseDto> getOwnRequests(Long userId) {
        log.info("Получение собственных запросов пользователя ID={}", userId);

        getUserOrThrow(userId);

        List<ItemRequest> requests = requestRepository.findByRequestorIdOrderByCreatedDesc(userId);
        log.debug("Найдено {} собственных запросов для пользователя ID={}", requests.size(), userId);

        return requests.stream()
                .map(itemRequestMapper::toDto)
                .toList();
    }

    /**
     * Получает все запросы других пользователей постранично.
     *
     * @param userId идентификатор пользователя, делающего запрос
     * @param from   индекс первого элемента (0-индексация)
     * @param size   количество элементов на странице
     * @return список запросов от других пользователей
     * @throws NotFoundException если пользователь не найден
     */
    @Override
    public List<ItemRequestResponseDto> getAllRequests(Long userId, int from, int size) {
        log.info("Получение всех чужих запросов для пользователя ID={}", userId);

        getUserOrThrow(userId);
        Pageable pageable = PageRequest.of(from / size, size);

        List<ItemRequest> requests = requestRepository.findAllOtherUsersRequests(userId, pageable);

        log.debug("Найдено {} чужих запросов для пользователя ID={}", requests.size(), userId);

        return requests.stream()
                .map(itemRequestMapper::toDto)
                .toList();
    }

    /**
     * Получает пользователя по ID или выбрасывает исключение, если не найден.
     *
     * @param userId идентификатор пользователя
     * @return объект пользователя
     * @throws NotFoundException если пользователь не найден
     */
    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь ID={} не найден", userId);
                    return new NotFoundException("Пользователь не найден");
                });
    }
}