package ru.practicum.shareit.request;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@FieldDefaults(level = AccessLevel.PRIVATE)
@DataJpaTest
class RequestRepositoryTest {

    @Autowired
    ItemRequestRepository requestRepository;

    @Autowired
    TestEntityManager testEntityManager;

    User user1;
    User user2;

    @BeforeEach
    void init() {
        user1 = User.builder()
                .name("User One")
                .email("user1@example.com")
                .build();
        user2 = User.builder()
                .name("User Two")
                .email("user2@example.com")
                .build();

        testEntityManager.persist(user1);
        testEntityManager.persist(user2);

        ItemRequest request1 = ItemRequest.builder()
                .description("Request from user1")
                .requestor(user1)
                .created(LocalDateTime.now().minusDays(1))
                .build();

        ItemRequest request2 = ItemRequest.builder()
                .description("Request from user2")
                .requestor(user2)
                .created(LocalDateTime.now().minusHours(1))
                .build();

        ItemRequest request3 = ItemRequest.builder()
                .description("Second request from user1")
                .requestor(user1)
                .created(LocalDateTime.now())
                .build();

        testEntityManager.persist(request1);
        testEntityManager.persist(request2);
        testEntityManager.persist(request3);
        testEntityManager.flush();
    }

    @Test
    void findByRequestorIdOrderByCreatedDesc_ShouldReturnOnlyUserRequestsInDescendingOrder() {
        List<ItemRequest> requests = requestRepository.findByRequestorIdOrderByCreatedDesc(user1.getId());

        assertThat(requests).hasSize(2);
        assertThat(requests.get(0).getDescription()).isEqualTo("Second request from user1");
        assertThat(requests.get(1).getDescription()).isEqualTo("Request from user1");
    }

    @Test
    void findAllOtherUsersRequests_ShouldReturnRequestsFromOthersWithPagination() {
        List<ItemRequest> requests = requestRepository.findAllOtherUsersRequests(
                user1.getId(),
                PageRequest.of(0, 10));

        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getRequestor().getId()).isEqualTo(user2.getId());
        assertThat(requests.get(0).getDescription()).isEqualTo("Request from user2");
    }
}