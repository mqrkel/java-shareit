package ru.practicum.shareit.request.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemRequestMapper {

    public static ItemRequestResponseDto toDto(ItemRequest itemRequest) {
        if (itemRequest == null) {
            return null;
        }
        return ItemRequestResponseDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .requestorId(itemRequest.getRequestor() != null ? itemRequest.getRequestor().getId() : null)
                .created(itemRequest.getCreated())
                .build();
    }

    public static ItemRequest toEntity(ItemRequestDto dto, User requestor) {
        if (dto == null) {
            return null;
        }
        return ItemRequest.builder()
                .description(dto.getDescription())
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build();
    }
}