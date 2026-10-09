package ru.practicum.shareit.dto;

import java.util.Optional;

public enum BookingState {
    ALL,//все
    CURRENT,//текущие
    PAST,//завершённые
    FUTURE,//будущие
    WAITING,//ожидающие подтверждения
    REJECTED;//отклонённые

    public static Optional<BookingState> from(String stringState) {
        for (BookingState state : values()) {
            if (state.name().equalsIgnoreCase(stringState)) {
                return Optional.of(state);
            }
        }
        return Optional.empty();
    }
}
