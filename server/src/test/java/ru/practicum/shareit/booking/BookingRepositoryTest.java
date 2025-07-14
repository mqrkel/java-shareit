package ru.practicum.shareit.booking;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static ru.practicum.shareit.booking.Booking.BookingStatus.APPROVED;

@FieldDefaults(level = AccessLevel.PRIVATE)
@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class BookingRepositoryTest {

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    private TestEntityManager testEntityManager;
    final User user = User.builder()
            .name("name")
            .email("email@email.com")
            .build();

    final User owner = User.builder()
            .name("name2")
            .email("email2@email.com")
            .build();

    final Item item = Item.builder()
            .name("name")
            .description("description")
            .available(true)
            .owner(owner)
            .build();

    final Booking booking = Booking.builder()
            .item(item)
            .booker(user)
            .status(APPROVED)
            .start(LocalDateTime.now().minusHours(1))
            .end(LocalDateTime.now().plusDays(1))
            .build();

    final Booking pastBooking = Booking.builder()
            .item(item)
            .booker(user)
            .status(APPROVED)
            .start(LocalDateTime.now().minusDays(2))
            .end(LocalDateTime.now().minusDays(1))
            .build();

    final Booking futureBooking = Booking.builder()
            .item(item)
            .booker(user)
            .status(APPROVED)
            .start(LocalDateTime.now().plusDays(1))
            .end(LocalDateTime.now().plusDays(2))
            .build();

    final Booking waitingBooking = Booking.builder()
            .item(item)
            .booker(user)
            .status(Booking.BookingStatus.WAITING)
            .start(LocalDateTime.now().plusHours(3))
            .end(LocalDateTime.now().plusHours(5))
            .build();

    final Booking rejectedBooking = Booking.builder()
            .item(item)
            .booker(user)
            .status(Booking.BookingStatus.REJECTED)
            .start(LocalDateTime.now().plusHours(6))
            .end(LocalDateTime.now().plusHours(8))
            .build();

    @BeforeEach
    public void init() {
        testEntityManager.persist(user);
        testEntityManager.persist(owner);
        testEntityManager.persist(item);

        testEntityManager.flush();
        bookingRepository.save(booking);
        bookingRepository.save(pastBooking);
        bookingRepository.save(futureBooking);
        bookingRepository.save(waitingBooking);
        bookingRepository.save(rejectedBooking);
    }

    @AfterEach
    public void deleteAll() {
        bookingRepository.deleteAll();
    }

    @Test
    void findAllByBookerId() {
        var result = bookingRepository.findByBookerIdOrderByStartDesc(user.getId(), Pageable.ofSize(10));

        assertThat(result).hasSize(5);
        assertThat(result.get(0).getStart()).isAfter(result.get(1).getStart());
    }

    @Test
    void findAllCurrentBookingsByBookerId() {
        var now = LocalDateTime.now();
        var result = bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                user.getId(), now, now, Pageable.ofSize(10)
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(booking.getId());
    }

    @Test
    void findAllPastBookingsByBookerId() {
        var now = LocalDateTime.now();
        var result = bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(
                user.getId(), now, Pageable.ofSize(10));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(pastBooking.getId());
    }


    @Test
    void findAllFutureBookingsByBookerId() {
        var now = LocalDateTime.now();
        var result = bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(
                user.getId(), now, Pageable.ofSize(10));

        assertThat(result).hasSize(3);
        assertThat(result).extracting(Booking::getId)
                .contains(futureBooking.getId(), waitingBooking.getId(), rejectedBooking.getId());
    }

    @Test
    void findAllApprovedBookingsByBookerId() {
        var result = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                user.getId(), APPROVED, Pageable.ofSize(10)
        );

        assertThat(result).hasSize(3); // все 3 с APPROVED
    }


    @Test
    void findAllWaitingBookingsByBookerId() {
        var result = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                user.getId(), Booking.BookingStatus.WAITING, Pageable.ofSize(10));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(Booking.BookingStatus.WAITING);
    }

    @Test
    void findAllRejectedBookingsByBookerId() {
        var result = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                user.getId(), Booking.BookingStatus.REJECTED, Pageable.ofSize(10));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(Booking.BookingStatus.REJECTED);
    }


    @Test
    void findAllByOwnerId() {
        var result = bookingRepository.findByOwnerId(owner.getId(), Pageable.ofSize(10));

        assertThat(result).hasSize(5);
        assertThat(result.get(0).getItem().getOwner().getId()).isEqualTo(owner.getId());
    }


    @Test
    void findAllCurrentBookingsByOwnerId() {
        var now = LocalDateTime.now();
        var result = bookingRepository.findCurrentByOwnerId(owner.getId(), now, Pageable.ofSize(10));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(booking.getId());
    }


    @Test
    void findAllPastBookingsByOwnerId() {
        var now = LocalDateTime.now();
        var result = bookingRepository.findPastByOwnerId(owner.getId(), now, Pageable.ofSize(10));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(pastBooking.getId());
    }

    @Test
    void findAllFutureBookingsByOwnerId() {
        var now = LocalDateTime.now();
        var result = bookingRepository.findFutureByOwnerId(owner.getId(), now, Pageable.ofSize(10));

        assertThat(result).hasSize(3);
        assertThat(result).extracting(Booking::getId)
                .contains(futureBooking.getId(), waitingBooking.getId(), rejectedBooking.getId());
    }

    @Test
    void findAllByItemIdOrderByStartAsc() {
        var result = bookingRepository.findByItemIdOrderByStartAsc(item.getId());

        assertThat(result).hasSize(5);
        assertThat(result.get(0).getStart()).isBefore(result.get(1).getStart());
    }

    @Test
    void findAllWaitingBookingsByOwnerId() {
        var result = bookingRepository.findByOwnerIdAndStatus(owner.getId(), Booking.BookingStatus.WAITING, Pageable.ofSize(10));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(Booking.BookingStatus.WAITING);
    }

    @Test
    void findAllRejectedBookingsByOwnerId() {
        var result = bookingRepository.findByOwnerIdAndStatus(owner.getId(), Booking.BookingStatus.REJECTED, Pageable.ofSize(10));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(Booking.BookingStatus.REJECTED);
    }
}