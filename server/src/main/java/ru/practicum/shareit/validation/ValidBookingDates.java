package ru.practicum.shareit.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = BookingDatesValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBookingDates {
    String message() default "Дата окончания бронирования должна быть позже даты начала";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}