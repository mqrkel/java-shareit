package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JsonTest
class BookingDtoTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSerializeBookingRequestDto() throws Exception {
        BookingRequestDto dto = BookingRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.of(2025, 7, 7, 14, 30))
                .end(LocalDateTime.of(2025, 7, 10, 18, 0))
                .build();

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("2025-07-07T14:30:00");
        assertThat(json).contains("2025-07-10T18:00:00");

        System.out.println("Serialized JSON: " + json);
    }

    @Test
    void testDeserializeBookingRequestDto() throws Exception {
        String json = "{"
                      + "\"itemId\":1,"
                      + "\"start\":\"2025-07-07T14:30:00\","
                      + "\"end\":\"2025-07-10T18:00:00\""
                      + "}";


        BookingRequestDto dto = objectMapper.readValue(json, BookingRequestDto.class);

        assertThat(dto.getItemId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2025, 7, 7, 14, 30));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2025, 7, 10, 18, 0));
    }
}