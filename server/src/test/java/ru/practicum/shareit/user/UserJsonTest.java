package ru.practicum.shareit.user;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.generated.user.dto.UserResponse;

@JsonTest
class UserJsonTest {

    @Autowired
    private JacksonTester<UserResponse> userResponseJacksonTester;

    @Test
    void testUserResponse() throws Exception {
        final UserResponse userResponse = UserResponse.builder()
                .id(5L)
                .name("User")
                .email("user@mail.ru")
                .build();

        final JsonContent<UserResponse> jsonContent = userResponseJacksonTester.write(userResponse);

        Assertions.assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(5);
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.name").isEqualTo("User");
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.email").isEqualTo("user@mail.ru");
    }

    @Test
    void shouldSerializeNullableFields() throws Exception {
        final JsonContent<UserResponse> jsonContent = userResponseJacksonTester.write(
                UserResponse.builder().id(5L).build()
        );

        Assertions.assertThat(jsonContent).hasJsonPath("$.name");
        Assertions.assertThat(jsonContent).hasJsonPath("$.email");
        Assertions.assertThat(jsonContent).extractingJsonPathValue("$.name").isNull();
        Assertions.assertThat(jsonContent).extractingJsonPathValue("$.email").isNull();
    }
}
