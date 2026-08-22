package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserResponse;
import ru.practicum.shareit.user.dto.UserMapper;

import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;

    @Override
    public Collection<UserResponse> getAllUsers() {
        log.info("Получен запрос на получение всех пользователей");
        return userStorage.findAll().stream()
                .map(UserMapper::toUserResponse)
                .toList();
    }

    @Override
    public UserResponse createUser(NewUserRequest userDto) {
        validateEmailUniqueness(userDto.getEmail());
        User user = UserMapper.toUser(userDto);
        User createdUser = userStorage.create(user);
        log.info("Создан новый пользователь с id: {}", createdUser.getId());
        return UserMapper.toUserResponse(createdUser);
    }

    @Override
    public UserResponse updateUser(UpdateUserRequest newUserDto) {
        Long userId = newUserDto.getId();
        User oldUser = getUserOrThrow(userId);

        if (newUserDto.getEmail() != null && !newUserDto.getEmail().equals(oldUser.getEmail())) {
            validateEmailUniqueness(newUserDto.getEmail());
        }
        User userForUpdate = UserMapper.toUser(newUserDto);
        User updatedUser = userStorage.update(userForUpdate);
        log.info("Успешно обновлен пользователь с id: {}", userId);
        return UserMapper.toUserResponse(updatedUser);
    }

    @Override
    public UserResponse getUserById(Long userId) {
        User user = getUserOrThrow(userId);
        return UserMapper.toUserResponse(user);
    }

    @Override
    public void deleteUser(Long userId) {
        getUserOrThrow(userId);
        userStorage.delete(userId);
        log.info("Удалена информация о пользователе с id: {}", userId);
    }

    private void validateEmailUniqueness(String email) {
        if (userStorage.isExistEmail(email)) {
            log.error("Ошибка 409 Conflict: Email {} уже используется", email);
            throw new DuplicatedDataException("Этот Email уже используется");
        }
    }

    private User getUserOrThrow(Long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id = {} не найден", userId);
                    return new NotFoundException("Пользователь с id = " + userId + " не найден");
                });
    }
}