package ru.practicum.shareit.user;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@FieldDefaults(level = AccessLevel.PRIVATE)
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    TestEntityManager em;

    User savedUser;

    @BeforeEach
    void setup() {
        User user = User.builder()
                .name("Максим")
                .email("max@example.com")
                .build();
        savedUser = em.persist(user);
    }

    @Test
    void shouldFindUserByEmail() {
        Optional<User> found = userRepository.findByEmail("max@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(savedUser.getId());
        assertThat(found.get().getName()).isEqualTo("Максим");
        assertThat(found.get().getEmail()).isEqualTo("max@example.com");
    }

    @Test
    void shouldReturnEmptyIfUserNotFoundByEmail() {
        Optional<User> found = userRepository.findByEmail("not-found@example.com");

        assertThat(found).isEmpty();
    }
}
