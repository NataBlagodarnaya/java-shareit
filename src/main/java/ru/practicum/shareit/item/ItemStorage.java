package ru.practicum.shareit.item;

import java.util.Collection;
import java.util.Optional;

public interface ItemStorage {

    Item save(Item item);

    Item update(Item item);

    Optional<Item> findById(Long id);

    Collection<Item> findAllByOwner(Long userId);

    Collection<Item> search(String text);

    void deleteById(Long id);
}