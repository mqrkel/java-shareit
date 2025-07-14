package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.user.dto.NewUserRequestDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.dto.UserUpdateRequestDto;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    private NewUserRequestDto newUserDto;
    private UserUpdateRequestDto updateDto;

    @BeforeEach
    void setUp() {
        newUserDto = NewUserRequestDto.builder()
                .name("Максим")
                .email("max@example.com")
                .build();

        updateDto = UserUpdateRequestDto.builder()
                .name("Обновлённый")
                .email("updated@example.com")
                .build();
    }

    @Test
    void shouldMapToEntityFromNewUserDto() {
        User user = mapper.toEntity(newUserDto);

        assertThat(user).isNotNull();
        assertThat(user.getId()).isNull();
        assertThat(user.getName()).isEqualTo(newUserDto.getName());
        assertThat(user.getEmail()).isEqualTo(newUserDto.getEmail());
    }

    @Test
    void shouldMapToDtoFromUser() {
        User user = User.builder()
                .id(42L)
                .name("Имя")
                .email("email@test.com")
                .build();

        UserResponseDto dto = mapper.toDto(user);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(user.getId());
        assertThat(dto.getName()).isEqualTo(user.getName());
        assertThat(dto.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void shouldUpdateUserFromDto() {
        User user = User.builder()
                .id(1L)
                .name("Старое имя")
                .email("old@example.com")
                .build();

        mapper.updateUserFromDto(updateDto, user);

        assertThat(user.getName()).isEqualTo(updateDto.getName());
        assertThat(user.getEmail()).isEqualTo(updateDto.getEmail());
        assertThat(user.getId()).isEqualTo(1L);
    }

    @Test
    void shouldIgnoreNullsOnUpdate() {
        User user = User.builder()
                .id(1L)
                .name("Старое имя")
                .email("old@example.com")
                .build();

        UserUpdateRequestDto partialUpdate = UserUpdateRequestDto.builder().name(null).email(null).build();

        mapper.updateUserFromDto(partialUpdate, user);

        assertThat(user.getName()).isEqualTo("Старое имя");
        assertThat(user.getEmail()).isEqualTo("old@example.com");
    }
}