package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemMapperImpl;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemRequestUpdateDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class ItemMapperImplTest {

    private ItemMapperImpl itemMapper;

    @BeforeEach
    void setUp() {
        itemMapper = new ItemMapperImpl();
    }

    @Test
    void toDto_ShouldMapAllFieldsCorrectly() {
        User owner = User.builder().id(5L).build();
        ItemRequest request = ItemRequest.builder().id(10L).build();

        Item item = Item.builder()
                .id(1L)
                .name("ItemName")
                .description("ItemDescription")
                .available(true)
                .owner(owner)
                .request(request)
                .build();

        ItemResponseDto dto = itemMapper.toDto(item);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(item.getId());
        assertThat(dto.getName()).isEqualTo(item.getName());
        assertThat(dto.getDescription()).isEqualTo(item.getDescription());
        assertThat(dto.getAvailable()).isEqualTo(item.getAvailable());
        assertThat(dto.getOwnerId()).isEqualTo(owner.getId());
        assertThat(dto.getRequestId()).isEqualTo(request.getId());
        assertThat(dto.getComments()).isEqualTo(Collections.emptyList());
    }

    @Test
    void toDto_ShouldReturnNull_WhenItemIsNull() {
        assertThat(itemMapper.toDto(null)).isNull();
    }

    @Test
    void toEntity_ShouldMapAllFieldsCorrectly() {
        ItemRequestDto dto = ItemRequestDto.builder()
                .name("Name")
                .description("Desc")
                .available(true)
                .build();

        User owner = User.builder().id(1L).build();
        ItemRequest request = ItemRequest.builder().id(2L).build();

        Item entity = itemMapper.toEntity(dto, owner, request);

        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo(dto.getName());
        assertThat(entity.getDescription()).isEqualTo(dto.getDescription());
        assertThat(entity.getAvailable()).isEqualTo(dto.getAvailable());
        assertThat(entity.getOwner()).isEqualTo(owner);
        assertThat(entity.getRequest()).isEqualTo(request);
    }

    @Test
    void toEntity_ShouldReturnNull_WhenAllArgumentsNull() {
        assertThat(itemMapper.toEntity(null, null, null)).isNull();
    }

    @Test
    void updateItemFromDto_ShouldUpdateNonNullFields() {
        ItemRequestUpdateDto updateDto = ItemRequestUpdateDto.builder()
                .name("UpdatedName")
                .description(null)
                .available(false)
                .build();

        Item item = Item.builder()
                .name("OldName")
                .description("OldDesc")
                .available(true)
                .build();

        itemMapper.updateItemFromDto(updateDto, item);

        assertThat(item.getName()).isEqualTo("UpdatedName");
        assertThat(item.getDescription()).isEqualTo("OldDesc");
        assertThat(item.getAvailable()).isFalse();
    }

    @Test
    void updateItemFromDto_ShouldDoNothing_WhenDtoIsNull() {
        Item item = Item.builder()
                .name("Name")
                .description("Desc")
                .available(true)
                .build();

        itemMapper.updateItemFromDto(null, item);

        assertThat(item.getName()).isEqualTo("Name");
        assertThat(item.getDescription()).isEqualTo("Desc");
        assertThat(item.getAvailable()).isTrue();
    }
}
