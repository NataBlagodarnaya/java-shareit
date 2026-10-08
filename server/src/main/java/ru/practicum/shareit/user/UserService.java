package ru.practicum.shareit.user;

import ru.practicum.shareit.dto.NewUserRequest;
import ru.practicum.shareit.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserResponse;

import java.util.Collection;

public interface UserService {

    Collection<UserResponse> getAllUsers();

    UserResponse createUser(NewUserRequest userDto);

    UserResponse updateUser(Long userId, UpdateUserRequest userDto);

    void deleteUser(Long userId);

    UserResponse getUserById(Long userId);
}
