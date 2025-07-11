package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.NewUserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.dto.UserUpdateRequestDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void createUser() throws Exception {
        NewUserRequestDto request = NewUserRequestDto.builder()
                .name("Maxim")
                .email("maxim@example.com")
                .build();

        UserResponseDto response = UserResponseDto.builder()
                .id(1L)
                .name("Maxim")
                .email("maxim@example.com")
                .build();

        Mockito.when(userService.create(any())).thenReturn(response);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.name").value(response.getName()))
                .andExpect(jsonPath("$.email").value(response.getEmail()));

        Mockito.verify(userService, times(1)).create(any());
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    void updateUser() throws Exception {
        Long userId = 1L;

        UserUpdateRequestDto updateDto = UserUpdateRequestDto.builder()
                .name("UpdatedName")
                .email("updated@example.com")
                .build();

        UserResponseDto response = UserResponseDto.builder()
                .id(userId)
                .name("UpdatedName")
                .email("updated@example.com")
                .build();

        Mockito.when(userService.update(eq(userId), any())).thenReturn(response);

        mockMvc.perform(patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("UpdatedName"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));

        Mockito.verify(userService, times(1)).update(eq(userId), any());
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    void getUserById() throws Exception {
        Long userId = 1L;

        UserResponseDto response = UserResponseDto.builder()
                .id(userId)
                .name("Maxim")
                .email("maxim@example.com")
                .build();

        Mockito.when(userService.getById(userId)).thenReturn(response);

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("Maxim"))
                .andExpect(jsonPath("$.email").value("maxim@example.com"));

        Mockito.verify(userService, times(1)).getById(userId);
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    void getAllUsers() throws Exception {
        UserResponseDto user1 = UserResponseDto.builder()
                .id(1L)
                .name("Maxim")
                .email("maxim@example.com")
                .build();

        UserResponseDto user2 = UserResponseDto.builder()
                .id(2L)
                .name("Alice")
                .email("alice@example.com")
                .build();

        Mockito.when(userService.getAll()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(user1.getId()))
                .andExpect(jsonPath("$[1].id").value(user2.getId()));

        Mockito.verify(userService, times(1)).getAll();
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    void deleteUser() throws Exception {
        Long userId = 1L;

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNoContent());

        Mockito.verify(userService, times(1)).delete(userId);
        Mockito.verifyNoMoreInteractions(userService);
    }
}