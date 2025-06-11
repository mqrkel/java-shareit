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
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Autowired
    private ObjectMapper objectMapper;

    private ItemResponseDto itemResponseDto;

    @BeforeEach
    void setUp() {
        itemResponseDto = ItemResponseDto.builder()
                .id(1L)
                .name("Test item")
                .description("Test description")
                .available(true)
                .build();
    }

    @Test
    void createItem_ShouldReturnCreatedItem() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .name("Test item")
                .description("Test description")
                .available(true)
                .build();

        when(itemService.create(ArgumentMatchers.any(ItemDto.class), ArgumentMatchers.anyLong()))
                .thenReturn(itemResponseDto);

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemResponseDto.getId()))
                .andExpect(jsonPath("$.name").value(itemResponseDto.getName()))
                .andExpect(jsonPath("$.description").value(itemResponseDto.getDescription()))
                .andExpect(jsonPath("$.available").value(itemResponseDto.getAvailable()));
    }

    @Test
    void updateItem_ShouldReturnUpdatedItem() throws Exception {
        ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .name("Updated name")
                .build();

        ItemResponseDto updatedResponse = ItemResponseDto.builder()
                .id(1L)
                .name("Updated name")
                .description("Test description")
                .available(true)
                .build();

        when(itemService.update(ArgumentMatchers.eq(1L), ArgumentMatchers.any(ItemUpdateDto.class), ArgumentMatchers.anyLong()))
                .thenReturn(updatedResponse);

        mockMvc.perform(patch("/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updatedResponse.getId()))
                .andExpect(jsonPath("$.name").value(updatedResponse.getName()));
    }

    @Test
    void getById_ShouldReturnItem() throws Exception {
        when(itemService.getById(1L, 1L)).thenReturn(itemResponseDto);

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemResponseDto.getId()))
                .andExpect(jsonPath("$.name").value(itemResponseDto.getName()));
    }

    @Test
    void getItemsByOwner_ShouldReturnListOfItems() throws Exception {
        when(itemService.getItemsByOwner(1L)).thenReturn(List.of(itemResponseDto));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemResponseDto.getId()));
    }

    @Test
    void searchAvailable_ShouldReturnListOfItems() throws Exception {
        when(itemService.searchAvailable("test")).thenReturn(List.of(itemResponseDto));

        mockMvc.perform(get("/items/search")
                        .param("text", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemResponseDto.getId()));
    }
}