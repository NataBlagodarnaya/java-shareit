package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserResponse;

import java.util.Collection;

/**
 * TODO Sprint add-controllers.
 */
@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public Collection<UserResponse> findAll() {
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    public UserResponse getUserById(@PathVariable Long userId) {
        log.info("Получен запрос GET /users/{}", userId);
        return userService.getUserById(userId); // Убедитесь, что этот метод есть в вашем сервисе
    }

    @PostMapping
    public UserResponse create(@Valid @RequestBody NewUserRequest userDto) {
        return userService.createUser(userDto);
    }

    @PatchMapping("/{userId}")
    public UserResponse update(@PathVariable Long userId,
                               @RequestBody UpdateUserRequest userDto) {
        userDto.setId(userId);
        return userService.updateUser(userDto);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
    }
}