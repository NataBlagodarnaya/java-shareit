package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;


@Slf4j
@Component
public class InMemoryItemStorage implements ItemStorage {

    private final Map<Long, Item> items = new HashMap<>();

    @Override
    public Item create(Item item) {
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
        Collection<Item> ownerItems = new ArrayList<>();
        for (Item item : items.values()) {
            if (item.getOwner() != null && item.getOwner().getId().equals(userId)) {
                ownerItems.add(item);
            }
        }
        return ownerItems;
    }

    @Override
    public Collection<Item> search(String text) {
        Collection<Item> foundItems = new ArrayList<>();
        String query = text.toLowerCase();
        for (Item item : items.values()) {
            if (item.getAvailable().equals(true)) {
                boolean matchesName = item.getName() != null
                        && item.getName().toLowerCase().contains(query);
                boolean matchesDescription = item.getDescription() != null
                        && item.getDescription().toLowerCase().contains(query);
                if (matchesName || matchesDescription) {
                    foundItems.add(item);
                }
            }
        }
        return foundItems;
    }

    @Override
    public void delete(Long id) {
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