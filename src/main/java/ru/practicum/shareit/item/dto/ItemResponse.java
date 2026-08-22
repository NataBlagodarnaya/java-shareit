package ru.practicum.shareit.item.dto;

import lombok.Data;

/**
 * TODO Sprint add-controllers.
 */
@Data
public class ItemResponse {
    private long id;
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;
}
