package ru.practicum.shareit.request.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

@Mapper(componentModel = "spring")
public interface ItemRequestMapper {

    @Mapping(source = "requestor.id", target = "requestorId")
    ItemRequestResponseDto toDto(ItemRequest itemRequest);

    @Mapping(target = "created", ignore = true)
    @Mapping(source = "requestor", target = "requestor")
    ItemRequest toEntity(ItemRequestDto dto, User requestor);
}
