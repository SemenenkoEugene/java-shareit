package ru.practicum.shareit.user.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.generated.user.dto.CreateUserRequest;
import ru.practicum.shareit.generated.user.dto.UpdateUserRequest;
import ru.practicum.shareit.generated.user.dto.UserResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GeneratedUserRecordsTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();
    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void allGeneratedUserModelsShouldBeRecords() {
        assertThat(List.of(CreateUserRequest.class, UpdateUserRequest.class, UserResponse.class))
                .allMatch(Class::isRecord);
    }

    @Test
    void buildersAndToBuildersShouldCreateImmutableCopies() {
        final CreateUserRequest create = CreateUserRequest.builder()
                .id(1L)
                .name("Anna")
                .email("anna@example.com")
                .build();
        final UpdateUserRequest update = UpdateUserRequest.builder()
                .name("Updated")
                .build();
        final UserResponse response = UserResponse.builder()
                .id(1L)
                .name("Anna")
                .email("anna@example.com")
                .build();

        assertThat(create.toBuilder().name("Maria").build().name()).isEqualTo("Maria");
        assertThat(update.toBuilder().email("updated@example.com").build().email())
                .isEqualTo("updated@example.com");
        assertThat(response.toBuilder().id(2L).build().id()).isEqualTo(2L);
    }

    @Test
    void jacksonShouldSerializeAndDeserializeAllModels() throws Exception {
        final CreateUserRequest create = OBJECT_MAPPER.readValue(
                "{\"id\":1,\"name\":\"Anna\",\"email\":\"anna@example.com\"}",
                CreateUserRequest.class);
        final UpdateUserRequest update = OBJECT_MAPPER.readValue(
                "{\"id\":1,\"name\":\"Maria\",\"email\":\"maria@example.com\"}",
                UpdateUserRequest.class);
        final UserResponse response = OBJECT_MAPPER.readValue(
                "{\"id\":1,\"name\":\"Anna\",\"email\":\"anna@example.com\"}",
                UserResponse.class);

        assertThat(create).isEqualTo(CreateUserRequest.builder()
                .id(1L).name("Anna").email("anna@example.com").build());
        assertThat(update).isEqualTo(UpdateUserRequest.builder()
                .id(1L).name("Maria").email("maria@example.com").build());
        assertThat(OBJECT_MAPPER.readTree(OBJECT_MAPPER.writeValueAsString(response)))
                .isEqualTo(OBJECT_MAPPER.readTree(
                        "{\"id\":1,\"name\":\"Anna\",\"email\":\"anna@example.com\"}"));
    }

    @Test
    void createValidationShouldRejectNullBlankAndInvalidValues() {
        assertCreateInvalid(null, "anna@example.com");
        assertCreateInvalid("", "anna@example.com");
        assertCreateInvalid("   ", "anna@example.com");
        assertCreateInvalid("Anna", null);
        assertCreateInvalid("Anna", "");
        assertCreateInvalid("Anna", "   ");
        assertCreateInvalid("Anna", "invalid");

        assertThat(validator.validate(CreateUserRequest.builder()
                .name("Anna")
                .email("anna@example.com")
                .build())).isEmpty();
    }

    @Test
    void updateValidationShouldAllowNullAndRejectPresentInvalidValues() {
        assertThat(validator.validate(UpdateUserRequest.builder().build())).isEmpty();

        for (final String invalidName : new String[]{"", "   "}) {
            assertThat(validator.validate(UpdateUserRequest.builder().name(invalidName).build()))
                    .isNotEmpty();
        }
        for (final String invalidEmail : new String[]{"", "   ", "invalid"}) {
            assertThat(validator.validate(UpdateUserRequest.builder().email(invalidEmail).build()))
                    .isNotEmpty();
        }
        assertThat(validator.validate(UpdateUserRequest.builder()
                .name("Anna")
                .email("anna@example.com")
                .build())).isEmpty();
    }

    private static void assertCreateInvalid(final String name, final String email) {
        assertThat(validator.validate(CreateUserRequest.builder()
                .name(name)
                .email(email)
                .build())).isNotEmpty();
    }
}
