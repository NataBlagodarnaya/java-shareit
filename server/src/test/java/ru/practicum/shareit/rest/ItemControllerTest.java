package ru.practicum.shareit.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.dto.NewCommentRequest;
import ru.practicum.shareit.dto.NewItemRequest;
import ru.practicum.shareit.dto.UpdateItemRequest;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.CommentResponse;
import ru.practicum.shareit.item.dto.ItemResponse;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemService itemService;

    private final Long userId = 1L;
    private final Long itemId = 1L;
    private final String userHeader = "X-Sharer-User-Id";

    private ItemResponse createItemResponse(Long id, String name, String description, Boolean available) {
        ItemResponse response = new ItemResponse();
        response.setId(id);
        response.setName(name);
        response.setDescription(description);
        response.setAvailable(available);
        return response;
    }

    private NewCommentRequest createNewCommentRequest(String text) {
        NewCommentRequest request = new NewCommentRequest();
        request.setText(text);
        return request;
    }

    private CommentResponse createCommentResponse(Long id, String text, String authorName) {
        CommentResponse response = new CommentResponse();
        response.setId(id);
        response.setText(text);
        response.setAuthorName(authorName);
        response.setCreated(LocalDateTime.now());
        return response;
    }

    private NewItemRequest createNewItemRequest(String name, String description, Boolean available) {
        NewItemRequest request = new NewItemRequest();
        request.setName(name);
        request.setDescription(description);
        request.setAvailable(available);
        return request;
    }

    private UpdateItemRequest createUpdateItemRequest(String name, String description, Boolean available) {
        UpdateItemRequest request = new UpdateItemRequest();
        request.setName(name);
        request.setDescription(description);
        request.setAvailable(available);
        return request;
    }

    @Test
    void create_ShouldReturnCreatedItem() throws Exception {
        NewItemRequest request = createNewItemRequest("Дрель", "Простая дрель", true);
        ItemResponse response = createItemResponse(itemId, "Дрель", "Простая дрель", true);

        Mockito.when(itemService.createItem(eq(userId), any(NewItemRequest.class)))
                .thenReturn(response);

        mvc.perform(post("/items")
                        .header(userHeader, userId)
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Дрель"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void update_ShouldReturnUpdatedItem() throws Exception {
        UpdateItemRequest request = createUpdateItemRequest("Дрель+", "Мощная дрель", false);
        ItemResponse response = createItemResponse(itemId, "Дрель+", "Мощная дрель", false);

        Mockito.when(itemService.updateItem(eq(userId), eq(itemId), any(UpdateItemRequest.class)))
                .thenReturn(response);

        mvc.perform(patch("/items/{itemId}", itemId)
                        .header(userHeader, userId)
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Дрель+"))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void getById_ShouldReturnItem() throws Exception {
        ItemResponse response = createItemResponse(itemId, "Отвертка", "Крестовая", true);

        Mockito.when(itemService.getItemById(itemId, userId))
                .thenReturn(response);

        mvc.perform(get("/items/{itemId}", itemId)
                        .header(userHeader, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Отвертка"));
    }

    @Test
    void getAllByOwner_ShouldReturnItemCollection() throws Exception {
        ItemResponse response = createItemResponse(itemId, "Отвертка", "Крестовая", true);

        Mockito.when(itemService.getAllItemsByOwner(userId))
                .thenReturn(List.of(response));

        mvc.perform(get("/items")
                        .header(userHeader, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(itemId));
    }

    @Test
    void searchItems_ShouldReturnMatchingItems() throws Exception {
        ItemResponse response = createItemResponse(itemId, "Дрель", "Простая дрель", true);

        Mockito.when(itemService.searchItems(userId, "дрель"))
                .thenReturn(List.of(response));

        mvc.perform(get("/items/search")
                        .header(userHeader, userId)
                        .param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Дрель"));
    }

    @Test
    void deleteItem_ShouldReturnStatusOk() throws Exception {
        Mockito.doNothing().when(itemService).deleteItem(itemId, userId);

        mvc.perform(delete("/items/{itemId}", itemId)
                        .header(userHeader, userId))
                .andExpect(status().isOk());

        Mockito.verify(itemService, Mockito.times(1)).deleteItem(itemId, userId);
    }

    @Test
    void createComment_ShouldReturnCommentResponse() throws Exception {
        NewCommentRequest request = createNewCommentRequest("Отличный инструмент, выручил!");
        CommentResponse response = createCommentResponse(1L, "Отличный инструмент, выручил!", "Иван");

        Mockito.when(itemService.createComment(eq(itemId), eq(userId), any(NewCommentRequest.class)))
                .thenReturn(response);

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(userHeader, userId)
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.text").value("Отличный инструмент, выручил!"))
                .andExpect(jsonPath("$.authorName").value("Иван"))
                .andExpect(jsonPath("$.created").value(notNullValue()));
    }

    @Test
    void getById_WhenItemDoesNotExist_ShouldReturnNotFoundStatus() throws Exception {
        Long fakeItemId = 999L;

        Mockito.when(itemService.getItemById(fakeItemId, userId))
                .thenThrow(new ru.practicum.shareit.exception.NotFoundException("Вещь с id " + fakeItemId + " не найдена"));

        mvc.perform(get("/items/{itemId}", fakeItemId)
                        .header(userHeader, userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void createComment_WhenUserDidNotBookItem_ShouldReturnBadRequestStatus() throws Exception {
        NewCommentRequest request = createNewCommentRequest("Пытаюсь оставить фейковый отзыв");

        Mockito.when(itemService.createComment(eq(itemId), eq(userId), any(NewCommentRequest.class)))
                .thenThrow(new ru.practicum.shareit.exception.BadRequestException("Пользователь не бронировал эту вещь"));

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(userHeader, userId)
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}