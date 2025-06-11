package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.util.List;

public interface ItemService {
    ItemResponseDto create(ItemDto itemDto, Long ownerId);

    ItemResponseDto update(Long itemId, ItemUpdateDto itemDto, Long ownerId);

    ItemResponseDto getById(Long itemId, Long userId);

    List<ItemResponseDto> getItemsByOwner(Long ownerId);

    List<ItemResponseDto> searchAvailable(String text);
}