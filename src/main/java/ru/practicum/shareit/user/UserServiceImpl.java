package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserResponse;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.util.ValidationUtil;

import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;
    private final ValidationUtil validationUtil;

    @Override
    public Collection<UserResponse> getAllUsers() {
        log.info("Получен запрос на получение всех пользователей");
        return userStorage.findAll().stream()
                .map(UserMapper::toUserResponse)
                .toList();
    }

    @Transactional
    @Override
    public UserResponse createUser(NewUserRequest userDto) {
        validateEmailUniqueness(userDto.getEmail());
        User user = UserMapper.toUser(userDto);
        User createdUser = userStorage.save(user);
        log.info("Создан новый пользователь с id: {}", createdUser.getId());
        return UserMapper.toUserResponse(createdUser);
    }

    @Transactional
    @Override
    public UserResponse updateUser(UpdateUserRequest newUserDto) {
        Long userId = newUserDto.getId();
        User oldUser = validationUtil.getUserOrThrow(userId);

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
        User user = validationUtil.getUserOrThrow(userId);
        return UserMapper.toUserResponse(user);
    }

    @Transactional
    @Override
    public void deleteUser(Long userId) {
        validationUtil.getUserOrThrow(userId);
        userStorage.deleteById(userId);
        log.info("Удалена информация о пользователе с id: {}", userId);
    }

    private void validateEmailUniqueness(String email) {
        if (userStorage.existsByEmail(email)) {
            log.error("Ошибка 409 Conflict: Email {} уже используется", email);
            throw new DuplicatedDataException("Этот Email уже используется");
        }
    }
}