package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@FieldDefaults(level = AccessLevel.PRIVATE)
@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MockMvc mockMvc;
    @MockBean
    ItemService itemService;
    final User user = User.builder()
            .id(1L)
            .name("username")
            .email("my@email.com")
            .build();
    final Item item = Item.builder()
            .id(1L)
            .name("item name")
            .description("description")
            .owner(user)
            .build();

    @Test
    @SneakyThrows
    void createItemWhenItemIsValid() {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .name("item name")
                .description("description")
                .available(true)
                .build();

        ItemResponseDto responseDto = ItemResponseDto.builder()
                .id(1L)
                .name("item name")
                .description("description")
                .available(true)
                .build();

        Mockito.when(itemService.create(any(ItemRequestDto.class), eq(user.getId())))
                .thenReturn(responseDto);


        String result = mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", String.valueOf(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ItemResponseDto actual = objectMapper.readValue(result, ItemResponseDto.class);
        assertEquals(responseDto.getId(), actual.getId());

        Mockito.verify(itemService).create(any(ItemRequestDto.class), eq(user.getId()));
    }


    @Test
    @SneakyThrows
    void updateWhenItemIsValidShouldReturnStatusIsOk() {
        ItemRequestUpdateDto updateDto = ItemRequestUpdateDto.builder()
                .name("Updated Name")
                .description("Updated Desc")
                .available(true)
                .build();

        ItemResponseDto responseDto = ItemResponseDto.builder()
                .id(1L)
                .name("Updated Name")
                .description("Updated Desc")
                .available(true)
                .build();

        when(itemService.update(eq(item.getId()), any(ItemRequestUpdateDto.class), eq(user.getId())))
                .thenReturn(responseDto);

        String result = mockMvc.perform(patch("/items/{itemId}", item.getId())
                        .header("X-Sharer-User-Id", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ItemResponseDto actual = objectMapper.readValue(result, ItemResponseDto.class);
        assertEquals(responseDto.getName(), actual.getName());
    }


    @Test
    @SneakyThrows
    void getShouldReturnStatusOk() {
        ItemResponseDto responseDto = ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(true)
                .build();

        when(itemService.getById(item.getId(), user.getId())).thenReturn(responseDto);

        String result = mockMvc.perform(get("/items/{itemId}", item.getId())
                        .header("X-Sharer-User-Id", user.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ItemResponseDto actual = objectMapper.readValue(result, ItemResponseDto.class);
        assertEquals(responseDto.getId(), actual.getId());
    }

    @Test
    @SneakyThrows
    void getAllShouldReturnStatusOk() {
        ItemResponseDto responseDto = ItemResponseDto.builder()
                .id(1L)
                .name("item name")
                .description("description")
                .available(true)
                .build();

        when(itemService.getItemsByOwner(user.getId())).thenReturn(List.of(responseDto));

        String result = mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", user.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<ItemResponseDto> items = objectMapper.readValue(result,
                objectMapper.getTypeFactory().constructCollectionType(List.class, ItemResponseDto.class));
        assertEquals(1, items.size());
    }


    @Test
    @SneakyThrows
    void searchItemsShouldReturnStatusOk() {
        ItemResponseDto responseDto = ItemResponseDto.builder()
                .id(1L)
                .name("item name")
                .description("description")
                .available(true)
                .build();

        when(itemService.searchAvailable("item")).thenReturn(List.of(responseDto));

        String result = mockMvc.perform(get("/items/search")
                        .param("text", "item"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<ItemResponseDto> items = objectMapper.readValue(result,
                objectMapper.getTypeFactory().constructCollectionType(List.class, ItemResponseDto.class));
        assertEquals(1, items.size());
    }

    @Test
    @SneakyThrows
    void createCommentWhenCommentIsValidShouldReturnStatusIsOk() {
        CommentRequestDto commentDto = CommentRequestDto.builder()
                .text("Cool item!")
                .build();

        CommentResponseDto responseDto = CommentResponseDto.builder()
                .id(1L)
                .text("Cool item!")
                .authorName(user.getName())
                .build();

        when(itemService.addComment(item.getId(), user.getId(), commentDto))
                .thenReturn(responseDto);

        String result = mockMvc.perform(post("/items/{itemId}/comment", item.getId())
                        .header("X-Sharer-User-Id", user.getId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CommentResponseDto actual = objectMapper.readValue(result, CommentResponseDto.class);
        assertEquals(responseDto.getText(), actual.getText());
    }
}