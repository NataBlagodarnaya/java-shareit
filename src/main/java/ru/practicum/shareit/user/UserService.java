package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserResponse;

import java.util.Collection;

public interface UserService {

    Collection<UserResponse> getAllUsers();

    UserResponse createUser(NewUserRequest userDto);

    UserResponse updateUser(UpdateUserRequest userDto);

    void deleteUser(Long userId);

    UserResponse getUserById(Long userId);
}
