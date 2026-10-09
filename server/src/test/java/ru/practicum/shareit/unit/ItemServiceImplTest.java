package ru.practicum.shareit.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.dto.NewCommentRequest;
import ru.practicum.shareit.dto.NewItemRequest;
import ru.practicum.shareit.dto.UpdateItemRequest;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.CommentRepository;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemServiceImpl;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ValidationUtil validationUtil;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private User booker;
    private Item item;
    private NewItemRequest newItemRequest;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setName("Владелец");

        booker = new User();
        booker.setId(2L);
        booker.setName("Бронирующий");

        item = new Item();
        item.setId(10L);
        item.setName("Дрель");
        item.setDescription("Красивая дрель");
        item.setAvailable(true);
        item.setOwner(owner);

        newItemRequest = new NewItemRequest();
        newItemRequest.setName("Дрель");
        newItemRequest.setDescription("Красивая дрель");
        newItemRequest.setAvailable(true);
    }

    @Test
    void createItem_withoutRequestId_shouldSaveAndReturnResponse() {
        Mockito.when(validationUtil.getUserOrThrow(1L)).thenReturn(owner);
        Mockito.when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemResponse response = itemService.createItem(1L, newItemRequest);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Дрель", response.getName());
        assertNull(response.getRequestId());
    }

    @Test
    void createItem_withValidRequestId_shouldLinkRequestAndSave() {
        newItemRequest.setRequestId(100L);
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(100L);

        Mockito.when(validationUtil.getUserOrThrow(1L)).thenReturn(owner);
        Mockito.when(itemRequestRepository.findById(100L)).thenReturn(Optional.of(itemRequest));

        item.setRequest(itemRequest);
        Mockito.when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemResponse response = itemService.createItem(1L, newItemRequest);

        assertNotNull(response);
        assertEquals(100L, response.getRequestId());
    }

    @Test
    void createItem_withInvalidRequestId_shouldThrowNotFoundException() {
        newItemRequest.setRequestId(999L);
        Mockito.when(validationUtil.getUserOrThrow(1L)).thenReturn(owner);
        Mockito.when(itemRequestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.createItem(1L, newItemRequest));
    }

    @Test
    void updateItem_whenValid_shouldUpdateFields() {
        Mockito.when(validationUtil.getItemOrThrow(10L)).thenReturn(item);
        Mockito.doNothing().when(validationUtil).validateOwner(item, 1L);
        Mockito.when(itemRepository.save(any(Item.class))).thenReturn(item);

        UpdateItemRequest updateRequest = new UpdateItemRequest();
        updateRequest.setName("Новая Дрель");
        updateRequest.setDescription("Новое описание");
        updateRequest.setAvailable(false);

        ItemResponse response = itemService.updateItem(1L, 10L, updateRequest);

        assertNotNull(response);
        Mockito.verify(itemRepository, Mockito.times(1)).save(item);
    }

    @Test
    void getItemById_whenRequestedByOwner_shouldIncludeBookings() {
        Mockito.when(validationUtil.getItemOrThrow(10L)).thenReturn(item);

        User testBooker = new User();
        testBooker.setId(2L);
        testBooker.setName("Бронирующий");

        Booking lastBooking = new Booking();
        lastBooking.setId(1L);
        lastBooking.setBooker(testBooker);
        lastBooking.setStart(LocalDateTime.now().minusDays(1));
        lastBooking.setEnd(LocalDateTime.now().minusSeconds(5));

        Booking nextBooking = new Booking();
        nextBooking.setId(2L);
        nextBooking.setBooker(testBooker);
        nextBooking.setStart(LocalDateTime.now().plusDays(1));
        nextBooking.setEnd(LocalDateTime.now().plusDays(2));

        Mockito.when(bookingRepository.findFirstByItem_IdAndStatusNotAndStartBeforeOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(Optional.of(lastBooking));
        Mockito.when(bookingRepository.findFirstByItem_IdAndStatusNotAndStartAfterOrderByStartAsc(anyLong(), any(), any()))
                .thenReturn(Optional.of(nextBooking));
        Mockito.when(commentRepository.findByItem_Id(10L)).thenReturn(List.of());

        ItemResponse response = itemService.getItemById(10L, 1L);

        assertNotNull(response);
        assertNotNull(response.getLastBooking());
        assertNotNull(response.getNextBooking());
    }

    @Test
    void getItemById_whenRequestedByNotOwner_shouldNotIncludeBookings() {
        Mockito.when(validationUtil.getItemOrThrow(10L)).thenReturn(item);
        Mockito.when(commentRepository.findByItem_Id(10L)).thenReturn(List.of());

        ItemResponse response = itemService.getItemById(10L, 2L);

        assertNotNull(response);
        assertNull(response.getLastBooking());
        assertNull(response.getNextBooking());
    }

    @Test
    void getAllItemsByOwner_shouldReturnCollection() {
        Mockito.when(validationUtil.getUserOrThrow(1L)).thenReturn(owner);
        Mockito.when(itemRepository.findAllByOwnerId(1L)).thenReturn(List.of(item));
        Mockito.when(commentRepository.findByItem_IdIn(anyList())).thenReturn(List.of());

        Collection<ItemResponse> responses = itemService.getAllItemsByOwner(1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    void searchItems_whenTextIsNotEmpty_shouldReturnFoundItems() {
        Mockito.when(itemRepository.searchByText("дрель")).thenReturn(List.of(item));

        Collection<ItemResponse> responses = itemService.searchItems(1L, "дрель");

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    void searchItems_whenTextIsEmpty_shouldReturnEmptyList() {
        Collection<ItemResponse> responses = itemService.searchItems(1L, "");

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void deleteItem_shouldCallDeleteInRepository() {
        Mockito.when(validationUtil.getItemOrThrow(10L)).thenReturn(item);
        Mockito.doNothing().when(validationUtil).validateOwner(item, 1L);

        assertDoesNotThrow(() -> itemService.deleteItem(10L, 1L));
        Mockito.verify(itemRepository, Mockito.times(1)).deleteById(10L);
    }

    @Test
    void createComment_whenNoApprovedBooking_shouldThrowBadRequestException() {
        Mockito.when(validationUtil.getItemOrThrow(10L)).thenReturn(item);
        Mockito.when(validationUtil.getUserOrThrow(2L)).thenReturn(booker);
        Mockito.when(bookingRepository.existsByBooker_IdAndItem_IdAndStatusAndEndBefore(anyLong(), anyLong(), any(), any()))
                .thenReturn(false);

        NewCommentRequest request = new NewCommentRequest();
        request.setText("Плохие данные");

        assertThrows(BadRequestException.class, () -> itemService.createComment(10L, 2L, request));
    }
}