package ru.practicum.shareit.user;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.dto.UserDto;

@JsonTest
class UserJsonTest {

    @Autowired
    private JacksonTester<UserDto> userDtoJacksonTester;

    @Test
    void testUserDto() throws Exception {
        final UserDto userDto = UserDto.builder()
                .id(5L)
                .name("User")
                .email("user@mail.ru")
                .build();

        final JsonContent<UserDto> jsonContent = userDtoJacksonTester.write(userDto);

        Assertions.assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(5);
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.name").isEqualTo("User");
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.email").isEqualTo("user@mail.ru");
    }
}
