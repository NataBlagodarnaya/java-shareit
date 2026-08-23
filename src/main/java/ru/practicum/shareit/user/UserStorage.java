package ru.practicum.shareit.user;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {

    Collection<User> findAll();

    User create(User user);

    User update(User user);

    Optional<User> findById(Long id);

    boolean isExistEmail(String email);

    void delete(Long id);
}
