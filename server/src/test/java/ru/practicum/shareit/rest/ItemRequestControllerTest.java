package ru.practicum.shareit.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.dto.NewItemRequestDto;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemRequestService itemRequestService;

    private final Long userId = 1L;
    private final Long requestId = 1L;
    private final String userHeader = "X-Sharer-User-Id";

    private NewItemRequestDto createNewItemRequestDto(String description) {
        NewItemRequestDto dto = new NewItemRequestDto();
        dto.setDescription(description);
        return dto;
    }

    private ItemRequestResponseDto createItemRequestResponseDto(Long id, String description) {
        ItemRequestResponseDto dto = new ItemRequestResponseDto();
        dto.setId(id);
        dto.setDescription(description);
        dto.setCreated(LocalDateTime.now());
        return dto;
    }

    @Test
    void create_ShouldReturnCreatedRequest() throws Exception {
        NewItemRequestDto requestDto = createNewItemRequestDto("Нужна стремянка на 2 дня");
        ItemRequestResponseDto responseDto = createItemRequestResponseDto(requestId, "Нужна стремянка на 2 дня");

        Mockito.when(itemRequestService.createRequest(eq(userId), any(NewItemRequestDto.class)))
                .thenReturn(responseDto);

        mvc.perform(post("/requests")
                        .header(userHeader, userId)
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.description").value("Нужна стремянка на 2 дня"))
                .andExpect(jsonPath("$.created").value(notNullValue()));
    }

    @Test
    void getAllByRequester_ShouldReturnCollection() throws Exception {
        ItemRequestResponseDto responseDto = createItemRequestResponseDto(requestId, "Нужна дрель");

        Mockito.when(itemRequestService.getAllRequestByRequester(userId))
                .thenReturn(List.of(responseDto));

        mvc.perform(get("/requests")
                        .header(userHeader, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(requestId))
                .andExpect(jsonPath("$[0].description").value("Нужна дрель"));
    }

    @Test
    void getAll_ShouldReturnCollectionFromOthers() throws Exception {
        ItemRequestResponseDto responseDto = createItemRequestResponseDto(requestId, "Ищу шуруповерт");

        Mockito.when(itemRequestService.getAllRequestsFromOthers(userId))
                .thenReturn(List.of(responseDto));

        mvc.perform(get("/requests/all")
                        .header(userHeader, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(requestId))
                .andExpect(jsonPath("$[0].description").value("Ищу шуруповерт"));
    }

    @Test
    void getRequestById_ShouldReturnRequest() throws Exception {
        ItemRequestResponseDto responseDto = createItemRequestResponseDto(requestId, "Нужна палатка");

        Mockito.when(itemRequestService.getRequestById(requestId, userId))
                .thenReturn(responseDto);

        mvc.perform(get("/requests/{requestId}", requestId)
                        .header(userHeader, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.description").value("Нужна палатка"));
    }
}