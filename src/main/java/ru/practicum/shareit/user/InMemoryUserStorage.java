package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage{

    private final Map<Long, User> users = new HashMap<>();

    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public User create(User user) {
        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User newUser) {
        User oldUser = users.get(newUser.getId());
        if (newUser.getName() != null && !newUser.getName().isBlank()) {
            oldUser.setName(newUser.getName());
        }
        if (newUser.getEmail() != null && !newUser.getEmail().isBlank()) {
            oldUser.setEmail(newUser.getEmail());
        }
        return oldUser;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public boolean isExistEmail(String email) {
        if (email == null) {
            return false;
        }
        for (User user : users.values()) {
            if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(email)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void delete(Long id) {
        users.remove(id);
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private boolean isExistEmail(User user) {
        return users.values().stream()
                .anyMatch(u -> u.getEmail().equals(user.getEmail()));
    }
}
