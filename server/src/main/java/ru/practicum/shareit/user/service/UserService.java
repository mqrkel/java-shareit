package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.NewUserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.dto.UserUpdateRequestDto;

import java.util.List;

public interface UserService {
    UserResponseDto create(NewUserRequestDto newUserRequestDto);

    UserResponseDto update(Long id, UserUpdateRequestDto userDto);

    UserResponseDto getById(Long id);

    List<UserResponseDto> getAll();

    void delete(Long id);
}