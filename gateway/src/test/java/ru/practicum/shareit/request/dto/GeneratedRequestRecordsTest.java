package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.generated.request.dto.CreateItemRequestRequest;
import ru.practicum.shareit.generated.request.dto.ItemRequestCreateResponse;
import ru.practicum.shareit.generated.request.dto.ItemRequestResponse;
import ru.practicum.shareit.generated.request.dto.RequestedItemResponse;

import java.time.LocalDateTime;
import java.lang.reflect.RecordComponent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class GeneratedRequestRecordsTest {
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
    void allGeneratedRequestModelsShouldBeRecords() {
        assertThat(List.of(
                CreateItemRequestRequest.class,
                ItemRequestCreateResponse.class,
                ItemRequestResponse.class,
                RequestedItemResponse.class
        )).allMatch(Class::isRecord);
    }

    @Test
    void buildersAndToBuildersShouldCreateImmutableCopies() {
        final RequestedItemResponse item = RequestedItemResponse.builder()
                .id(1L)
                .name("Drill")
                .description("Cordless")
                .available(true)
                .requestId(2L)
                .build();
        final ItemRequestCreateResponse created = ItemRequestCreateResponse.builder()
                .id(2L)
                .description("Need a drill")
                .created(LocalDateTime.parse("2026-11-19T12:00:00"))
                .build();
        final ItemRequestResponse response = ItemRequestResponse.builder()
                .id(created.id())
                .description(created.description())
                .created(created.created())
                .items(List.of(item))
                .build();

        assertThat(item.toBuilder().name("Saw").build().name()).isEqualTo("Saw");
        assertThat(created.toBuilder().description("Updated").build().description()).isEqualTo("Updated");
        assertThat(response.toBuilder().items(List.of()).build().items()).isEmpty();
        assertThat(CreateItemRequestRequest.builder().description("Need tools").build()
                .toBuilder().description("Need a drill").build().description()).isEqualTo("Need a drill");
    }

    @Test
    void jacksonShouldKeepResponsePropertyNamesAndTypes() throws Exception {
        final String json = "{\"id\":7,\"description\":\"Need a drill\"," +
                "\"created\":\"2026-11-19T12:00:00\",\"items\":[{\"id\":3,\"name\":\"Drill\"," +
                "\"description\":\"Cordless\",\"available\":true,\"requestId\":7}]}";

        final ItemRequestResponse response = OBJECT_MAPPER.readValue(json, ItemRequestResponse.class);

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.created()).isEqualTo(LocalDateTime.parse("2026-11-19T12:00:00"));
        assertThat(response.items()).singleElement().satisfies(item -> {
            assertThat(item.id()).isEqualTo(3L);
            assertThat(item.available()).isTrue();
            assertThat(item.requestId()).isEqualTo(7L);
        });
        final Set<String> propertyNames = new HashSet<>();
        OBJECT_MAPPER.readTree(OBJECT_MAPPER.writeValueAsString(response))
                .fieldNames()
                .forEachRemaining(propertyNames::add);
        assertThat(propertyNames).containsExactlyInAnyOrder("id", "description", "created", "items");
    }

    @Test
    void recordAccessorsShouldKeepJsonPropertyAnnotations() throws Exception {
        assertJsonProperties(CreateItemRequestRequest.class, "description");
        assertJsonProperties(ItemRequestCreateResponse.class, "id", "description", "created");
        assertJsonProperties(ItemRequestResponse.class, "id", "description", "created", "items");
        assertJsonProperties(RequestedItemResponse.class,
                "id", "name", "description", "available", "requestId");
    }

    @Test
    void descriptionShouldRejectNullAndBlankValuesIncludingUnicodeSeparator() {
        for (final String value : new String[]{null, "", "   ", "\u001C"}) {
            final CreateItemRequestRequest request = CreateItemRequestRequest.builder()
                    .description(value)
                    .build();

            assertThat(validator.validate(request)).isNotEmpty();
        }
        assertThat(validator.validate(CreateItemRequestRequest.builder()
                .description("Need a drill")
                .build())).isEmpty();
    }

    private static void assertJsonProperties(final Class<?> type, final String... propertyNames)
            throws NoSuchMethodException {
        assertThat(type.getRecordComponents())
                .extracting(RecordComponent::getName)
                .containsExactly(propertyNames);
        for (final String propertyName : propertyNames) {
            assertThat(type.getMethod(propertyName).getAnnotation(JsonProperty.class))
                    .isNotNull()
                    .extracting(JsonProperty::value)
                    .isEqualTo(propertyName);
        }
    }
}
