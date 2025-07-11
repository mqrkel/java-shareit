package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.user.dto.NewUserRequestDto;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.dto.UserUpdateRequestDto;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@JsonTest
class UserDtoTest {

    @Autowired
    private JacksonTester<UserRequestDto> userRequestDtoTester;

    @Autowired
    private JacksonTester<UserResponseDto> userResponseDtoTester;

    @Autowired
    private JacksonTester<NewUserRequestDto> newUserRequestDtoTester;

    @Autowired
    private JacksonTester<UserUpdateRequestDto> userUpdateRequestDtoTester;

    @Test
    void userRequestDto_SerializeDeserialize() throws Exception {
        UserRequestDto dto = UserRequestDto.builder()
                .id(1L)
                .name("Максим")
                .email("max@example.com")
                .build();

        var json = userRequestDtoTester.write(dto);

        assertThat(json).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(json).extractingJsonPathStringValue("$.name").isEqualTo("Максим");
        assertThat(json).extractingJsonPathStringValue("$.email").isEqualTo("max@example.com");

        var parsed = userRequestDtoTester.parseObject(json.getJson());
        assertThat(parsed).isEqualTo(dto);
    }

    @Test
    void userResponseDto_SerializeDeserialize() throws Exception {
        UserResponseDto dto = UserResponseDto.builder()
                .id(2L)
                .name("Анна")
                .email("anna@example.com")
                .build();

        var json = userResponseDtoTester.write(dto);

        assertThat(json).extractingJsonPathNumberValue("$.id").isEqualTo(2);
        assertThat(json).extractingJsonPathStringValue("$.name").isEqualTo("Анна");
        assertThat(json).extractingJsonPathStringValue("$.email").isEqualTo("anna@example.com");

        var parsed = userResponseDtoTester.parseObject(json.getJson());
        assertThat(parsed).isEqualTo(dto);
    }

    @Test
    void newUserRequestDto_SerializeDeserialize() throws Exception {
        NewUserRequestDto dto = NewUserRequestDto.builder()
                .name("Петр")
                .email("petr@example.com")
                .build();

        var json = newUserRequestDtoTester.write(dto);

        assertThat(json).doesNotHaveJsonPath("$.id");
        assertThat(json).extractingJsonPathStringValue("$.name").isEqualTo("Петр");
        assertThat(json).extractingJsonPathStringValue("$.email").isEqualTo("petr@example.com");

        var parsed = newUserRequestDtoTester.parseObject(json.getJson());
        assertThat(parsed).isEqualTo(dto);
    }

    @Test
    void userUpdateRequestDto_SerializeDeserialize() throws Exception {
        UserUpdateRequestDto dto = UserUpdateRequestDto.builder()
                .name("Сергей")
                .email("sergey@example.com")
                .build();

        var json = userUpdateRequestDtoTester.write(dto);

        assertThat(json).extractingJsonPathStringValue("$.name").isEqualTo("Сергей");
        assertThat(json).extractingJsonPathStringValue("$.email").isEqualTo("sergey@example.com");

        var parsed = userUpdateRequestDtoTester.parseObject(json.getJson());
        assertThat(parsed).isEqualTo(dto);
    }
}
