package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.Collection;
import java.util.Optional;

@Component
@ConditionalOnProperty(name = "db.mode", havingValue = "db")
@RequiredArgsConstructor
public class DbItemStorage implements ItemStorage {

    private final ItemRepository itemRepository;

    @Override
    public Item save(Item item) {
        return itemRepository.save(item);
    }

    @Override
    public Item update(Item newItem) {
        Item oldItem = itemRepository.findById(newItem.getId())
                .orElseThrow(() -> new NotFoundException("Вещь с id " + newItem.getId() + " не найдена"));

        if (newItem.getName() != null && !newItem.getName().isBlank()) {
            oldItem.setName(newItem.getName());
        }
        if (newItem.getDescription() != null && !newItem.getDescription().isBlank()) {
            oldItem.setDescription(newItem.getDescription());
        }
        if (newItem.getAvailable() != null) {
            oldItem.setAvailable(newItem.getAvailable());
        }

        return itemRepository.save(oldItem);
    }

    @Override
    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    @Override
    public Collection<Item> findAllByOwner(Long userId) {
        return itemRepository.findAllByOwnerId(userId);
    }

    @Override
    public Collection<Item> search(String text) {
        if (text == null || text.isBlank()) {
            return java.util.Collections.emptyList();
        }
        return itemRepository.searchByText(text);
    }

    @Override
    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }
}