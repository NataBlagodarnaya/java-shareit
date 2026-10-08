package ru.practicum.shareit.integration;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.dto.NewItemRequestDto;
import ru.practicum.shareit.dto.NewUserRequest;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.dto.UserResponse;
import ru.practicum.shareit.user.UserService;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestIntegrationTest {

    private final ItemRequestService itemRequestService;
    private final UserService userService;

    @Test
    void getAllRequestByRequester_shouldReturnRequestsFromDb() {
        NewUserRequest userDto = new NewUserRequest();
        userDto.setName("Пользователь 1");
        userDto.setEmail("r1@mail.com");
        UserResponse user = userService.createUser(userDto);

        NewItemRequestDto dto = new NewItemRequestDto();
        dto.setDescription("Нужна дрель");
        itemRequestService.createRequest(user.getId(), dto);

        Collection<ItemRequestResponseDto> list = itemRequestService.getAllRequestByRequester(user.getId());

        assertEquals(1, list.size());
        assertEquals("Нужна дрель", list.iterator().next().getDescription());
    }

    @Test
    void getAllRequestsFromOthers_shouldReturnOnlyOtherUsersRequests() {
        NewUserRequest user1Dto = new NewUserRequest();
        user1Dto.setName("Пользователь 2");
        user1Dto.setEmail("r2@mail.com");
        UserResponse user1 = userService.createUser(user1Dto);

        NewUserRequest user2Dto = new NewUserRequest();
        user2Dto.setName("Пользователь 3");
        user2Dto.setEmail("r3@mail.com");
        UserResponse user2 = userService.createUser(user2Dto);

        NewItemRequestDto dto = new NewItemRequestDto();
        dto.setDescription("Нужен мишка");
        itemRequestService.createRequest(user2.getId(), dto);

        Collection<ItemRequestResponseDto> list = itemRequestService.getAllRequestsFromOthers(user1.getId());

        assertEquals(1, list.size());
        assertEquals("Нужен мишка", list.iterator().next().getDescription());
    }
}