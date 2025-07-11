package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.item.dto.ItemResponseDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSerializeBookingShortDto() throws Exception {
        ItemResponseDto.BookingShortDto dto = ItemResponseDto.BookingShortDto.builder()
                .id(123L)
                .bookerId(456L)
                .start(LocalDateTime.of(2025, 7, 11, 10, 0))
                .end(LocalDateTime.of(2025, 7, 12, 12, 0))
                .build();

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json)
                .contains("\"id\":123")
                .contains("\"bookerId\":456")
                .contains("\"start\":\"2025-07-11T10:00:00\"")
                .contains("\"end\":\"2025-07-12T12:00:00\"");
    }

    @Test
    void testDeserializeBookingShortDto() throws Exception {
        String json = "{\n" +
                      "  \"id\": 123,\n" +
                      "  \"bookerId\": 456,\n" +
                      "  \"start\": \"2025-07-11T10:00:00\",\n" +
                      "  \"end\": \"2025-07-12T12:00:00\"\n" +
                      "}\n";

        ItemResponseDto.BookingShortDto dto = objectMapper.readValue(json, ItemResponseDto.BookingShortDto.class);

        assertThat(dto.getId()).isEqualTo(123L);
        assertThat(dto.getBookerId()).isEqualTo(456L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2025, 7, 11, 10, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2025, 7, 12, 12, 0));
    }
}