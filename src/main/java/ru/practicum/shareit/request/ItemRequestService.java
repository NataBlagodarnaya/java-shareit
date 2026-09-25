package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;

import java.util.Collection;

public interface ItemRequestService {

    ItemRequestResponseDto createRequest(Long userId, NewItemRequestDto newItemRequestDto);

    Collection<ItemRequestResponseDto> getAllRequestByRequester(Long userId);

    Collection<ItemRequestResponseDto> getAllRequestsFromOthers(Long userId);

    ItemRequestResponseDto getRequestById(Long userId, Long requestId);

}
