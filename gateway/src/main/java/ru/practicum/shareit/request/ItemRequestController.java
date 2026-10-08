package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.dto.NewItemRequestDto;


@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {

    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody NewItemRequestDto newItemRequestDto,
                                         @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Запрос на создание реквеста на вещь с данными {}, от пользователя {}", newItemRequestDto, userId);
        return itemRequestClient.createRequest(userId, newItemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByRequester(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Запрос на получение всех реквестов пользователя {}", userId);
        return itemRequestClient.getAllRequestByRequester(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAll(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Запрос на получение всех реквестов от других пользователей (кроме {})", userId);
        return itemRequestClient.getAllRequestsFromOthers(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@PathVariable Long requestId,
                                                 @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Запрос от пользователя {} на получение реквеста по id {}", userId, requestId);
        return itemRequestClient.getRequestById(requestId, userId);
    }
}