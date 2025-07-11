package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BookingMapperTest {

    @Autowired
    private BookingMapper bookingMapper;

    @Test
    void toDto_shouldMapBookingToBookingResponseDto() {
        User booker = User.builder()
                .id(10L)
                .name("Max")
                .email("max@mail.com")
                .build();

        Item item = Item.builder()
                .id(20L)
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .owner(booker)
                .build();

        Booking booking = Booking.builder()
                .id(100L)
                .start(LocalDateTime.of(2025, 7, 7, 10, 0))
                .end(LocalDateTime.of(2025, 7, 8, 10, 0))
                .booker(booker)
                .item(item)
                .status(Booking.BookingStatus.WAITING)
                .build();

        BookingResponseDto dto = bookingMapper.toDto(booking);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getStart()).isEqualTo(booking.getStart());
        assertThat(dto.getEnd()).isEqualTo(booking.getEnd());
        assertThat(dto.getStatus()).isEqualTo("WAITING");

        assertThat(dto.getItem()).isNotNull();
        assertThat(dto.getItem().getId()).isEqualTo(item.getId());
        assertThat(dto.getItem().getName()).isEqualTo(item.getName());

        assertThat(dto.getBooker()).isNotNull();
        assertThat(dto.getBooker().getId()).isEqualTo(booker.getId());
        assertThat(dto.getBooker().getName()).isEqualTo(booker.getName());
    }

    @Test
    void toEntity_shouldMapBookingRequestDtoToBooking() {
        User booker = User.builder()
                .id(10L)
                .name("Max")
                .email("max@mail.com")
                .build();

        Item item = Item.builder()
                .id(20L)
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .owner(booker)
                .build();

        BookingRequestDto dto = BookingRequestDto.builder()
                .start(LocalDateTime.of(2025, 7, 7, 10, 0))
                .end(LocalDateTime.of(2025, 7, 8, 10, 0))
                .itemId(item.getId())
                .build();

        Booking booking = bookingMapper.toEntity(dto, item, booker);

        assertThat(booking).isNotNull();
        assertThat(booking.getStart()).isEqualTo(dto.getStart());
        assertThat(booking.getEnd()).isEqualTo(dto.getEnd());
        assertThat(booking.getItem()).isEqualTo(item);
        assertThat(booking.getBooker()).isEqualTo(booker);
    }

    @Test
    void toEntity_shouldReturnNull_whenAllParamsNull() {
        Booking booking = bookingMapper.toEntity(null, null, null);
        assertThat(booking).isNull();
    }
}