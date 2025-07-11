package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.*;

import java.util.List;

public interface ItemService {
    ItemResponseDto create(ItemRequestDto itemRequestDto, Long ownerId);

    ItemResponseDto update(Long itemId, ItemRequestUpdateDto itemDto, Long ownerId);

    ItemResponseDto getById(Long itemId, Long userId);

    List<ItemResponseDto> getItemsByOwner(Long ownerId);

    List<ItemResponseDto> searchAvailable(String text);

    CommentResponseDto addComment(Long itemId, Long userId, CommentRequestDto dto);
}