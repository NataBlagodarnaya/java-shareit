package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;

import java.util.Collection;

public interface ItemService {

    ItemResponse createItem(Long userId, NewItemRequest newItemRequest);

    ItemResponse updateItem(Long userId, Long itemId, UpdateItemRequest updateItemRequest);

    ItemResponse getItemById(Long itemId, Long userId);

    Collection<ItemResponse> getAllItemsByOwner(Long userId);

    Collection<ItemResponse> searchItems(String text);

    void deleteItem(Long itemId, Long userId);
}