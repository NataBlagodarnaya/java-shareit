package ru.practicum.shareit.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.dto.NewUserRequest;
import ru.practicum.shareit.dto.UpdateUserRequest;
import ru.practicum.shareit.user.UserClient;
import ru.practicum.shareit.user.UserController;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerGatewayTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserClient userClient;

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

    @Test
    void create_WhenDataIsValid_ShouldReturnStatusOk() throws Exception {
        NewUserRequest request = createNewUserRequest("Ivan", "ivan@mail.com");

        Mockito.when(userClient.createUser(any(NewUserRequest.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void create_WhenNameIsEmpty_ShouldReturnBadRequest_ByValidation() throws Exception {
        NewUserRequest badRequest = createNewUserRequest("", "ivan@mail.com");

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(badRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(userClient);
    }

    @Test
    void create_WhenEmailIsInvalid_ShouldReturnBadRequest_ByValidation() throws Exception {
        NewUserRequest badRequest = createNewUserRequest("Ivan", "not-an-email");

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(badRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(userClient);
    }

    @Test
    void update_ShouldReturnStatusOk() throws Exception {
        UpdateUserRequest request = createUpdateUserRequest("Ivan Updated", "ivan_new@mail.com");

        Mockito.when(userClient.updateUser(eq(userId), any(UpdateUserRequest.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mvc.perform(patch("/users/{userId}", userId)
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getUserById_ShouldReturnStatusOk() throws Exception {
        Mockito.when(userClient.getUserById(userId))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk());
    }

    @Test
    void findAll_ShouldReturnStatusOk() throws Exception {
        Mockito.when(userClient.getAllUsers())
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mvc.perform(get("/users"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser_ShouldReturnStatusOk() throws Exception {
        Mockito.when(userClient.deleteUser(userId))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mvc.perform(delete("/users/{userId}", userId))
                .andExpect(status().isOk());
    }
}
