package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ItemRequestResponseDto {
    Long id;
    String description;
    Long requestorId;
    LocalDateTime created;
}