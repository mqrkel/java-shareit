package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService service;

    @Autowired
    private ObjectMapper om;

    private NewUserDto validNew;
    private UserUpdateDto validUpdate;
    private UserResponseDto response;

    @BeforeEach
    void setUp() {
        validNew = NewUserDto.builder()
                .name("Alice")
                .email("alice@example.com")
                .build();

        validUpdate = UserUpdateDto.builder()
                .name("Alice A.")
                .email("alice.a@example.com")
                .build();

        response = UserResponseDto.builder()
                .id(1L)
                .name("Alice")
                .email("alice@example.com")
                .build();
    }

    @Test
    void createUser_Success() throws Exception {
        when(service.create(ArgumentMatchers.any(NewUserDto.class))).thenReturn(response);

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(validNew)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.name").value(response.getName()))
                .andExpect(jsonPath("$.email").value(response.getEmail()));
    }

    @Test
    void getById_Success() throws Exception {
        when(service.getById(1L)).thenReturn(response);

        mvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.name").value(response.getName()));
    }

    @Test
    void getAll_Success() throws Exception {
        when(service.getAll()).thenReturn(List.of(response));

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(response.getId()));
    }

    @Test
    void updateUser_Success() throws Exception {
        UserResponseDto updated = UserResponseDto.builder()
                .id(1L)
                .name(validUpdate.getName())
                .email(validUpdate.getEmail())
                .build();
        when(service.update(1L, validUpdate)).thenReturn(updated);

        mvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(validUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updated.getName()))
                .andExpect(jsonPath("$.email").value(updated.getEmail()));
    }

    @Test
    void deleteUser_Success() throws Exception {
        mvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void createUser_EmptyName_BadRequest() throws Exception {
        NewUserDto dto = NewUserDto.builder().name("").email("a@b.com").build();
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void createUser_InvalidEmail_BadRequest() throws Exception {
        NewUserDto dto = NewUserDto.builder().name("Bob").email("not-an-email").build();
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void createUser_BlankEmail_BadRequest() throws Exception {
        NewUserDto dto = NewUserDto.builder().name("Bob").email("").build();
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void updateUser_NotFound() throws Exception {
        when(service.update(1L, validUpdate))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        mvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(validUpdate)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Пользователь не найден"));
    }

    @Test
    void createUser_ConflictEmail() throws Exception {
        when(service.create(ArgumentMatchers.any(NewUserDto.class)))
                .thenThrow(new ConflictException("Пользователь с email alice@example.com уже существует"));

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(validNew)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }
}