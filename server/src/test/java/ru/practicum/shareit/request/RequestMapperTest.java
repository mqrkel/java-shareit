package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RequestMapperTest {

    private final ItemRequestMapper mapper = Mappers.getMapper(ItemRequestMapper.class);

    @Test
    void toDto_ShouldMapEntityToDto() {
        User user = User.builder()
                .id(100L)
                .name("Maxim")
                .email("maxim@example.com")
                .build();

        ItemRequest request = ItemRequest.builder()
                .id(1L)
                .description("Test description")
                .requestor(user)
                .created(LocalDateTime.now())
                .build();

        ItemRequestResponseDto dto = mapper.toDto(request);

        assertThat(dto).isNotNull();
        assertThat(dto.getRequestorId()).isEqualTo(user.getId());
        assertThat(dto.getDescription()).isEqualTo(request.getDescription());
        assertThat(dto.getId()).isEqualTo(request.getId());
    }

    @Test
    void toEntity_ShouldMapDtoToEntity() {
        User user = User.builder()
                .id(100L)
                .name("Maxim")
                .email("maxim@example.com")
                .build();

        ItemRequestDto dto = ItemRequestDto.builder()
                .description("Test description")
                .build();

        ItemRequest entity = mapper.toEntity(dto, user);

        assertThat(entity).isNotNull();
        assertThat(entity.getDescription()).isEqualTo(dto.getDescription());
        assertThat(entity.getRequestor()).isEqualTo(user);
        assertThat(entity.getCreated()).isNull();
    }

    @Test
    void toDto_ShouldMapEntityToDto_IncludingCreatedTime() {
        LocalDateTime createdTime = LocalDateTime.of(2025, 7, 9, 12, 34, 56);

        User user = User.builder()
                .id(100L)
                .name("Maxim")
                .email("maxim@example.com")
                .build();

        ru.practicum.shareit.request.ItemRequest request = ru.practicum.shareit.request.ItemRequest.builder()
                .id(1L)
                .description("Test description")
                .requestor(user)
                .created(createdTime)
                .build();

        ItemRequestResponseDto dto = mapper.toDto(request);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(request.getId());
        assertThat(dto.getDescription()).isEqualTo(request.getDescription());
        assertThat(dto.getRequestorId()).isEqualTo(user.getId());
        assertThat(dto.getCreated()).isEqualTo(createdTime);
    }
}