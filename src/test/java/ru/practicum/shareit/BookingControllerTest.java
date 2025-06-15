package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BookingService service;

    @Autowired
    private ObjectMapper om;

    private BookingResponseDto resp;
    private BookingDto req;

    @BeforeEach
    void init() {
        resp = BookingResponseDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(null)
                .booker(null)
                .status("WAITING")
                .build();

        req = BookingDto.builder()
                .itemId(42L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
    }

    @Test
    void createBooking() throws Exception {
        when(service.createBooking(ArgumentMatchers.any(), ArgumentMatchers.eq(7L)))
                .thenReturn(resp);

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(resp.getId()))
                .andExpect(jsonPath("$.status").value(resp.getStatus()));
    }

    @Test
    void approveBooking() throws Exception {
        when(service.approveBooking(1L, 7L, true)).thenReturn(resp);

        mvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 7L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(resp.getId()));
    }

    @Test
    void getBookingById() throws Exception {
        when(service.getBookingById(1L, 7L)).thenReturn(resp);

        mvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(resp.getId()));
    }

    @Test
    void getBookingsByBooker() throws Exception {
        when(service.getBookingsByBooker(7L, "WAITING", 0, 10))
                .thenReturn(List.of(resp));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 7L)
                        .param("state", "WAITING")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(resp.getId()));
    }

    @Test
    void getBookingsByOwner() throws Exception {
        when(service.getBookingsByOwner(7L, "ALL", 0, 10))
                .thenReturn(List.of(resp));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 7L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(resp.getId()));
    }
}