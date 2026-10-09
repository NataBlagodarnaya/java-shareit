package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.dto.NewUserRequest;
import ru.practicum.shareit.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserResponse;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.util.ValidationUtil;

import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ValidationUtil validationUtil;

    @Override
    public Collection<UserResponse> getAllUsers() {
        log.info("Получен запрос на получение всех пользователей");
        return userRepository.findAll().stream()
                .map(UserMapper::toUserResponse)
                .toList();
    }

    @Transactional
    @Override
    public UserResponse createUser(NewUserRequest userDto) {
        validationUtil.validateEmailUniqueness(userDto.getEmail());
        User user = UserMapper.toUser(userDto);
        User createdUser = userRepository.save(user);
        log.info("Создан новый пользователь с id: {}", createdUser.getId());
        return UserMapper.toUserResponse(createdUser);
    }

    @Transactional
    @Override
    public UserResponse updateUser(Long userId, UpdateUserRequest newUserDto) {
        User oldUser = validationUtil.getUserOrThrow(userId);

        if (newUserDto.getEmail() != null && !newUserDto.getEmail().equals(oldUser.getEmail())) {
            validationUtil.validateEmailUniqueness(newUserDto.getEmail());
        }
        if (newUserDto.getName() != null && !newUserDto.getName().isBlank()) {
            oldUser.setName(newUserDto.getName());
        }
        if (newUserDto.getEmail() != null && !newUserDto.getEmail().isBlank()) {
            oldUser.setEmail(newUserDto.getEmail());
        }
        User updatedUser = userRepository.save(oldUser);
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
        userRepository.deleteById(userId);
        log.info("Удалена информация о пользователе с id: {}", userId);
    }
}