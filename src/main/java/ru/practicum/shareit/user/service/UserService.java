package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.List;

public interface UserService {
    UserResponseDto create(NewUserDto newUserDto);

    UserResponseDto update(Long id, UserUpdateDto userDto);

    UserResponseDto getById(Long id);

    List<UserResponseDto> getAll();

    void delete(Long id);
}