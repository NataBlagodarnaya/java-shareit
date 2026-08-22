package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;

import java.util.Collection;

/**
 * TODO Sprint add-controllers.
 */
@Slf4j
@RestController
@RequestMapping(path = "/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ItemResponse create(@Valid @RequestBody NewItemRequest newItemRequest,
                               @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.createItem(userId, newItemRequest);
    }

    @PatchMapping("/{itemId}")
    public ItemResponse update(@PathVariable Long itemId,
                               @Valid @RequestBody UpdateItemRequest updateItemRequest,
                               @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.updateItem(userId, itemId, updateItemRequest);
    }

    @GetMapping("/{itemId}")
    public ItemResponse getById(@PathVariable Long itemId,
                               @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getItemById(itemId, userId);
    }

    @GetMapping
    public Collection<ItemResponse> getAllByOwner(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getAllItemsByOwner(userId);
    }

    @GetMapping("/search")
    public Collection<ItemResponse> searchItems(@RequestParam(name = "text") String text,
                                                @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.searchItems(text);
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@PathVariable Long itemId,
                           @RequestHeader("X-Sharer-User-Id") Long userId) {
        itemService.deleteItem(itemId, userId);
    }
}
