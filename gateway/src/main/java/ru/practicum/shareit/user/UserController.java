package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.user.dto.UserDto;


@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {

    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody final UserDto userDto) {
        log.debug("Получен POST-запрос к эндпоинту: '/users' на добавление пользователя");
        return userClient.create(userDto);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> update(@PathVariable final Long userId,
                                         @RequestBody final UserDto userDto) {
        log.debug("Получен PATCH-запрос к эндпоинту: '/users' на обновление пользователя с ID={}", userId);
        return userClient.update(userId, userDto);
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable final Long userId) {
        log.debug("Получен DELETE-запрос к эндпоинту: '/users' на удаление пользователя с ID={}", userId);
        userClient.delete(userId);
    }

    @GetMapping
    public ResponseEntity<Object> getUsers() {
        return userClient.getAll();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUserById(@PathVariable final Long userId) {
        return userClient.getById(userId);
    }
}
