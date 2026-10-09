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
import ru.practicum.shareit.dto.NewCommentRequest;
import ru.practicum.shareit.dto.NewItemRequest;
import ru.practicum.shareit.dto.UpdateItemRequest;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.ItemController;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerGatewayTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemClient itemClient;

    private final Long userId = 1L;
    private final Long itemId = 1L;
    private final String userHeader = "X-Sharer-User-Id";

    private NewItemRequest createNewItemRequest(String name, String description, Boolean available) {
        NewItemRequest request = new NewItemRequest();
        request.setName(name);
        request.setDescription(description);
        request.setAvailable(available);
        return request;
    }

    private UpdateItemRequest createUpdateItemRequest(String name) {
        UpdateItemRequest request = new UpdateItemRequest();
        request.setName(name);
        return request;
    }

    private NewCommentRequest createNewCommentRequest(String text) {
        NewCommentRequest request = new NewCommentRequest();
        request.setText(text);
        return request;
    }

    @Test
    void create_WhenDataIsValid_ShouldReturnStatusOk() throws Exception {
        NewItemRequest request = createNewItemRequest("Дрель", "Простая дрель", true);

        Mockito.when(itemClient.createItem(eq(userId), any(NewItemRequest.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mvc.perform(post("/items")
                        .header(userHeader, userId)
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void create_WhenNameIsEmpty_ShouldReturnBadRequest_ByValidation() throws Exception {
        NewItemRequest badRequest = createNewItemRequest("", "Простая дрель", true);

        mvc.perform(post("/items")
                        .header(userHeader, userId)
                        .content(mapper.writeValueAsString(badRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(itemClient);
    }

    @Test
    void update_ShouldReturnStatusOk() throws Exception {
        UpdateItemRequest request = createUpdateItemRequest("Дрель+");

        Mockito.when(itemClient.updateItem(eq(userId), eq(itemId), any(UpdateItemRequest.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mvc.perform(patch("/items/{itemId}", itemId)
                        .header(userHeader, userId)
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getById_ShouldReturnStatusOk() throws Exception {
        Mockito.when(itemClient.getItemById(itemId, userId))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mvc.perform(get("/items/{itemId}", itemId)
                        .header(userHeader, userId))
                .andExpect(status().isOk());
    }

    @Test
    void getAllByOwner_ShouldReturnStatusOk() throws Exception {
        Mockito.when(itemClient.getAllItemsByOwner(userId))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mvc.perform(get("/items")
                        .header(userHeader, userId))
                .andExpect(status().isOk());
    }

    @Test
    void searchItems_ShouldReturnStatusOk() throws Exception {
        Mockito.when(itemClient.searchItems(userId, "дрель"))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mvc.perform(get("/items/search")
                        .header(userHeader, userId)
                        .param("text", "дрель"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteItem_ShouldReturnStatusOk() throws Exception {
        Mockito.when(itemClient.deleteItem(itemId, userId))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mvc.perform(delete("/items/{itemId}", itemId)
                        .header(userHeader, userId))
                .andExpect(status().isOk());
    }

    @Test
    void createComment_WhenDataIsValid_ShouldReturnStatusOk() throws Exception {
        NewCommentRequest request = createNewCommentRequest("Всё отлично работает!");

        Mockito.when(itemClient.createComment(eq(itemId), eq(userId), any(NewCommentRequest.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(userHeader, userId)
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void createComment_WhenTextIsEmpty_ShouldReturnBadRequest_ByValidation() throws Exception {
        NewCommentRequest badRequest = createNewCommentRequest("");

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(userHeader, userId)
                        .content(mapper.writeValueAsString(badRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(itemClient);
    }
}