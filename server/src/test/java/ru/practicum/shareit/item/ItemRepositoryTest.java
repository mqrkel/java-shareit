package ru.practicum.shareit.item;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.User;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@FieldDefaults(level = AccessLevel.PRIVATE)
@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    ItemRepository itemRepository;
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

    @BeforeEach
    public void addItems() {
        testEntityManager.persist(user);
        testEntityManager.flush();
        itemRepository.save(item);
    }

    @AfterEach
    public void deleteAll() {
        itemRepository.deleteAll();
    }

    @Test
    void findByOwnerId() {
        var result = itemRepository.findByOwnerId(user.getId());

        assertThat(result).hasSize(1);

        assertThat(result.get(0).getOwner().getId()).isEqualTo(user.getId());
        assertThat(result.get(0).getName()).isEqualTo(item.getName());
    }

}