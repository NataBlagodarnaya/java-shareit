package ru.practicum.shareit.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.dto.NewUserRequest;
import ru.practicum.shareit.dto.UpdateUserRequest;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserService userService;

    private final Long userId = 1L;

    private NewUserRequest createNewUserRequest(String name, String email) {
        NewUserRequest request = new NewUserRequest();
        request.setName(name);
        request.setEmail(email);
        return request;
    }

    private UpdateUserRequest createUpdateUserRequest(String name, String email) {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName(name);
        request.setEmail(email);
        return request;
    }

    private UserResponse createUserResponse(Long id, String name, String email) {
        UserResponse response = new UserResponse();
        response.setId(id);
        response.setName(name);
        response.setEmail(email);
        return response;
    }

    @Test
    void create_ShouldReturnCreatedUser() throws Exception {
        NewUserRequest request = createNewUserRequest("Иван", "ivan@mail.com");
        UserResponse response = createUserResponse(userId, "Иван", "ivan@mail.com");

        Mockito.when(userService.createUser(any(NewUserRequest.class)))
                .thenReturn(response);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("Иван"))
                .andExpect(jsonPath("$.email").value("ivan@mail.com"));
    }

    @Test
    void update_ShouldReturnUpdatedUser() throws Exception {
        UpdateUserRequest request = createUpdateUserRequest("Иванушка", "ivan_new@mail.com");
        UserResponse response = createUserResponse(userId, "Иванушка", "ivan_new@mail.com");

        Mockito.when(userService.updateUser(eq(userId), any(UpdateUserRequest.class)))
                .thenReturn(response);

        mvc.perform(patch("/users/{userId}", userId)
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("Иванушка"))
                .andExpect(jsonPath("$.email").value("ivan_new@mail.com"));
    }

    @Test
    void getUserById_ShouldReturnUser() throws Exception {
        UserResponse response = createUserResponse(userId, "Иван", "ivan@mail.com");

        Mockito.when(userService.getUserById(userId))
                .thenReturn(response);

        mvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("Иван"));
    }

    @Test
    void findAll_ShouldReturnUserCollection() throws Exception {
        UserResponse response = createUserResponse(userId, "Иван", "ivan@mail.com");

        Mockito.when(userService.getAllUsers())
                .thenReturn(List.of(response));

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(userId))
                .andExpect(jsonPath("$[0].name").value("Иван"));
    }

    @Test
    void deleteUser_ShouldReturnStatusOk() throws Exception {
        Mockito.doNothing().when(userService).deleteUser(userId);

        mvc.perform(delete("/users/{userId}", userId))
                .andExpect(status().isOk());

        Mockito.verify(userService, Mockito.times(1)).deleteUser(userId);
    }

    @Test
    void getUserById_WhenUserNotFound_ShouldReturnNotFoundStatus() throws Exception {
        Long fakeUserId = 999L;

        Mockito.when(userService.getUserById(fakeUserId))
                .thenThrow(new ru.practicum.shareit.exception.NotFoundException("Пользователь не найден"));

        mvc.perform(get("/users/{userId}", fakeUserId))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_WhenEmailIsInvalid_ShouldReturnBadRequestStatus() throws Exception {
        NewUserRequest badRequest = createNewUserRequest("Иван", "invalid-email");

        Mockito.when(userService.createUser(any(NewUserRequest.class)))
                .thenThrow(new ru.practicum.shareit.exception.BadRequestException("Некорректный email"));

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(badRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_WhenEmailAlreadyExists_ShouldReturnConflictStatus() throws Exception {
        NewUserRequest request = createNewUserRequest("Иван", "duplicate@mail.com");

        Mockito.when(userService.createUser(any(NewUserRequest.class)))
                .thenThrow(new ru.practicum.shareit.exception.DuplicatedDataException("Этот Email уже используется"));

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void getUserById_WhenUnexpectedRuntimeException_ShouldReturnInternalServerError() throws Exception {
        Long targetId = 1L;

        Mockito.when(userService.getUserById(targetId))
                .thenThrow(new RuntimeException("Непредвиденная ошибка сервера"));

        mvc.perform(get("/users/{userId}", targetId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Непредвиденная ошибка сервера"));
    }

}