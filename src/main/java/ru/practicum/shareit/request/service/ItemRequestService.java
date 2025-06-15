package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestResponseDto create(Long userId, ItemRequestDto dto);

    List<ItemRequestResponseDto> getOwnRequests(Long userId);

    List<ItemRequestResponseDto> getAllRequests(Long userId);
}