package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ItemRequestService requestService;

    @Autowired
    private ObjectMapper objectMapper;

    private ItemRequestDto requestDto;
    private ItemRequestResponseDto responseDto;

    @BeforeEach
    void setUp() {
        requestDto = ItemRequestDto.builder()
                .description("Need a vintage record player")
                .build();

        responseDto = ItemRequestResponseDto.builder()
                .id(1L)
                .description(requestDto.getDescription())
                .requestorId(7L)
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    void createRequest_ShouldReturnCreated() throws Exception {
        when(requestService.create(ArgumentMatchers.eq(7L), ArgumentMatchers.any(ItemRequestDto.class)))
                .thenReturn(responseDto);

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.description").value(responseDto.getDescription()))
                .andExpect(jsonPath("$.requestorId").value(responseDto.getRequestorId()));
    }

    @Test
    void getOwnRequests_ShouldReturnList() throws Exception {
        when(requestService.getOwnRequests(7L))
                .thenReturn(List.of(responseDto));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(responseDto.getId()))
                .andExpect(jsonPath("$[0].description").value(responseDto.getDescription()));
    }

    @Test
    void getAllRequests_ShouldReturnList() throws Exception {
        int from = 0;
        int size = 10;

        when(requestService.getAllRequests(7L, from, size))
                .thenReturn(List.of(responseDto));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 7L)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(responseDto.getId()))
                .andExpect(jsonPath("$[0].description").value(responseDto.getDescription()))
                .andExpect(jsonPath("$[0].requestorId").value(responseDto.getRequestorId()))
                .andExpect(jsonPath("$[0].created").exists());
    }
}
