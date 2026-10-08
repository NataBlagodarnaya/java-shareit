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
import ru.practicum.shareit.dto.NewItemRequestDto;
import ru.practicum.shareit.request.ItemRequestClient;
import ru.practicum.shareit.request.ItemRequestController;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerGatewayTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemRequestClient itemRequestClient;

    private final Long userId = 1L;
    private final Long requestId = 1L;
    private final String userHeader = "X-Sharer-User-Id";

    private NewItemRequestDto createNewItemRequestDto(String description) {
        NewItemRequestDto dto = new NewItemRequestDto();
        dto.setDescription(description);
        return dto;
    }

    @Test
    void create_WhenDataIsValid_ShouldReturnStatusOk() throws Exception {
        NewItemRequestDto requestDto = createNewItemRequestDto("Ищу стремянку на 3 дня");

        Mockito.when(itemRequestClient.createRequest(eq(userId), any(NewItemRequestDto.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mvc.perform(post("/requests")
                        .header(userHeader, userId)
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void create_WhenDescriptionIsEmpty_ShouldReturnBadRequest_ByValidation() throws Exception {
        NewItemRequestDto badRequest = createNewItemRequestDto("");

        mvc.perform(post("/requests")
                        .header(userHeader, userId)
                        .content(mapper.writeValueAsString(badRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(itemRequestClient);
    }

    @Test
    void getAllByRequester_ShouldReturnStatusOk() throws Exception {
        Mockito.when(itemRequestClient.getAllRequestByRequester(userId))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mvc.perform(get("/requests")
                        .header(userHeader, userId))
                .andExpect(status().isOk());
    }

    @Test
    void getAll_ShouldReturnStatusOk() throws Exception {
        Mockito.when(itemRequestClient.getAllRequestsFromOthers(userId))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mvc.perform(get("/requests/all")
                        .header(userHeader, userId))
                .andExpect(status().isOk());
    }

    @Test
    void getRequestById_ShouldReturnStatusOk() throws Exception {
        Mockito.when(itemRequestClient.getRequestById(requestId, userId))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mvc.perform(get("/requests/{requestId}", requestId)
                        .header(userHeader, userId))
                .andExpect(status().isOk());
    }
}