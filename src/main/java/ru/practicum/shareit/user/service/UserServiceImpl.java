package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;

    @Override
    public UserResponseDto create(NewUserDto newUserDto) {
        User user = UserMapper.toEntity(newUserDto);
        return UserMapper.toDto(userStorage.save(user));
    }

    @Override
    public UserResponseDto update(Long id, UserUpdateDto userDto) {
        User existingUser = userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        User updated = UserMapper.merge(existingUser, userDto);
        return UserMapper.toDto(userStorage.update(updated));
    }

    @Override
    public UserResponseDto getById(Long id) {
        return UserMapper.toDto(userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден")));
    }

    @Override
    public List<UserResponseDto> getAll() {
        return userStorage.findAll().stream()
                .map(UserMapper::toDto)
                .toList();
    }

    @Override
    public void delete(Long id) {
        userStorage.deleteById(id);
    }
}