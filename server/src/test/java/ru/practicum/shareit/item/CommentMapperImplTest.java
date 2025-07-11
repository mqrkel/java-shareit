package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentMapperImpl;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CommentMapperImplTest {

    private CommentMapperImpl commentMapper;

    @BeforeEach
    void setup() {
        commentMapper = new CommentMapperImpl();
    }

    @Test
    void toDto_shouldMapAuthorName() {
        User author = User.builder()
                .id(42L)
                .name("Максим")
                .email("maxim@example.com")
                .build();

        Comment comment = Comment.builder()
                .id(1L)
                .text("Это тестовый комментарий")
                .author(author)
                .created(LocalDateTime.of(2025, 7, 11, 12, 0))
                .build();

        CommentResponseDto dto = commentMapper.toDto(comment);

        assertThat(dto).isNotNull();
        assertThat(dto.getAuthorName()).isEqualTo("Максим");
        assertThat(dto.getText()).isEqualTo("Это тестовый комментарий");
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2025, 7, 11, 12, 0));
    }
}
