package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.dto.NewCommentRequest;
import ru.practicum.shareit.dto.NewItemRequest;
import ru.practicum.shareit.dto.UpdateItemRequest;


@RestController
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody NewItemRequest newItemRequest,
                                         @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Запрос на создание вещи от пользователя {} с данными: {}", userId, newItemRequest);
        return itemClient.createItem(userId, newItemRequest);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@PathVariable Long itemId,
                               @RequestBody UpdateItemRequest updateItemRequest,
                               @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Запрос на обновление вещи от пользователя {} с данными: {}", userId, updateItemRequest);
        return itemClient.updateItem(userId, itemId, updateItemRequest);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getById(@PathVariable Long itemId,
                               @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Запрос на получение вещи с id {} от пользователя {}", itemId, userId);
        return itemClient.getItemById(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByOwner(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Запрос на получение списка вещей владельцем {}", userId);
        return itemClient.getAllItemsByOwner(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestParam(name = "text") String text,
                                                @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Запрос на получение вещей, содержащих текст {} в названии или описании", text);
        return itemClient.searchItems(userId, text);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> deleteItem(@PathVariable Long itemId,
                           @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Запрос на удаление вещи с id {}", itemId);
        return itemClient.deleteItem(itemId, userId);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@PathVariable Long itemId,
                                         @RequestHeader("X-Sharer-User-Id") Long userId,
                                         @Valid @RequestBody NewCommentRequest request) {
        log.info("Запрос на создание комментария ({}) к вещи {} пользователем {}",request, itemId, userId);
        return itemClient.createComment(itemId, userId, request);
    }
}