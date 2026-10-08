package ru.practicum.shareit.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class ValidationUtil {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id = {} не найден", userId);
                    return new NotFoundException("Пользователь с id = " + userId + " не найден");
                });
    }

    public Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.error("Вещь с id {} не найдена", itemId);
                    return new NotFoundException("Вещь с id " + itemId + " не найдена");
                });
    }

    public Booking getBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Бронирование с id {} не найдено", bookingId);
                    return new NotFoundException("Бронирование с id = " + bookingId + " не найдено");
                });
    }

    public void validateEmailUniqueness(String email) {
        if (userRepository.existsByEmail(email)) {
            log.error("Ошибка 409 Conflict: Email {} уже используется", email);
            throw new DuplicatedDataException("Этот Email уже используется");
        }
    }

    public void validateOwner(Item item, Long userId) {
        if (!item.getOwner().getId().equals(userId)) {
            log.error("Доступ заблокирован: пользователь {} не владелец вещи {}", userId, item.getId());
            throw new NotFoundException("Пользователь с id " + userId + " не является владельцем этой вещи");
        }
    }
}
