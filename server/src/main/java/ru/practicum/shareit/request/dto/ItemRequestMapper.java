package ru.practicum.shareit.request.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.dto.NewItemRequestDto;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemRequestMapper {

    public static ItemRequest toItemRequest(NewItemRequestDto dto, User requester) {
        ItemRequest request = new ItemRequest();
        request.setDescription(dto.getDescription());
        request.setRequester(requester);
        request.setCreated(LocalDateTime.now());
        return request;
    }

    public static ItemRequestResponseDto toResponseDto(ItemRequest itemRequest) {
        ItemRequestResponseDto response = new ItemRequestResponseDto();
        response.setId(itemRequest.getId());
        response.setDescription(itemRequest.getDescription());
        response.setCreated(itemRequest.getCreated());
        return response;
    }

    public static ItemRequestResponseDto toResponseDto(ItemRequest itemRequest, Collection<Item> items) {
        ItemRequestResponseDto response = toResponseDto(itemRequest);
        if (items != null && !items.isEmpty()) {
            List<ItemShortDto> itemShortDtos = items.stream()
                    .map(item -> new ItemShortDto(
                            item.getId(),
                            item.getName(),
                            item.getOwner().getId()
                    ))
                    .toList();
            response.setItems(itemShortDtos);
        }
        return response;
    }
}
