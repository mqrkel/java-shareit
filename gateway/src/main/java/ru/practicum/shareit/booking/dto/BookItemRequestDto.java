package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.validation.ValidBookingDates;

import java.time.LocalDateTime;

@Data
@Builder
@ValidBookingDates
public class BookItemRequestDto {
    @NotNull(message = "itemId не может быть null")
    @Positive(message = "itemId должен быть положительным числом")
    private Long itemId;

    @NotNull(message = "start не может быть null")
    @FutureOrPresent
    private LocalDateTime start;

    @NotNull(message = "end не может быть null")
    @Future
    private LocalDateTime end;
}