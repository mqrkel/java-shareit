package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.UserController;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ShareItTests {

    @Autowired
    private ItemController itemController;
    @Autowired
    private ItemService itemService;

    @Autowired
    private UserController userController;

    @Autowired
    private ItemRequestController itemRequestController;
    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private BookingController bookingController;
    @Autowired
    private BookingService bookingService;

    @Test
    void contextLoads() {
        assertThat(itemController).isNotNull();
        assertThat(itemService).isNotNull();
        assertThat(userController).isNotNull();
        assertThat(itemRequestController).isNotNull();
        assertThat(itemRequestService).isNotNull();
        assertThat(bookingController).isNotNull();
        assertThat(bookingService).isNotNull();
    }
}
