package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserResponseDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.booking.Booking.BookingStatus.WAITING;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final User user = User.builder()
            .id(1L)
            .name("Name")
            .email("name@email.com")
            .build();

    private final Item item = Item.builder()
            .id(1L)
            .name("ItemName")
            .description("ItemDescription")
            .owner(user)
            .build();

    private final BookingRequestDto bookingRequestDto = BookingRequestDto.builder()
            .itemId(1L)
            .start(LocalDateTime.now().minusMonths(1))
            .end(LocalDateTime.now().plusMonths(2))
            .build();

    private final BookingResponseDto bookingResponseDto = BookingResponseDto.builder()
            .id(1L)
            .start(bookingRequestDto.getStart())
            .end(bookingRequestDto.getEnd())
            .item(ItemResponseDto.builder()
                    .id(item.getId())
                    .name(item.getName())
                    .description(item.getDescription())
                    .available(true)
                    .ownerId(user.getId())
                    .build())
            .booker(UserResponseDto.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .build())
            .status(WAITING.toString())
            .build();

    @Test
    @SneakyThrows
    void shouldCreateBooking() {
        when(bookingService.createBooking(bookingRequestDto, user.getId())).thenReturn(bookingResponseDto);

        var result = mvc.perform(post("/bookings")
                        .content(objectMapper.writeValueAsString(bookingRequestDto))
                        .contentType("application/json")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(USER_ID_HEADER, user.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(bookingResponseDto), result);

        verify(bookingService, times(1)).createBooking(bookingRequestDto, user.getId());
    }

    @Test
    @SneakyThrows
    void approveBooking() {
        Long bookingId = 1L;
        Long ownerId = user.getId();
        boolean approved = true;

        when(bookingService.approveBooking(bookingId, ownerId, approved)).thenReturn(bookingResponseDto);

        var result = mvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .param("approved", String.valueOf(approved))
                        .header(USER_ID_HEADER, ownerId)
                        .contentType("application/json")
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(bookingResponseDto), result);

        verify(bookingService, times(1)).approveBooking(bookingId, ownerId, approved);
    }

    @Test
    @SneakyThrows
    void getBookingById() {
        Long bookingId = 1L;
        Long userId = user.getId();

        when(bookingService.getBookingById(bookingId, userId)).thenReturn(bookingResponseDto);

        var result = mvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(USER_ID_HEADER, userId)
                        .contentType("application/json")
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(bookingResponseDto), result);

        verify(bookingService, times(1)).getBookingById(bookingId, userId);
    }

    @Test
    @SneakyThrows
    void getBookingsByBooker() {
        Long userId = user.getId();

        var bookingsList = List.of(bookingResponseDto);
        when(bookingService.getBookingsByBooker(userId, "ALL", 0, 10)).thenReturn(bookingsList);

        var result = mvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, userId)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10")
                        .contentType("application/json")
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(bookingsList), result);

        verify(bookingService, times(1)).getBookingsByBooker(userId, "ALL", 0, 10);
    }

    @Test
    @SneakyThrows
    void getBookingsByOwner() {
        Long ownerId = user.getId();

        var bookingsList = List.of(bookingResponseDto);
        when(bookingService.getBookingsByOwner(ownerId, "ALL", 0, 10)).thenReturn(bookingsList);

        var result = mvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, ownerId)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10")
                        .contentType("application/json")
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(bookingsList), result);

        verify(bookingService, times(1)).getBookingsByOwner(ownerId, "ALL", 0, 10);
    }

    @Test
    @SneakyThrows
    void approveBooking_whenConflict_shouldReturn409() {
        Long bookingId = 1L;
        Long ownerId = user.getId();

        when(bookingService.approveBooking(eq(bookingId), eq(ownerId), anyBoolean()))
                .thenThrow(new ConflictException("Бронирование уже обработано"));

        mvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .param("approved", "true")
                        .header(USER_ID_HEADER, ownerId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value(containsString("Бронирование уже обработано")));
    }

    @Test
    @SneakyThrows
    void createBooking_shouldReturnNotFound_whenBookingServiceThrowsNotFoundException() {
        when(bookingService.createBooking(any(), anyLong()))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        mvc.perform(post("/bookings")
                        .content(objectMapper.writeValueAsString(bookingRequestDto))
                        .contentType("application/json")
                        .header(USER_ID_HEADER, user.getId()))
                .andExpect(status().isNotFound())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    assertTrue(json.contains("NOT_FOUND"));
                    assertTrue(json.contains("Пользователь не найден"));
                });

        verify(bookingService, times(1)).createBooking(any(), anyLong());
    }

    @Test
    @SneakyThrows
    void createBooking_shouldReturnBadRequest_whenBookingServiceThrowsValidationException() {
        when(bookingService.createBooking(any(), anyLong()))
                .thenThrow(new ValidationException("Некорректные даты бронирования"));

        mvc.perform(post("/bookings")
                        .content(objectMapper.writeValueAsString(bookingRequestDto))
                        .contentType("application/json")
                        .header(USER_ID_HEADER, user.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    assertTrue(json.contains("VALIDATION_ERROR"));
                    assertTrue(json.contains("Некорректные даты бронирования"));
                });

        verify(bookingService, times(1)).createBooking(any(), anyLong());
    }
}