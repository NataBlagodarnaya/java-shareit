package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserStorage;

import java.util.ArrayList;
import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Override
    public ItemResponse createItem(Long userId, NewItemRequest itemDto) {
        User owner = getUserOrThrow(userId);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        Item savedItem = itemStorage.create(item);
        log.info("Пользователь {} успешно добавил новую вещь с id: {}", userId, savedItem.getId());
        return ItemMapper.toItemResponse(savedItem);
    }

    @Override
    public ItemResponse updateItem(Long userId, Long itemId, UpdateItemRequest itemDto) {
        Item oldItem = getItemOrThrow(itemId);
        validateOwner(oldItem, userId);
        Item newItem = ItemMapper.toItem(itemDto);
        newItem.setId(itemId);
        Item updatedItem = itemStorage.update(newItem);
        log.info("Пользователь {} успешно обновил информацию о вещи с id: {}", userId, itemId);
        return ItemMapper.toItemResponse(updatedItem);
    }

    @Override
    public ItemResponse getItemById(Long itemId, Long userId) {
        Item item = getItemOrThrow(itemId);
        log.info("Получена информация о предмете с id: {}", itemId);
        return ItemMapper.toItemResponse(item);
    }

    @Override
    public Collection<ItemResponse> getAllItemsByOwner(Long userId) {
        getUserOrThrow(userId);
        Collection<ItemResponse> ownerItems = itemStorage.findAllByOwner(userId).stream()
                .map(ItemMapper::toItemResponse)
                .toList();
        log.info("Успешно возвращено {} вещей для владельца с id: {}", ownerItems.size(), userId);

        return ownerItems;
    }

    @Override
    public Collection<ItemResponse> searchItems(String text) {
        if (text == null || text.isBlank()) {
            log.info("Передан пустой текст для поиска. Возвращен пустой список.");
            return new ArrayList<>();
        }
        Collection<ItemResponse> foundItems = itemStorage.search(text).stream()
                .map(ItemMapper::toItemResponse)
                .toList();
        log.info("Поиск завершен успешно. По запросу '{}' найдено вещей: {}", text, foundItems.size());
        return foundItems;
    }

    @Override
    public void deleteItem(Long itemId, Long userId) {
        Item item = getItemOrThrow(itemId);
        validateOwner(item, userId);
        itemStorage.delete(itemId);
        log.info("Удалена информация о предмете с id: {}", itemId);
    }

    private void validateOwner(Item item, Long userId) {
        if (!item.getOwner().getId().equals(userId)) {
            log.error("Доступ заблокирован: пользователь {} не владелец вещи {}", userId, item.getId());
            throw new NotFoundException("Пользователь с id " + userId + " не является владельцем этой вещи");
        }
    }

    private User getUserOrThrow(Long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id {} не найден", userId);
                    return new NotFoundException("Пользователь с id " + userId + " не найден");
                });
    }

    private Item getItemOrThrow(Long itemId) {
        return itemStorage.findById(itemId)
                .orElseThrow(() -> {
                    log.error("Вещь с id {} не найдена", itemId);
                    return new NotFoundException("Вещь с id " + itemId + " не найдена");
                });
    }
}