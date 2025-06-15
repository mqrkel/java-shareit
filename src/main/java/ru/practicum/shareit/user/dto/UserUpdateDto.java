package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserUpdateDto {
    @Size(min = 1, message = "Name must not be empty")
    private String name;

    @Email(message = "Email must be valid")
    private String email;
}