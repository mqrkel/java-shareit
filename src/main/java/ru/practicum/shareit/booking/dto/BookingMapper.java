package ru.practicum.shareit.booking.dto;

import org.mapstruct.*;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

@Mapper(
        componentModel = "spring",
        uses = {
                ru.practicum.shareit.item.dto.ItemMapper.class,
                ru.practicum.shareit.user.dto.UserMapper.class
        }
)
public interface BookingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "booker", target = "booker")
    @Mapping(source = "item", target = "item")
    @Mapping(source = "dto.start", target = "start")
    @Mapping(source = "dto.end", target = "end")
    Booking toEntity(BookingDto dto, Item item, User booker);

    @Mapping(source = "status", target = "status", qualifiedByName = "statusToString")
    BookingResponseDto toDto(Booking booking);

    @Named("statusToString")
    static String statusToString(Enum<?> status) {
        return status != null ? status.name() : null;
    }
}