package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;

import java.util.Collection;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestResponseDto create(@Valid @RequestBody NewItemRequestDto newItemRequestDto,
                                         @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.createRequest(userId, newItemRequestDto);
    }

    @GetMapping
    public Collection<ItemRequestResponseDto> getAllByRequester(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.getAllRequestByRequester(userId);
    }

    @GetMapping("/all")
    public Collection<ItemRequestResponseDto> getAll(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.getAllRequestsFromOthers(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestResponseDto getRequestById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @PathVariable Long requestId) {
        return itemRequestService.getRequestById(userId, requestId);
    }
}