package ru.practicum.shareit.item;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    ItemRepository itemRepository;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    CommentRepository commentRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    ItemMapper itemMapper;
    @Mock
    CommentMapper commentMapper;
    @InjectMocks
    ItemServiceImpl itemService;

    @Test
    void addNewItemWhenInvoked() {
        var itemRequest = ItemRequestDto.builder()
                .name("New Item")
                .description("Item description")
                .available(true)
                .build();

        var ownerId = 1L;

        var owner = User.builder()
                .id(ownerId)
                .name("Owner")
                .email("owner@email.com")
                .build();

        var savedItem = Item.builder()
                .id(10L)
                .name(itemRequest.getName())
                .description(itemRequest.getDescription())
                .available(itemRequest.getAvailable())
                .owner(owner)
                .build();

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemMapper.toEntity(eq(itemRequest), eq(owner), isNull())).thenReturn(savedItem);
        when(itemRepository.save(any())).thenReturn(savedItem);
        when(itemMapper.toDto(any(Item.class))).thenAnswer(invocation ->
                toDtoFromItem(invocation.getArgument(0))
        );

        var result = itemService.create(itemRequest, ownerId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(savedItem.getId());
        assertThat(result.getName()).isEqualTo(savedItem.getName());

        verify(itemRepository).save(any());
        verify(userRepository).findById(ownerId);
    }


    @Test
    void getItemById() {
        Long itemId = 1L;
        Long userId = 2L;

        var owner = User.builder().id(userId).build();
        var item = Item.builder().id(itemId).owner(owner).name("item1").build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(commentRepository.findByItemId(itemId)).thenReturn(Collections.emptyList());

        ItemResponseDto dto = ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .comments(Collections.emptyList())
                .build();

        when(itemMapper.toDto(item)).thenReturn(dto);

        var result = itemService.getById(itemId, userId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(itemId);
        assertThat(result.getComments()).isEmpty();
        verify(itemRepository, times(1)).findById(itemId);
        verify(commentRepository, times(1)).findByItemId(itemId);
        verify(itemMapper, times(1)).toDto(any(Item.class));
        verifyNoMoreInteractions(itemRepository, commentRepository, itemMapper);
    }

    @Test
    void updateItem() {
        Long itemId = 1L;
        Long ownerId = 1L;

        var item = Item.builder()
                .id(itemId)
                .name("Old name")
                .description("Old desc")
                .available(true)
                .owner(User.builder().id(ownerId).build())
                .build();

        var updateDto = ItemRequestUpdateDto.builder()
                .name("New name")
                .description("New desc")
                .available(false)
                .build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        doAnswer(invocation -> {
            ItemRequestUpdateDto dto = invocation.getArgument(0);
            Item itemArg = invocation.getArgument(1);
            if (dto.getName() != null) {
                itemArg.setName(dto.getName());
            }
            if (dto.getDescription() != null) {
                itemArg.setDescription(dto.getDescription());
            }
            if (dto.getAvailable() != null) {
                itemArg.setAvailable(dto.getAvailable());
            }
            return null;
        }).when(itemMapper).updateItemFromDto(any(ItemRequestUpdateDto.class), any(Item.class));

        when(itemMapper.toDto(any(Item.class))).thenAnswer(i ->
                toDtoFromItem(i.getArgument(0))
        );

        var updated = itemService.update(itemId, updateDto, ownerId);

        assertThat(updated).isNotNull();
        assertThat(updated.getName()).isEqualTo(updateDto.getName());
        assertThat(updated.getDescription()).isEqualTo(updateDto.getDescription());
        assertThat(updated.getAvailable()).isEqualTo(updateDto.getAvailable());
        verify(itemRepository, times(1)).findById(itemId);
        verify(itemMapper, times(1)).updateItemFromDto(eq(updateDto), any(Item.class));
        verify(itemMapper, times(1)).toDto(any(Item.class));
        verifyNoMoreInteractions(itemRepository, itemMapper);
    }


    @Test
    void updateItemWhenUserIsNotItemOwnerShouldThrowException() {
        Long itemId = 1L;
        Long ownerId = 1L;
        Long otherUserId = 2L;

        var item = Item.builder()
                .id(itemId)
                .owner(User.builder().id(ownerId).build())
                .build();

        var updateDto = ItemRequestUpdateDto.builder()
                .name("Name")
                .build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        System.out.println("OwnerId = " + ownerId + ", OtherUserId = " + otherUserId);
        System.out.println("Item owner id = " + item.getOwner().getId());

        assertThatThrownBy(() -> itemService.update(itemId, updateDto, otherUserId))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Редактировать вещь может только её владелец");
    }

    @Test
    void updateItemWhenItemIdIsNotValid() {
        Long invalidItemId = 999L;
        Long ownerId = 1L;
        var updateDto = ItemRequestUpdateDto.builder().name("New name").build();

        when(itemRepository.findById(invalidItemId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.update(invalidItemId, updateDto, ownerId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllComments() {
        Long itemId = 1L;

        var owner = User.builder().id(1L).build();
        var item = Item.builder()
                .id(itemId)
                .owner(owner)
                .name("ItemName")
                .available(true)
                .description("Item description")
                .build();

        var comments = List.of(
                Comment.builder().id(1L).text("Comment1").build(),
                Comment.builder().id(2L).text("Comment2").build()
        );

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(commentRepository.findByItemId(itemId)).thenReturn(comments);

        when(itemMapper.toDto(item)).thenReturn(
                ItemResponseDto.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .description(item.getDescription())
                        .available(item.getAvailable())
                        .ownerId(item.getOwner().getId())
                        .comments(Collections.emptyList())
                        .build()
        );

        when(commentMapper.toDto(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            return CommentResponseDto.builder()
                    .id(comment.getId())
                    .text(comment.getText())
                    .authorName("AuthorName")
                    .created(LocalDateTime.now())
                    .build();
        });

        var result = itemService.getById(itemId, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getComments()).hasSize(comments.size());
        assertThat(result.getComments().get(0).getText()).isEqualTo("Comment1");
    }


    @Test
    void createComment() {
        Long itemId = 1L;
        Long userId = 1L;

        CommentRequestDto commentRequestDto = CommentRequestDto.builder()
                .text("Nice item!")
                .build();

        Item item = Item.builder().id(itemId).build();
        User user = User.builder().id(userId).build();

        Booking pastBooking = Booking.builder()
                .id(1L)
                .item(item)
                .booker(user)
                .start(LocalDateTime.now().minusDays(5))
                .end(LocalDateTime.now().minusDays(1))
                .build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(bookingRepository.findByItemIdOrderByStartAsc(itemId))
                .thenReturn(List.of(pastBooking));

        when(commentRepository.save(any())).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(1L);
            return comment;
        });

        CommentResponseDto commentResponseDto = CommentResponseDto.builder()
                .id(1L)
                .text("Nice item!")
                .authorName("user")
                .created(LocalDateTime.now())
                .build();
        when(commentMapper.toDto(any())).thenReturn(commentResponseDto);

        CommentResponseDto response = itemService.addComment(itemId, userId, commentRequestDto);

        assertThat(response).isNotNull();
        assertThat(response.getText()).isEqualTo("Nice item!");
        assertThat(response.getId()).isEqualTo(1L);

        InOrder inOrder = inOrder(itemRepository, userRepository, bookingRepository, commentRepository, commentMapper);

        inOrder.verify(itemRepository).findById(itemId);
        inOrder.verify(userRepository).findById(userId);
        inOrder.verify(bookingRepository).findByItemIdOrderByStartAsc(itemId);
        inOrder.verify(commentRepository).save(any(Comment.class));
        inOrder.verify(commentMapper).toDto(any(Comment.class));

        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository, commentRepository, commentMapper);
    }


    @Test
    void createComment_whenItemIdIsNotValid_thenThrowObjectNotFoundException() {
        Long invalidItemId = 999L;
        Long userId = 1L;
        CommentRequestDto dto = CommentRequestDto.builder()
                .text("Test comment")
                .build();

        when(itemRepository.findById(invalidItemId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.addComment(invalidItemId, userId, dto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Вещь не найдена");
    }

    @Test
    void createCommentWhenUserHaveNotAnyBookingsShouldThrowValidationException() {
        Long itemId = 1L;
        Long userId = 1L;
        CommentRequestDto dto = CommentRequestDto.builder()
                .text("Test comment")
                .build();

        Item item = Item.builder().id(itemId).build();
        User user = User.builder().id(userId).build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(bookingRepository.findByItemIdOrderByStartAsc(itemId)).thenReturn(List.of());

        assertThatThrownBy(() -> itemService.addComment(itemId, userId, dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Оставить комментарий может только пользователь, бравший вещь в аренду.");
    }

    private ItemResponseDto toDtoFromItem(Item item) {
        return ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .ownerId(item.getOwner() != null ? item.getOwner().getId() : null)
                .build();
    }

    @Test
    void getItemsByOwner_shouldReturnItemWithBookingsAndComments() {
        Long ownerId = 1L;
        Long itemId = 10L;
        LocalDateTime now = LocalDateTime.now();

        var user = User.builder().id(ownerId).build();

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(user));

        var item = Item.builder()
                .id(itemId)
                .name("Item")
                .description("Desc")
                .available(true)
                .owner(user)
                .build();

        var bookingPast = Booking.builder()
                .id(1L)
                .item(item)
                .booker(user)
                .start(now.minusDays(10))
                .end(now.minusDays(5))
                .build();

        var bookingFuture = Booking.builder()
                .id(2L)
                .item(item)
                .booker(user)
                .start(now.plusDays(5))
                .end(now.plusDays(10))
                .build();

        var comment = Comment.builder()
                .id(1L)
                .item(item)
                .author(user)
                .text("Nice item")
                .created(now.minusDays(1))
                .build();

        when(itemRepository.findByOwnerId(ownerId)).thenReturn(List.of(item));
        when(bookingRepository.findByItemIdOrderByStartAsc(itemId)).thenReturn(List.of(bookingPast, bookingFuture));
        when(commentRepository.findByItemId(itemId)).thenReturn(List.of(comment));

        ItemResponseDto dto = ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .ownerId(ownerId)
                .build();

        when(itemMapper.toDto(item)).thenReturn(dto);
        when(commentMapper.toDto(comment)).thenReturn(CommentResponseDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName("Author")
                .created(comment.getCreated())
                .build());

        var result = itemService.getItemsByOwner(ownerId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getComments()).hasSize(1);

        verify(userRepository, times(1)).findById(ownerId);
        verify(itemRepository, times(1)).findByOwnerId(ownerId);
        verify(bookingRepository, times(1)).findByItemIdOrderByStartAsc(anyLong());
        verify(commentRepository, times(1)).findByItemId(anyLong());
        verify(itemMapper, times(1)).toDto(any(Item.class));
        verify(commentMapper, times(1)).toDto(any(Comment.class));
        verifyNoMoreInteractions(userRepository, itemRepository, bookingRepository, commentRepository, itemMapper, commentMapper);
    }


    @Test
    void searchAvailable_whenTextProvided_shouldReturnList() {
        String text = "item";
        var item = Item.builder()
                .id(1L)
                .name("item")
                .description("desc")
                .available(true)
                .build();

        when(itemRepository.searchAvailableByText(text)).thenReturn(List.of(item));
        when(itemMapper.toDto(item)).thenAnswer(i -> toDtoFromItem(i.getArgument(0)));

        var result = itemService.searchAvailable(text);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("item");
    }

    @Test
    void searchAvailable_whenTextIsBlank_shouldReturnEmptyList() {
        var result = itemService.searchAvailable("   ");
        assertThat(result).isEmpty();
    }
}