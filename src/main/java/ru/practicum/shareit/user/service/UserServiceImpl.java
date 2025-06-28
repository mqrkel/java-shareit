package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponseDto create(NewUserDto newUserDto) {
        if (userRepository.findByEmail(newUserDto.getEmail()).isPresent()) {
            throw new ConflictException("Пользователь с email " + newUserDto.getEmail() + " уже существует");
        }
        User user = userMapper.toEntity(newUserDto);
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponseDto update(Long id, UserUpdateDto userDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        if (userDto.getEmail() != null && !userDto.getEmail().equals(existingUser.getEmail())) {
            userRepository.findByEmail(userDto.getEmail())
                    .ifPresent(u -> {
                        throw new ConflictException("Email уже используется другим пользователем");
                    });
        }

        userMapper.updateUserFromDto(userDto, existingUser);
        return userMapper.toDto(userRepository.save(existingUser));
    }

    @Override
    public UserResponseDto getById(Long id) {
        return userMapper.toDto(userRepository.findById(id).orElseThrow(() -> new NotFoundException("Пользователь не найден")));
    }

    @Override
    public List<UserResponseDto> getAll() {
        return userRepository.findAll().stream().map(userMapper::toDto).toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}