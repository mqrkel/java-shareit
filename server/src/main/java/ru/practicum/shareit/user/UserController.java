package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.NewUserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.dto.UserUpdateRequestDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

/**
 * REST-контроллер для управления пользователями.
 * Позволяет создавать, обновлять, получать и удалять пользователей.
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Создает нового пользователя.
     *
     * @param newUserRequestDto DTO с данными для создания пользователя.
     * @return DTO созданного пользователя с деталями.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDto create(@RequestBody NewUserRequestDto newUserRequestDto) {
        log.info("POST /users - создание пользователя с email '{}'", newUserRequestDto.getEmail());
        UserResponseDto createdUser = userService.create(newUserRequestDto);
        log.debug("Пользователь создан: {}", createdUser);
        return createdUser;
    }

    /**
     * Обновляет данные пользователя по ID.
     *
     * @param id                   ID пользователя для обновления.
     * @param userUpdateRequestDto DTO с новыми данными пользователя.
     * @return DTO обновленного пользователя.
     */
    @PatchMapping("/{id}")
    public UserResponseDto update(@PathVariable Long id, @RequestBody UserUpdateRequestDto userUpdateRequestDto) {
        log.info("PATCH /users/{} - обновление пользователя", id);
        UserResponseDto updatedUser = userService.update(id, userUpdateRequestDto);
        log.debug("Пользователь обновлен: {}", updatedUser);
        return updatedUser;
    }

    /**
     * Получает пользователя по ID.
     *
     * @param id ID пользователя для получения.
     * @return DTO пользователя с указанным ID.
     */
    @GetMapping("/{id}")
    public UserResponseDto getById(@PathVariable Long id) {
        log.info("GET /users/{} - получение пользователя", id);
        return userService.getById(id);
    }

    /**
     * Получает список всех пользователей.
     *
     * @return Список DTO всех пользователей.
     */
    @GetMapping
    public List<UserResponseDto> getAll() {
        log.info("GET /users - получение списка всех пользователей");
        return userService.getAll();
    }

    /**
     * Удаляет пользователя по ID.
     *
     * @param id ID пользователя для удаления.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        log.info("DELETE /users/{} - удаление пользователя", id);
        userService.delete(id);
        log.debug("Пользователь с ID={} удалён", id);
    }
}