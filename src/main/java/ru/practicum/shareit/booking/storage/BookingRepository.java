package ru.practicum.shareit.booking.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.Booking.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId, Pageable pageable);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status, Pageable pageable);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long bookerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime end, Pageable pageable);

    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime start, Pageable pageable);

    @Query("SELECT b FROM Booking b " +
           "WHERE b.item.owner.id = :ownerId " +
           "ORDER BY b.start DESC")
    List<Booking> findByOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

    @Query("SELECT b FROM Booking b " +
           "WHERE b.item.owner.id = :ownerId " +
           "AND b.status = :status " +
           "ORDER BY b.start DESC")
    List<Booking> findByOwnerIdAndStatus(
            @Param("ownerId") Long ownerId,
            @Param("status") BookingStatus status,
            Pageable pageable);

    @Query("SELECT b FROM Booking b " +
           "WHERE b.item.owner.id = :ownerId " +
           "AND b.start < :now AND b.end > :now " +
           "ORDER BY b.start DESC")
    List<Booking> findCurrentByOwnerId(
            @Param("ownerId") Long ownerId,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    @Query("SELECT b FROM Booking b " +
           "WHERE b.item.owner.id = :ownerId " +
           "AND b.end < :now " +
           "ORDER BY b.start DESC")
    List<Booking> findPastByOwnerId(
            @Param("ownerId") Long ownerId,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    @Query("SELECT b FROM Booking b " +
           "WHERE b.item.owner.id = :ownerId " +
           "AND b.start > :now " +
           "ORDER BY b.start DESC")
    List<Booking> findFutureByOwnerId(
            @Param("ownerId") Long ownerId,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    List<Booking> findByItemIdOrderByStartAsc(Long itemId);
}