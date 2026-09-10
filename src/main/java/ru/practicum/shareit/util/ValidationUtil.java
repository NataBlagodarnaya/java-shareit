package ru.practicum.shareit.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemStorage;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserStorage;

@Component
@RequiredArgsConstructor
@Slf4j
public class ValidationUtil {

    private final BookingRepository bookingRepository;
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    public User getUserOrThrow(Long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id = {} не найден", userId);
                    return new NotFoundException("Пользователь с id = " + userId + " не найден");
                });
    }

    public Item getItemOrThrow(Long itemId) {
        return itemStorage.findById(itemId)
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
}
