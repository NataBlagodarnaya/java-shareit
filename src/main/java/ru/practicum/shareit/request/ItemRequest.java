package ru.practicum.shareit.request;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-item-requests.
 */
@Getter
@Setter
public class ItemRequest {
    private long id;
    private String description;// описание требуемой вещи
    private User requestor;
    private LocalDateTime created;

}
