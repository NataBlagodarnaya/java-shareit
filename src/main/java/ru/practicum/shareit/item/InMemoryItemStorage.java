package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.*;


@Slf4j
@Component
@ConditionalOnProperty(name = "db.mode", havingValue = "memory", matchIfMissing = true)
public class InMemoryItemStorage implements ItemStorage {

    private final Map<Long, Item> items = new HashMap<>();

    @Override
    public Item save(Item item) {
        item.setId(getNextId());
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item newItem) {
        Item oldItem = items.get(newItem.getId());
        if (newItem.getName() != null && !newItem.getName().isBlank()) {
            oldItem.setName(newItem.getName());
        }
        if (newItem.getDescription() != null && !newItem.getDescription().isBlank()) {
            oldItem.setDescription(newItem.getDescription());
        }
        if (newItem.getAvailable() != null) {
            oldItem.setAvailable(newItem.getAvailable());
        }
        return oldItem;
    }

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public Collection<Item> findAllByOwner(Long userId) {
        return items.values().stream()
                .filter(item -> item.getOwner() != null && item.getOwner().getId().equals(userId))
                .toList();
    }

    @Override
    public Collection<Item> search(String text) {
        String query = text.toLowerCase();
        return items.values().stream()
                .filter(item -> (item.getAvailable() != null && item.getAvailable())
                        && ((item.getName() != null && item.getName().toLowerCase().contains(query))
                        || (item.getDescription() != null && item.getDescription().toLowerCase().contains(query))))
                        .toList();
    }

    @Override
    public void deleteById(Long id) {
        items.remove(id);
    }

    private long getNextId() {
        long currentMaxId = items.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}