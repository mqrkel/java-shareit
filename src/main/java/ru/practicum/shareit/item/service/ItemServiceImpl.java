package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ItemRequestRepository requestRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public ItemResponseDto create(ItemDto itemDto, Long ownerId) {
        log.info("Создание новой вещи: {}, владелец ID={}", itemDto.getName(), ownerId);
        User owner = getUserOrThrow(ownerId);
        ItemRequest request = itemDto.getRequestId() == null ? null :
                requestRepository.findById(itemDto.getRequestId()).orElse(null);

        Item item = ItemMapper.toEntity(itemDto, owner, request);
        Item savedItem = itemRepository.save(item);
        log.debug("Вещь успешно создана: {}", savedItem);
        return ItemMapper.toDto(savedItem);
    }


    @Override
    @Transactional
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
        log.debug("Вещь успешно обновлена: {}", item);
        return ItemMapper.toDto(item);
    }

    @Override
    public ItemResponseDto getById(Long itemId, Long userId) {
        log.info("Получение вещи ID={} пользователем ID={}", itemId, userId);
        Item item = getItemOrThrow(itemId);

        List<Comment> comments = commentRepository.findByItemId(itemId);
        List<CommentResponseDto> commentDtos = comments.stream()
                .map(CommentMapper::toDto)
                .toList();

        ItemResponseDto dto = ItemMapper.toDto(item);
        dto.setComments(commentDtos);

        return dto;
    }

    @Override
    public List<ItemResponseDto> getItemsByOwner(Long ownerId) {
        log.info("Получение всех вещей пользователя ID={}", ownerId);
        getUserOrThrow(ownerId);
        List<Item> items = itemRepository.findByOwnerId(ownerId);
        LocalDateTime now = LocalDateTime.now();

        return items.stream().map(item -> {
            List<Booking> bookings = bookingRepository.findByItemIdOrderByStartAsc(item.getId());
            List<Comment> comments = commentRepository.findByItemId(item.getId());

            ItemResponseDto dto = ItemMapper.toDto(item);

            dto.setLastBooking(bookings.stream()
                    .filter(b -> b.getEnd().isBefore(now))
                    .max(Comparator.comparing(Booking::getEnd))
                    .map(this::convertToShortDto)
                    .orElse(null));

            dto.setNextBooking(bookings.stream()
                    .filter(b -> b.getStart().isAfter(now))
                    .min(Comparator.comparing(Booking::getStart))
                    .map(this::convertToShortDto)
                    .orElse(null));

            dto.setComments(comments.stream()
                    .map(CommentMapper::toDto)
                    .toList());

            return dto;
        }).toList();
    }


    @Override
    public List<ItemResponseDto> searchAvailable(String text) {
        log.info("Поиск доступных вещей по тексту: '{}'", text);
        if (text == null || text.isBlank()) {
            log.debug("Пустой текст поиска, возвращён пустой список");
            return List.of();
        }
        List<Item> items = itemRepository.searchAvailableByText(text);
        log.debug("Найдено {} вещей по запросу '{}'", items.size(), text);
        return items.stream().map(ItemMapper::toDto).toList();
    }

    @Transactional
    public CommentResponseDto addComment(Long itemId, Long userId, CommentDto dto) {
        Item item = getItemOrThrow(itemId);
        User author = getUserOrThrow(userId);

        boolean hasPastBooking = bookingRepository.findByItemIdOrderByStartAsc(itemId).stream()
                .anyMatch(b -> b.getBooker().getId().equals(userId) && b.getEnd().isBefore(LocalDateTime.now()));
        if (!hasPastBooking) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Оставить комментарий может только пользователь, бравший вещь в аренду.");
        }
        Comment comment = Comment.builder()
                .text(dto.getText())
                .author(author)
                .item(item)
                .created(LocalDateTime.now())
                .build();

        Comment saved = commentRepository.save(comment);
        return CommentMapper.toDto(saved);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь ID={} не найден", userId);
                    return new NotFoundException("Пользователь не найден");
                });
    }

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Вещь ID={} не найдена", itemId);
                    return new NotFoundException("Вещь не найдена");
                });
    }

    private ItemResponseDto.BookingShortDto convertToShortDto(Booking booking) {
        return ItemResponseDto.BookingShortDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .build();
    }
}