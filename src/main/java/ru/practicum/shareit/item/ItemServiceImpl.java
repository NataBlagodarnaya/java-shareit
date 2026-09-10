package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserStorage;
import ru.practicum.shareit.util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemStorage itemStorage;
    private final BookingRepository bookingRepository;
    private final ValidationUtil validationUtil;
    private final CommentRepository commentRepository;

    @Transactional
    @Override
    public ItemResponse createItem(Long userId, NewItemRequest itemDto) {
        User owner = validationUtil.getUserOrThrow(userId);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        Item savedItem = itemStorage.save(item);
        log.info("Пользователь {} успешно добавил новую вещь с id: {}", userId, savedItem.getId());
        return ItemMapper.toItemResponse(savedItem);
    }

    @Transactional
    @Override
    public ItemResponse updateItem(Long userId, Long itemId, UpdateItemRequest itemDto) {
        Item oldItem = validationUtil.getItemOrThrow(itemId);
        validateOwner(oldItem, userId);
        Item newItem = ItemMapper.toItem(itemDto);
        newItem.setId(itemId);
        Item updatedItem = itemStorage.update(newItem);
        log.info("Пользователь {} успешно обновил информацию о вещи с id: {}", userId, itemId);
        return ItemMapper.toItemResponse(updatedItem);
    }

    @Override
    public ItemResponse getItemById(Long itemId, Long userId) {
        Item item = validationUtil.getItemOrThrow(itemId);

        Booking lastBooking = null;
        Booking nextBooking = null;

        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();

            lastBooking = bookingRepository
                    .findFirstByItem_IdAndStatusNotAndStartBeforeOrderByStartDesc(itemId, BookingStatus.REJECTED, now)
                    .orElse(null);

            nextBooking = bookingRepository
                    .findFirstByItem_IdAndStatusNotAndStartAfterOrderByStartAsc(itemId, BookingStatus.REJECTED, now)
                    .orElse(null);
        }
        List<Comment> comments = commentRepository.findByItem_Id(itemId);
        List<CommentResponse> commentResponses = comments.stream()
                .map(CommentMapper::toCommentResponse)
                .toList();
        log.info("Получена информация о предмете с id: {}. Запрос от пользователя: {}", itemId, userId);

        return ItemMapper.toItemResponse(item, lastBooking, nextBooking, commentResponses);
    }

    @Override
    public Collection<ItemResponse> getAllItemsByOwner(Long userId) {
        validationUtil.getUserOrThrow(userId);

        Collection<Item> items = itemStorage.findAllByOwner(userId);
        LocalDateTime now = LocalDateTime.now();

        List<Long> itemIds = items.stream().map(Item::getId).toList();
        List<Comment> allComments = commentRepository.findByItem_IdIn(itemIds);
        Map<Long, List<CommentResponse>> commentsByItemId = allComments.stream()
                .collect(Collectors.groupingBy(
                        comment -> comment.getItem().getId(),
                        Collectors.mapping(CommentMapper::toCommentResponse, Collectors.toList())
                ));

        Collection<ItemResponse> ownerItems = items.stream()
                .map(item -> {
                    Booking lastBooking = bookingRepository
                            .findFirstByItem_IdAndStatusNotAndStartBeforeOrderByStartDesc(item.getId(), BookingStatus.REJECTED, now)
                            .orElse(null);

                    Booking nextBooking = bookingRepository
                            .findFirstByItem_IdAndStatusNotAndStartAfterOrderByStartAsc(item.getId(), BookingStatus.REJECTED, now)
                            .orElse(null);

                    List<CommentResponse> itemComments = commentsByItemId.getOrDefault(item.getId(), List.of());

                    return ItemMapper.toItemResponse(item, lastBooking, nextBooking, itemComments);
                })
                .toList();
        log.info("Успешно возвращено {} вещей для владельца с id: {}", ownerItems.size(), userId);

        return ownerItems;
    }

    @Override
    public Collection<ItemResponse> searchItems(String text) {
        if (text == null || text.isBlank()) {
            log.info("Передан пустой текст для поиска. Возвращен пустой список.");
            return new ArrayList<>();
        }
        Collection<ItemResponse> foundItems = itemStorage.search(text).stream()
                .map(ItemMapper::toItemResponse)
                .toList();
        log.info("Поиск завершен успешно. По запросу '{}' найдено вещей: {}", text, foundItems.size());
        return foundItems;
    }

    @Transactional
    @Override
    public void deleteItem(Long itemId, Long userId) {
        Item item = validationUtil.getItemOrThrow(itemId);
        validateOwner(item, userId);
        itemStorage.deleteById(itemId);
        log.info("Удалена информация о предмете с id: {}", itemId);
    }

    @Transactional
    @Override
    public CommentResponse createComment(Long itemId, Long userId, NewCommentRequest request) {
        Item item = validationUtil.getItemOrThrow(itemId);
        User author = validationUtil.getUserOrThrow(userId);
        boolean hasApprovedBooking = bookingRepository.existsByBooker_IdAndItem_IdAndStatusAndEndBefore(
                userId,
                itemId,
                BookingStatus.APPROVED,
                LocalDateTime.now()
        );
        if (!hasApprovedBooking) {
            throw new BadRequestException("Пользователь " + userId + " не может оставить отзыв на вещь " + itemId +
                    ", так как у него нет завершенных бронирований.");
        }
        Comment comment = CommentMapper.toComment(request, item, author);
        Comment savedComment = commentRepository.save(comment);
        log.info("Пользователь {} успешно добавил комментарий {} к вещи {}", userId, savedComment.getId(), itemId);
        return CommentMapper.toCommentResponse(savedComment);
    }

    private void validateOwner(Item item, Long userId) {
        if (!item.getOwner().getId().equals(userId)) {
            log.error("Доступ заблокирован: пользователь {} не владелец вещи {}", userId, item.getId());
            throw new NotFoundException("Пользователь с id " + userId + " не является владельцем этой вещи");
        }
    }
}