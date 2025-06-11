package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UserResponseDto;

import java.time.LocalDateTime;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShareItIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    HttpHeaders ownerHeaders;
    HttpHeaders bookerHeaders;

    @BeforeEach
    void setup() {
        ownerHeaders = new HttpHeaders();
        ownerHeaders.setContentType(MediaType.APPLICATION_JSON);

        bookerHeaders = new HttpHeaders();
        bookerHeaders.setContentType(MediaType.APPLICATION_JSON);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private NewUserDto newUser(String name, String email) {
        return NewUserDto.builder()
                .name(name)
                .email(email)
                .build();
    }

    @Test
    void fullFlow_UserItemBooking() {
        // 1) Create owner
        ResponseEntity<UserResponseDto> ownerResp = rest.exchange(
                url("/users"),
                HttpMethod.POST,
                new HttpEntity<>(newUser("Owner", "owner@example.com"), ownerHeaders),
                UserResponseDto.class);
        assertThat(ownerResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long ownerId = Objects.requireNonNull(ownerResp.getBody()).getId();
        ownerHeaders.set("X-Sharer-User-Id", ownerId.toString());

        // 2) Create booker
        ResponseEntity<UserResponseDto> bookerResp = rest.exchange(
                url("/users"),
                HttpMethod.POST,
                new HttpEntity<>(newUser("Booker", "booker@example.com"), ownerHeaders),
                UserResponseDto.class);
        assertThat(bookerResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long bookerId = Objects.requireNonNull(bookerResp.getBody()).getId();
        bookerHeaders.set("X-Sharer-User-Id", bookerId.toString());

        // 3) Owner creates item
        ItemDto newItem = ItemDto.builder()
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .ownerId(ownerId)
                .build();
        ResponseEntity<ItemResponseDto> itemResp = rest.exchange(
                url("/items"),
                HttpMethod.POST,
                new HttpEntity<>(newItem, ownerHeaders),
                ItemResponseDto.class);
        assertThat(itemResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        Long itemId = Objects.requireNonNull(itemResp.getBody()).getId();

        // 4) Booker creates booking
        BookingDto bookingRequest = BookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        ResponseEntity<BookingResponseDto> bookingResp = rest.exchange(
                url("/bookings"),
                HttpMethod.POST,
                new HttpEntity<>(bookingRequest, bookerHeaders),
                BookingResponseDto.class);
        assertThat(bookingResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        Long bookingId = Objects.requireNonNull(bookingResp.getBody()).getId();

        // 5) Owner approves booking
        ResponseEntity<BookingResponseDto> approveResp = rest.exchange(
                url("/bookings/" + bookingId + "?approved=true"),
                HttpMethod.PATCH,
                new HttpEntity<>(null, ownerHeaders),
                BookingResponseDto.class);
        assertThat(approveResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(approveResp.getBody()).getStatus()).isEqualTo("APPROVED");

        // 6) Booker fetches booking
        ResponseEntity<BookingResponseDto> fetchResp = rest.exchange(
                url("/bookings/" + bookingId),
                HttpMethod.GET,
                new HttpEntity<>(null, bookerHeaders),
                BookingResponseDto.class);
        assertThat(fetchResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(fetchResp.getBody()).getId()).isEqualTo(bookingId);

        // 7) Booker lists own bookings
        ResponseEntity<BookingResponseDto[]> listByBooker = rest.exchange(
                url("/bookings?state=ALL&from=0&size=10"),
                HttpMethod.GET,
                new HttpEntity<>(null, bookerHeaders),
                BookingResponseDto[].class);
        assertThat(listByBooker.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listByBooker.getBody()).isNotEmpty();

        // 8) Owner lists bookings on own items
        ResponseEntity<BookingResponseDto[]> listByOwner = rest.exchange(
                url("/bookings/owner?state=ALL&from=0&size=10"),
                HttpMethod.GET,
                new HttpEntity<>(null, ownerHeaders),
                BookingResponseDto[].class);
        assertThat(listByOwner.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listByOwner.getBody()).isNotEmpty();
    }
}