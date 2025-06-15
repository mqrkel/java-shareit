package ru.practicum.shareit.user.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {

    public static UserResponseDto toDto(User user) {
        if (user == null) {
            return null;
        }
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static User toEntity(NewUserDto dto) {
        if (dto == null) {
            return null;
        }
        return User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .build();
    }

    public static User merge(User existingUser, UserUpdateDto dto) {
        if (dto == null) {
            return existingUser;
        }
        if (dto.getName() != null) {
            existingUser.setName(dto.getName());
        }
        if (dto.getEmail() != null) {
            existingUser.setEmail(dto.getEmail());
        }
        return existingUser;
    }
}