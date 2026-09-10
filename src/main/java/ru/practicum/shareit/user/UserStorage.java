package ru.practicum.shareit.user;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {

    Collection<User> findAll();

    User save(User user);

    User update(User user);

    Optional<User> findById(Long id);

    boolean existsByEmail(String email);

    void deleteById(Long id);
}
