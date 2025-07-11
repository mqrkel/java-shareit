package ru.practicum.shareit.item;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@FieldDefaults(level = AccessLevel.PRIVATE)
@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    CommentRepository commentRepository;
    @Autowired
    TestEntityManager testEntityManager;
    final User user = User.builder()
            .name("name")
            .email("email@email.com")
            .build();
    final Item item = Item.builder()
            .name("name")
            .description("description")
            .available(true)
            .owner(user)
            .build();
    final Comment comment = Comment.builder()
            .item(item)
            .author(user)
            .created(LocalDateTime.now())
            .text("comment")
            .build();

    @BeforeEach
    public void init() {
        testEntityManager.persist(user);
        testEntityManager.persist(item);
        testEntityManager.flush();

        comment.setItem(item);
        comment.setAuthor(user);
        commentRepository.save(comment);
    }

    @AfterEach
    public void deleteAll() {
        commentRepository.deleteAll();
    }

    @Test
    void findAllByItemId() {
        Long itemId = item.getId();
        List<Comment> comments = commentRepository.findByItemId(itemId);

        assertEquals(1, comments.size());
        assertEquals("comment", comments.get(0).getText());
    }
}