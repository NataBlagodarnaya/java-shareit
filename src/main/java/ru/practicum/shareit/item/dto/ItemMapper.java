package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.item.Item;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemMapper {

    public static ItemResponse toItemResponse(Item item, Booking lastBooking, Booking nextBooking,
                                              List<CommentResponse> comments) {
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
        dto.setLastBooking(lastBooking != null ? BookingMapper.toBookingShortDto(lastBooking) : null);
        dto.setNextBooking(nextBooking != null ? BookingMapper.toBookingShortDto(nextBooking) : null);
        dto.setComments(comments != null ? comments : new ArrayList<>());
        return dto;
    }

    public static ItemResponse toItemResponse(Item item) {
        return toItemResponse(item, null, null, new ArrayList<>());
    }

    public static ItemShortDto toItemShortDto(Item item) {
        if (item == null) return null;
        return new ItemShortDto(item.getId(), item.getName());
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