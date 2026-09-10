package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.NotFoundException;
import java.util.Collection;
import java.util.Optional;

@Component
@ConditionalOnProperty(name = "db.mode", havingValue = "db")
@RequiredArgsConstructor
public class DbUserStorage implements UserStorage {

    private final UserRepository userRepository;

    @Override
    public Collection<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public User update(User newUser) {
        User oldUser = userRepository.findById(newUser.getId())
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + newUser.getId() + " не найден"));

        if (newUser.getName() != null && !newUser.getName().isBlank()) {
            oldUser.setName(newUser.getName());
        }
        if (newUser.getEmail() != null && !newUser.getEmail().isBlank()) {
            oldUser.setEmail(newUser.getEmail());
        }

        return userRepository.save(oldUser);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}