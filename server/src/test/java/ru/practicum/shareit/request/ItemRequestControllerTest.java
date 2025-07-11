package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService requestService;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Test
    void createRequest() throws Exception {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need a drill")
                .build();

        ItemRequestResponseDto responseDto = ItemRequestResponseDto.builder()
                .id(1L)
                .description("Need a drill")
                .created(LocalDateTime.now())
                .build();

        Mockito.when(requestService.create(anyLong(), any(ItemRequestDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(USER_ID_HEADER, "1")
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.description").value("Need a drill"));

        Mockito.verify(requestService, Mockito.times(1))
                .create(Mockito.eq(1L), Mockito.any(ItemRequestDto.class));
        Mockito.verifyNoMoreInteractions(requestService);
    }

    @Test
    void getUserRequests() throws Exception {
        ItemRequestResponseDto responseDto = ItemRequestResponseDto.builder()
                .id(1L)
                .description("Need a drill")
                .created(LocalDateTime.now())
                .build();

        Mockito.when(requestService.getOwnRequests(anyLong()))
                .thenReturn(List.of(responseDto));

        mockMvc.perform(get("/requests")
                        .header(USER_ID_HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(responseDto.getId()))
                .andExpect(jsonPath("$[0].description").value("Need a drill"));
    }

    @Test
    void getAllRequests() throws Exception {
        ItemRequestResponseDto responseDto = ItemRequestResponseDto.builder()
                .id(2L)
                .description("Need a ladder")
                .created(LocalDateTime.now())
                .build();

        Mockito.when(requestService.getAllRequests(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(responseDto));

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, "1")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(responseDto.getId()))
                .andExpect(jsonPath("$[0].description").value("Need a ladder"));
    }

    @Test
    void getRequestById() throws Exception {
        ItemRequestResponseDto responseDto = ItemRequestResponseDto.builder()
                .id(1L)
                .description("Need a drill")
                .created(LocalDateTime.now())
                .build();

        Mockito.when(requestService.getRequestById(anyLong(), anyLong()))
                .thenReturn(responseDto);

        mockMvc.perform(get("/requests/{requestId}", 1L)
                        .header(USER_ID_HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.description").value("Need a drill"));

        Mockito.verify(requestService).getRequestById(1L, 1L);
        Mockito.verifyNoMoreInteractions(requestService);
    }
}