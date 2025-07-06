package ru.practicum.shareit.user.dto;

import org.mapstruct.*;
import ru.practicum.shareit.user.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    User toEntity(NewUserDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateUserFromDto(UserUpdateDto dto, @MappingTarget User user);
}
