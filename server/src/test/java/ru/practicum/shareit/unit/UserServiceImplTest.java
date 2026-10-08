package ru.practicum.shareit.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.dto.NewUserRequest;
import ru.practicum.shareit.dto.UpdateUserRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserServiceImpl;
import ru.practicum.shareit.user.dto.UserResponse;
import ru.practicum.shareit.util.ValidationUtil;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ValidationUtil validationUtil;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Иван");
        user.setEmail("ivan@mail.com");
    }

    @Test
    void getAllUsers_shouldReturnCollectionOfUserResponse() {
        Mockito.when(userRepository.findAll()).thenReturn(List.of(user));

        Collection<UserResponse> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        UserResponse response = result.iterator().next();
        assertEquals(1L, response.getId());
        assertEquals("Иван", response.getName());
        assertEquals("ivan@mail.com", response.getEmail());
    }

    @Test
    void createUser_whenValidRequest_shouldSaveAndReturnUserResponse() {
        Mockito.doNothing().when(validationUtil).validateEmailUniqueness(anyString());
        Mockito.when(userRepository.save(any(User.class))).thenReturn(user);

        NewUserRequest request = new NewUserRequest();
        request.setName("Иван");
        request.setEmail("ivan@mail.com");

        UserResponse response = userService.createUser(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Иван", response.getName());
        assertEquals("ivan@mail.com", response.getEmail());
        Mockito.verify(validationUtil, Mockito.times(1)).validateEmailUniqueness("ivan@mail.com");
    }

    @Test
    void updateUser_whenAllFieldsPresent_shouldUpdateAndReturnUserResponse() {
        Mockito.when(validationUtil.getUserOrThrow(1L)).thenReturn(user);
        Mockito.doNothing().when(validationUtil).validateEmailUniqueness(anyString());

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setName("Новый Иван");
        updatedUser.setEmail("updated@mail.com");
        Mockito.when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Новый Иван");
        request.setEmail("updated@mail.com");

        UserResponse response = userService.updateUser(1L, request);

        assertNotNull(response);
        assertEquals("Новый Иван", response.getName());
        assertEquals("updated@mail.com", response.getEmail());
    }

    @Test
    void updateUser_whenOnlyNamePresent_shouldUpdateOnlyName() {
        Mockito.when(validationUtil.getUserOrThrow(1L)).thenReturn(user);

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setName("Новое только имя");
        updatedUser.setEmail("ivan@mail.com");
        Mockito.when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Новое только имя");
        request.setEmail(null);

        UserResponse response = userService.updateUser(1L, request);

        assertNotNull(response);
        assertEquals("Новое только имя", response.getName());
        assertEquals("ivan@mail.com", response.getEmail());
        Mockito.verify(validationUtil, Mockito.never()).validateEmailUniqueness(anyString());
    }

    @Test
    void getUserById_whenUserExists_shouldReturnUserResponse() {
        Mockito.when(validationUtil.getUserOrThrow(1L)).thenReturn(user);

        UserResponse response = userService.getUserById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Иван", response.getName());
    }

    @Test
    void deleteUser_whenUserExists_shouldCallDelete() {
        Mockito.when(validationUtil.getUserOrThrow(1L)).thenReturn(user);
        Mockito.doNothing().when(userRepository).deleteById(anyLong());

        assertDoesNotThrow(() -> userService.deleteUser(1L));

        Mockito.verify(userRepository, Mockito.times(1)).deleteById(1L);
    }
}