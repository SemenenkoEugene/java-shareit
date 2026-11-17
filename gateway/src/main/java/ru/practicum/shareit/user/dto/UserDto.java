package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.groups.Default;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.validation.OnUpdate;

@Setter
@Getter
@Builder(toBuilder = true)
public class UserDto {

    private Long id;

    @NotBlank
    @Pattern(regexp = ".*\\S.*", flags = Pattern.Flag.DOTALL, groups = OnUpdate.class)
    private String name;

    @NotBlank
    @NotNull
    @Email(groups = {Default.class, OnUpdate.class})
    @Pattern(regexp = ".*\\S.*", groups = OnUpdate.class)
    private String email;
}
