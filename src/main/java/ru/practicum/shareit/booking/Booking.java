package ru.practicum.shareit.booking;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Getter
@Setter
public class Booking {
    private long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private Item item;
    private User booker;
    private String status; //WAITING — новое бронирование, ожидает одобрения, APPROVED —бронирование подтверждено владельцем, REJECTED — бронирование отклонено владельцем, CANCELED — бронирование отменено создателем.
}
