package ru.practicum.shareit.booking;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ru.practicum.shareit.booking.Booking.BookingStatus.APPROVED;
import static ru.practicum.shareit.booking.Booking.BookingStatus.WAITING;

@FieldDefaults(level = AccessLevel.PRIVATE)
@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    UserRepository userRepository;
    @Mock
    ItemRepository itemRepository;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    BookingMapper bookingMapper;
    @InjectMocks
    BookingServiceImpl bookingService;

    final User user = User.builder()
            .id(1L)
            .name("username")
            .email("email@email.com")
            .build();

    final User owner = User.builder()
            .id(2L)
            .name("owner")
            .email("owner@email.com")
            .build();

    final Item item = Item.builder()
            .id(1L)
            .name("item name")
            .description("desc")
            .available(true)
            .owner(owner)
            .build();

    final BookingRequestDto bookingDto = BookingRequestDto.builder()
            .itemId(1L)
            .start(LocalDateTime.now().plusDays(1))
            .end(LocalDateTime.now().plusDays(2))
            .build();

    final Booking booking = Booking.builder()
            .id(1L)
            .start(bookingDto.getStart())
            .end(bookingDto.getEnd())
            .status(WAITING)
            .item(item)
            .booker(user)
            .build();

    final BookingResponseDto bookingResponseDto = BookingResponseDto.builder()
            .id(1L)
            .build();

    @Test
    void create() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingMapper.toEntity(any(), any(), any())).thenReturn(booking);
        when(bookingRepository.save(any())).thenReturn(booking);
        when(bookingMapper.toDto(any())).thenReturn(bookingResponseDto);

        BookingResponseDto result = bookingService.createBooking(bookingDto, user.getId());

        assertThat(result).isNotNull();
        verify(bookingRepository, times(1)).save(any());
    }


    @Test
    void createWhenEndIsBeforeStartShouldThrowValidationException() {
        BookingRequestDto dto = BookingRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(dto, user.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Некорректные даты бронирования");
    }

    @Test
    void createWhenItemIsNotAvailableShouldThrowValidationException() {
        Item unavailableItem = Item.builder()
                .id(1L)
                .available(false)
                .owner(owner)
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(unavailableItem.getId())).thenReturn(Optional.of(unavailableItem));

        assertThatThrownBy(() -> bookingService.createBooking(bookingDto, user.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Вещь недоступна для бронирования");
    }

    @Test
    void createWhenItemOwnerEqualsBookerShouldThrowValidationException() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(bookingDto, owner.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Владелец вещи не может бронировать собственную вещь");
    }

    @Test
    void updateWhenStatusNotApproved() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        booking.setStatus(APPROVED);

        assertThatThrownBy(() -> bookingService.approveBooking(booking.getId(), owner.getId(), true))
                .isInstanceOf(ru.practicum.shareit.exception.ConflictException.class)
                .hasMessageContaining("Бронирование уже обработано");
    }

    @Test
    void updateWhenStatusNotWaiting() {
        Booking approvedBooking = booking.toBuilder().status(APPROVED).build();

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(approvedBooking));

        assertThatThrownBy(() -> bookingService.approveBooking(booking.getId(), owner.getId(), true))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void updateWhenUserIsNotItemOwnerShouldThrowValidationException() {
        Booking waitingBooking = booking.toBuilder().status(WAITING).build();

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(waitingBooking));

        assertThatThrownBy(() -> bookingService.approveBooking(booking.getId(), user.getId(), true))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Подтвердить бронирование может только владелец вещи");
    }

    @Test
    void getById() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(bookingMapper.toDto(booking)).thenReturn(bookingResponseDto);

        BookingResponseDto result = bookingService.getBookingById(booking.getId(), user.getId());

        assertThat(result).isNotNull();
        verify(bookingRepository).findById(booking.getId());
        verify(bookingMapper).toDto(booking);
    }


    @Test
    void getByIdWhenBookingIdIsNotValidShouldThrowNotFoundException() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingById(booking.getId(), user.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Бронирование не найдено");
    }

    @Test
    void getByIdWhenUserIsNotItemOwnerShouldThrowValidationException() {
        User otherUser = User.builder().id(999L).build();
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.getBookingById(booking.getId(), otherUser.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Доступ запрещён");
    }

    @Test
    void getAllByBookerWhenBookingStateAll() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdOrderByStartDesc(eq(user.getId()), any()))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingsByBooker(user.getId(), "ALL", 0, 10);

        assertThat(result).hasSize(1);
        verify(bookingRepository).findByBookerIdOrderByStartDesc(eq(user.getId()), any());
    }


    @Test
    void getAllByBooker_whenBookingStateCURRENT() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(eq(user.getId()), any(), any(), any()))
                .thenReturn(List.of(booking));

        List<?> result = bookingService.getBookingsByBooker(user.getId(), "CURRENT", 0, 10);

        assertThat(result).hasSize(1);
        verify(bookingRepository).findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(eq(user.getId()), any(), any(), any());
    }

    @Test
    void getAllByBookerWhenBookingStatePAST() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(eq(user.getId()), any(), any()))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingsByBooker(user.getId(), "PAST", 0, 10);

        assertThat(result).hasSize(1);
        verify(bookingRepository).findByBookerIdAndEndBeforeOrderByStartDesc(eq(user.getId()), any(), any());
    }

    @Test
    void getAllByBookerWhenBookingStateFUTURE() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(eq(user.getId()), any(), any()))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingsByBooker(user.getId(), "FUTURE", 0, 10);

        assertThat(result).hasSize(1);
        verify(bookingRepository).findByBookerIdAndStartAfterOrderByStartDesc(eq(user.getId()), any(), any());
    }

    @Test
    void getAllByBookerWhenBookingStateWAITING() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdAndStatusOrderByStartDesc(eq(user.getId()), eq(WAITING), any()))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingsByBooker(user.getId(), "WAITING", 0, 10);

        assertThat(result).hasSize(1);
        verify(bookingRepository).findByBookerIdAndStatusOrderByStartDesc(eq(user.getId()), eq(WAITING), any());
    }

    @Test
    void getAllByBookerWhenBookingStateIsNotValidShouldThrowIllegalArgumentException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> bookingService.getBookingsByBooker(user.getId(), "UNKNOWN", 0, 10))
                .isInstanceOf(ru.practicum.shareit.exception.ValidationException.class)
                .hasMessageContaining("Unknown state");
    }

    @Test
    void getAllByOwnerWhenBookingStateAll() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findByOwnerId(eq(owner.getId()), any()))
                .thenReturn(List.of(booking));

        List<?> result = bookingService.getBookingsByOwner(owner.getId(), "ALL", 0, 10);

        assertThat(result).hasSize(1);
        verify(bookingRepository).findByOwnerId(eq(owner.getId()), any());
    }

    @Test
    void getAllByOwnerWhenBookingStateCURRENT() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findCurrentByOwnerId(eq(owner.getId()), any(), any()))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingsByOwner(owner.getId(), "CURRENT", 0, 10);

        assertThat(result).hasSize(1);
        verify(bookingRepository).findCurrentByOwnerId(eq(owner.getId()), any(), any());
    }

    @Test
    void getAllByOwnerWhenBookingStatePAST() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findPastByOwnerId(eq(owner.getId()), any(), any()))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingsByOwner(owner.getId(), "PAST", 0, 10);

        assertThat(result).hasSize(1);
        verify(bookingRepository).findPastByOwnerId(eq(owner.getId()), any(), any());
    }

    @Test
    void getAllByOwnerWhenBookingStateFUTURE() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findFutureByOwnerId(eq(owner.getId()), any(), any()))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingsByOwner(owner.getId(), "FUTURE", 0, 10);

        assertThat(result).hasSize(1);
        verify(bookingRepository).findFutureByOwnerId(eq(owner.getId()), any(), any());
    }

    @Test
    void getAllByOwnerWhenBookingStateWAITING() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findByOwnerIdAndStatus(eq(owner.getId()), eq(WAITING), any()))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingsByOwner(owner.getId(), "WAITING", 0, 10);

        assertThat(result).hasSize(1);
        verify(bookingRepository).findByOwnerIdAndStatus(eq(owner.getId()), eq(WAITING), any());
    }

    @Test
    void getAllByOwnerWhenBookingStateREJECTED() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findByOwnerIdAndStatus(eq(owner.getId()), eq(Booking.BookingStatus.REJECTED), any()))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingsByOwner(owner.getId(), "REJECTED", 0, 10);

        assertThat(result).hasSize(1);
        verify(bookingRepository).findByOwnerIdAndStatus(eq(owner.getId()), eq(Booking.BookingStatus.REJECTED), any());
    }

    @Test
    void getAllByBookerWhenBookingStateREJECTED() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdAndStatusOrderByStartDesc(eq(user.getId()), eq(Booking.BookingStatus.REJECTED), any()))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingsByBooker(user.getId(), "REJECTED", 0, 10);

        assertThat(result).hasSize(1);
        verify(bookingRepository).findByBookerIdAndStatusOrderByStartDesc(eq(user.getId()), eq(Booking.BookingStatus.REJECTED), any());
    }

    @Test
    void getAllByOwnerWhenBookingStateIsNotValidThenThrowIllegalArgumentException() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        assertThatThrownBy(() -> bookingService.getBookingsByOwner(owner.getId(), "UNKNOWN_STATE", 0, 10))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Unknown state");
    }

    @Test
    void getUserOrThrowShouldThrowNotFoundExceptionWhenUserDoesNotExist() {
        Long nonExistentUserId = 999L;

        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingsByBooker(nonExistentUserId, "ALL", 0, 10))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь не найден");
    }

    @Test
    void createBookingShouldThrowNotFoundExceptionWhenItemDoesNotExist() {
        Long nonExistentItemId = 999L;

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(nonExistentItemId)).thenReturn(Optional.empty());

        BookingRequestDto dto = BookingRequestDto.builder()
                .itemId(nonExistentItemId)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        assertThatThrownBy(() -> bookingService.createBooking(dto, user.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Вещь не найдена");
    }

    @Test
    void approveBookingShouldSetStatusApproved() {
        Booking waitingBooking = booking.toBuilder().status(WAITING).build();

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(waitingBooking));
        when(bookingMapper.toDto(any())).thenReturn(bookingResponseDto);

        BookingResponseDto result = bookingService.approveBooking(booking.getId(), owner.getId(), true);

        assertThat(result).isNotNull();
        verify(bookingRepository).save(any());
        assertThat(waitingBooking.getStatus()).isEqualTo(APPROVED);
    }

    @Test
    void approveBookingShouldSetStatusRejected() {
        Booking waitingBooking = booking.toBuilder().status(WAITING).build();

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(waitingBooking));
        when(bookingMapper.toDto(any())).thenReturn(bookingResponseDto);

        BookingResponseDto result = bookingService.approveBooking(booking.getId(), owner.getId(), false);

        assertThat(result).isNotNull();
        verify(bookingRepository).save(any());
        assertThat(waitingBooking.getStatus()).isEqualTo(Booking.BookingStatus.REJECTED);
    }
}