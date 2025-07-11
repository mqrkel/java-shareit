package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.NewUserRequestDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.dto.UserUpdateRequestDto;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserResponseDto responseDto;
    private NewUserRequestDto newUserDto;
    private UserUpdateRequestDto updateDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Максим")
                .email("maxim@example.com")
                .build();

        responseDto = UserResponseDto.builder()
                .id(1L)
                .name("Максим")
                .email("maxim@example.com")
                .build();

        newUserDto = NewUserRequestDto.builder()
                .name("Максим")
                .email("maxim@example.com")
                .build();

        updateDto = UserUpdateRequestDto.builder()
                .name("Обновлённый")
                .email("maxim@example.com")
                .build();
    }

    @Test
    void create_whenEmailNotExists_thenCreateUser() {
        when(userRepository.findByEmail(newUserDto.getEmail())).thenReturn(Optional.empty());
        when(userMapper.toEntity(newUserDto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(responseDto);

        UserResponseDto result = userService.create(newUserDto);

        assertThat(result).isEqualTo(responseDto);

        verify(userRepository).findByEmail(newUserDto.getEmail());
        verify(userMapper).toEntity(newUserDto);
        verify(userRepository).save(user);
        verify(userMapper).toDto(user);
    }

    @Test
    void create_whenEmailExists_thenThrowConflict() {
        when(userRepository.findByEmail(newUserDto.getEmail())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.create(newUserDto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("уже существует");

        verify(userRepository).findByEmail(newUserDto.getEmail());
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    void update_whenUserExists_thenUpdateUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(responseDto);

        UserResponseDto result = userService.update(1L, updateDto);

        assertThat(result).isEqualTo(responseDto);

        verify(userRepository).findById(1L);
        verify(userMapper).updateUserFromDto(updateDto, user);
        verify(userRepository).save(user);
        verify(userMapper).toDto(user);
    }

    @Test
    void update_whenUserNotFound_thenThrowNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(999L, updateDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден");

        verify(userRepository).findById(999L);
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    void update_whenEmailAlreadyUsed_thenThrowConflict() {
        User anotherUser = User.builder().id(2L).email("other@example.com").build();
        updateDto.setEmail("other@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(anotherUser));

        assertThatThrownBy(() -> userService.update(1L, updateDto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("уже используется");

        verify(userRepository).findById(1L);
        verify(userRepository).findByEmail("other@example.com");
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    void getById_whenUserExists_thenReturnDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(responseDto);

        UserResponseDto result = userService.getById(1L);

        assertThat(result).isEqualTo(responseDto);

        verify(userRepository).findById(1L);
        verify(userMapper).toDto(user);
    }

    @Test
    void getById_whenUserNotFound_thenThrowNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден");

        verify(userRepository).findById(1L);
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    void getAll_shouldReturnList() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toDto(user)).thenReturn(responseDto);

        List<UserResponseDto> all = userService.getAll();

        assertThat(all).hasSize(1);
        assertThat(all.get(0)).isEqualTo(responseDto);

        verify(userRepository).findAll();
        verify(userMapper).toDto(user);
    }

    @Test
    void delete_shouldCallRepositoryDelete() {
        userService.delete(1L);

        verify(userRepository).deleteById(1L);
    }
}