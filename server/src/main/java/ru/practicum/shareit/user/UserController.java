package ru.practicum.shareit.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Пользователи", description = "Операции по созданию, обновлению и удалению пользователей")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Создать пользователя",
            description = "Добавляет нового пользователя в систему",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Пользователь успешно создан",
                            content = @Content(schema = @Schema(implementation = UserDto.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректные данные запроса")
            }
    )
    @PostMapping
    public UserDto create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные нового пользователя",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserDto.class))
            )
            @RequestBody final UserDto userDto
    ) {
        log.debug("POST /users — создание нового пользователя");
        return userService.create(userDto);
    }

    @Operation(
            summary = "Обновить данные пользователя",
            description = "Позволяет изменить имя или email пользователя",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Данные пользователя обновлены",
                            content = @Content(schema = @Schema(implementation = UserDto.class))),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
            }
    )
    @PatchMapping("/{userId}")
    public UserDto update(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Обновлённые данные пользователя",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserDto.class))
            )
            @RequestBody final UserDto userDto,
            @Parameter(description = "ID пользователя", required = true)
            @PathVariable final Long userId
    ) {
        log.debug("PATCH /users/{} — обновление пользователя", userId);
        return userService.update(userDto, userId);
    }

    @Operation(
            summary = "Удалить пользователя",
            description = "Удаляет пользователя по его идентификатору",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Пользователь удалён"),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
            }
    )
    @DeleteMapping("/{userId}")
    public void delete(
            @Parameter(description = "ID пользователя", required = true)
            @PathVariable final Long userId
    ) {
        log.debug("DELETE /users/{} — удаление пользователя", userId);
        userService.delete(userId);
    }

    @Operation(
            summary = "Получить список всех пользователей",
            description = "Возвращает список всех зарегистрированных пользователей",
            responses = @ApiResponse(responseCode = "200", description = "Список пользователей получен")
    )
    @GetMapping
    public List<UserDto> getUsers() {
        log.debug("GET /users — получение списка всех пользователей");
        return userService.getUsers();
    }

    @Operation(
            summary = "Получить пользователя по ID",
            description = "Возвращает данные пользователя по его идентификатору",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Пользователь найден",
                            content = @Content(schema = @Schema(implementation = UserDto.class))),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
            }
    )
    @GetMapping("/{userId}")
    public UserDto getUserById(
            @Parameter(description = "ID пользователя", required = true)
            @PathVariable final Long userId
    ) {
        log.debug("GET /users/{} — получение пользователя", userId);
        return userService.getUserById(userId);
    }
}
