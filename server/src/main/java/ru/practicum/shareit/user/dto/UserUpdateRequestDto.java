package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserUpdateRequestDto {
    private String name;
    private String email;
}