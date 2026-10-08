package ru.practicum.shareit.item.dto;

import lombok.Data;

@Data
public class ItemShortDto {
    private Long id;
    private String name;
    private Long ownerId;

    public ItemShortDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public ItemShortDto(Long id, String name, Long ownerId) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
    }
}
