package ru.practicum.shareit.integration;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.dto.NewUserRequest;
import ru.practicum.shareit.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserResponse;
import ru.practicum.shareit.user.UserService;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserIntegrationTest {

    private final UserService userService;

    @Test
    void getAllUsers_shouldReturnSavedUsersFromDatabase() {
        NewUserRequest user1 = new NewUserRequest();
        user1.setName("Первый");
        user1.setEmail("one@mail.com");
        userService.createUser(user1);

        NewUserRequest user2 = new NewUserRequest();
        user2.setName("Второй");
        user2.setEmail("two@mail.com");
        userService.createUser(user2);

        Collection<UserResponse> users = userService.getAllUsers();

        assertNotNull(users);
        assertTrue(users.size() >= 2);
    }

    @Test
    void updateUser_shouldModifyUserFieldsInDatabase() {
        NewUserRequest createDto = new NewUserRequest();
        createDto.setName("Старое имя");
        createDto.setEmail("old@mail.com");
        UserResponse created = userService.createUser(createDto);

        UpdateUserRequest updateDto = new UpdateUserRequest();
        updateDto.setName("Новое имя");
        updateDto.setEmail("new@mail.com");
        UserResponse updated = userService.updateUser(created.getId(), updateDto);

        assertEquals("Новое имя", updated.getName());
        assertEquals("new@mail.com", updated.getEmail());
    }
}
