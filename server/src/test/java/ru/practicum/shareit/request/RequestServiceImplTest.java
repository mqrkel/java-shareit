package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {

    @Mock
    ItemRequestRepository requestRepository;

    @Mock
    ItemRequestMapper itemRequestMapper;

    @Mock
    ItemRepository itemRepository;

    @Mock
    ItemMapper itemMapper;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    ItemRequestServiceImpl requestService;

    User user;
    ItemRequestDto itemRequestDto;
    ru.practicum.shareit.request.ItemRequest requestEntity;
    ItemRequestResponseDto responseDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Maxim")
                .email("maxim@example.com")
                .build();

        itemRequestDto = ItemRequestDto.builder()
                .description("Need a drill")
                .build();

        requestEntity = ru.practicum.shareit.request.ItemRequest.builder()
                .id(10L)
                .description("Need a drill")
                .requestor(user)
                .created(LocalDateTime.now())
                .build();

        responseDto = ItemRequestResponseDto.builder()
                .id(10L)
                .description("Need a drill")
                .build();
    }

    @Test
    void addNewRequest() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRequestMapper.toEntity(itemRequestDto, user)).thenReturn(requestEntity);
        when(requestRepository.save(requestEntity)).thenReturn(requestEntity);
        when(itemRequestMapper.toDto(requestEntity)).thenReturn(responseDto);

        ItemRequestResponseDto created = requestService.create(user.getId(), itemRequestDto);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isEqualTo(requestEntity.getId());

        // Проверяем вызовы и порядок
        InOrder inOrder = inOrder(userRepository, itemRequestMapper, requestRepository, itemRequestMapper);
        inOrder.verify(userRepository, times(1)).findById(user.getId());
        inOrder.verify(itemRequestMapper, times(1)).toEntity(itemRequestDto, user);
        inOrder.verify(requestRepository, times(1)).save(requestEntity);
        inOrder.verify(itemRequestMapper, times(1)).toDto(requestEntity);

        verifyNoMoreInteractions(userRepository, itemRequestMapper, requestRepository);
    }

    @Test
    void getUserRequests() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(requestRepository.findByRequestorIdOrderByCreatedDesc(user.getId()))
                .thenReturn(List.of(requestEntity));
        when(itemRequestMapper.toDto(requestEntity)).thenReturn(responseDto);

        List<ItemRequestResponseDto> requests = requestService.getOwnRequests(user.getId());

        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getId()).isEqualTo(requestEntity.getId());

        InOrder inOrder = inOrder(userRepository, requestRepository, itemRequestMapper, itemRepository, itemMapper);
        inOrder.verify(userRepository, times(1)).findById(user.getId());
        inOrder.verify(requestRepository, times(1)).findByRequestorIdOrderByCreatedDesc(user.getId());
        inOrder.verify(itemRequestMapper, times(1)).toDto(requestEntity);
        inOrder.verify(itemRepository, times(1)).findByRequestId(requestEntity.getId());
        inOrder.verify(itemMapper, times(0)).toDto(any()); // Тут мы вернули пустой список, map не вызовется

        verifyNoMoreInteractions(userRepository, requestRepository, itemRequestMapper, itemRepository, itemMapper);
    }

    @Test
    void getAllRequests() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(requestRepository.findAllOtherUsersRequests(eq(user.getId()), any(PageRequest.class)))
                .thenReturn(List.of(requestEntity));
        when(itemRequestMapper.toDto(requestEntity)).thenReturn(responseDto);

        List<ItemRequestResponseDto> requests = requestService.getAllRequests(user.getId(), 0, 10);

        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getId()).isEqualTo(requestEntity.getId());

        InOrder inOrder = inOrder(userRepository, requestRepository, itemRequestMapper, itemRepository, itemMapper);
        inOrder.verify(userRepository, times(1)).findById(user.getId());
        inOrder.verify(requestRepository, times(1)).findAllOtherUsersRequests(eq(user.getId()), any(PageRequest.class));
        inOrder.verify(itemRequestMapper, times(1)).toDto(requestEntity);
        inOrder.verify(itemRepository, times(1)).findByRequestId(requestEntity.getId());
        inOrder.verify(itemMapper, times(0)).toDto(any());

        verifyNoMoreInteractions(userRepository, requestRepository, itemRequestMapper, itemRepository, itemMapper);
    }

    @Test
    void getRequestById() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(requestRepository.findById(requestEntity.getId())).thenReturn(Optional.of(requestEntity));
        when(itemRequestMapper.toDto(requestEntity)).thenReturn(responseDto);

        ItemRequestResponseDto result = requestService.getRequestById(user.getId(), requestEntity.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(requestEntity.getId());

        InOrder inOrder = inOrder(userRepository, requestRepository, itemRequestMapper, itemRepository, itemMapper);
        inOrder.verify(userRepository, times(1)).findById(user.getId());
        inOrder.verify(requestRepository, times(1)).findById(requestEntity.getId());
        inOrder.verify(itemRequestMapper, times(1)).toDto(requestEntity);
        inOrder.verify(itemRepository, times(1)).findByRequestId(requestEntity.getId());
        inOrder.verify(itemMapper, times(0)).toDto(any());

        verifyNoMoreInteractions(userRepository, requestRepository, itemRequestMapper, itemRepository, itemMapper);
    }

    @Test
    void getRequestByIdWhenRequestIdIsNotValidShouldThrowObjectNotFoundException() {
        Long userId = user.getId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(requestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.getRequestById(userId, 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Запрос не найден");

        verify(userRepository, times(1)).findById(userId);
        verify(requestRepository, times(1)).findById(999L);

        verifyNoMoreInteractions(userRepository, requestRepository);
    }

}