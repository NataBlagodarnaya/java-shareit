package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.dto.NewItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.util.ValidationUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService{

    private final ItemRequestRepository itemRequestRepository;
    private final ValidationUtil validationUtil;
    private final ItemRepository itemRepository;

    @Transactional
    @Override
    public ItemRequestResponseDto createRequest(Long userId, NewItemRequestDto newItemRequestDto) {
        User requester = validationUtil.getUserOrThrow(userId);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(newItemRequestDto, requester);
        ItemRequest savedRequest = itemRequestRepository.save(itemRequest);
        log.info("Пользователь {} успешно добавил новый запрос с id: {}", userId, savedRequest.getId());
        return ItemRequestMapper.toResponseDto(savedRequest);
    }

    @Override
    public Collection<ItemRequestResponseDto> getAllRequestByRequester(Long userId) {
        validationUtil.getUserOrThrow(userId);
        Sort sortByCreatedDesc = Sort.by(Sort.Direction.DESC, "created");
Collection<ItemRequest> itemRequests = itemRequestRepository.findAllByRequesterId(userId, sortByCreatedDesc);
        return fillRequestsWithItems(itemRequests);
    }

    @Override
    public Collection<ItemRequestResponseDto> getAllRequestsFromOthers(Long userId) {
        validationUtil.getUserOrThrow(userId);
        Sort sortByCreatedDesc = Sort.by(Sort.Direction.DESC, "created");
        Collection<ItemRequest> itemRequests = itemRequestRepository.findAllByRequesterIdNot(userId, sortByCreatedDesc);
        return fillRequestsWithItems(itemRequests);
    }

    private Collection<ItemRequestResponseDto> fillRequestsWithItems(Collection<ItemRequest> itemRequests) {
        if (itemRequests.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> requestIds = itemRequests.stream()
                .map(ItemRequest::getId)
                .toList();

        Collection<Item> allItems = itemRepository.findAllByRequestIdIn(requestIds);

        Map<Long, List<Item>> itemsByRequestId = allItems.stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        return itemRequests.stream()
                .map(request -> {
                    List<Item> requestItems = itemsByRequestId.getOrDefault(request.getId(), Collections.emptyList());
                    return ItemRequestMapper.toResponseDto(request, requestItems);
                })
                .toList();
    }

    @Override
    public ItemRequestResponseDto getRequestById(Long requestId, Long userId) {
        validationUtil.getUserOrThrow(userId);

        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id " + requestId + " не найден"));

        Collection<Item> items = itemRepository.findAllByRequestIdIn(List.of(requestId));

        return ItemRequestMapper.toResponseDto(request, items);
    }
}