package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.Item;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemMapper {

    public static ItemResponse toItemResponse(Item item) {
        ItemResponse dto = new ItemResponse();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        if (item.getRequest() != null) {
            dto.setRequestId(item.getRequest().getId());
        } else {
            dto.setRequestId(null);
        }
        return dto;
    }

    public static Item toItem(NewItemRequest dto) {
        Item item = new Item();
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setAvailable(dto.getAvailable());
        return item;
    }

    public static Item toItem(UpdateItemRequest dto) {
        Item item = new Item();
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setAvailable(dto.getAvailable());
        return item;
    }
}