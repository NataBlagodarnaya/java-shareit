package ru.practicum.shareit.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.dto.NewItemRequestDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.ItemRequestServiceImpl;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private ValidationUtil validationUtil;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private User requester;
    private ItemRequest itemRequest;
    private NewItemRequestDto newItemRequestDto;
    private Item item;

    @BeforeEach
    void setUp() {
        requester = new User();
        requester.setId(1L);
        requester.setName("Запрашивающий");
        requester.setEmail("req@mail.com");

        itemRequest = new ItemRequest();
        itemRequest.setId(10L);
        itemRequest.setDescription("Нужна дрель");
        itemRequest.setRequester(requester);
        itemRequest.setCreated(LocalDateTime.now());

        newItemRequestDto = new NewItemRequestDto();
        newItemRequestDto.setDescription("Нужна дрель");

        item = new Item();
        item.setId(100L);
        item.setName("Дрель");
        item.setDescription("Красивая дрель");
        item.setAvailable(true);
        item.setRequest(itemRequest);
        item.setOwner(requester);
    }

    @Test
    void createRequest_whenValid_shouldSaveAndReturnResponseDto() {
        Mockito.when(validationUtil.getUserOrThrow(1L)).thenReturn(requester);
        Mockito.when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(itemRequest);

        ItemRequestResponseDto response = itemRequestService.createRequest(1L, newItemRequestDto);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Нужна дрель", response.getDescription());
    }

    @Test
    void getAllRequestByRequester_whenRequestsExist_shouldReturnPopulatedCollection() {
        Mockito.when(validationUtil.getUserOrThrow(1L)).thenReturn(requester);
        Mockito.when(itemRequestRepository.findAllByRequesterId(eq(1L), any(Sort.class)))
                .thenReturn(List.of(itemRequest));
        Mockito.when(itemRepository.findAllByRequestIdIn(anyList())).thenReturn(List.of(item));

        Collection<ItemRequestResponseDto> result = itemRequestService.getAllRequestByRequester(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        ItemRequestResponseDto responseDto = result.iterator().next();
        assertEquals(10L, responseDto.getId());
        assertFalse(responseDto.getItems().isEmpty());
        assertEquals(100L, responseDto.getItems().get(0).getId());
    }

    @Test
    void getAllRequestByRequester_whenNoRequestsExist_shouldReturnEmptyList() {
        Mockito.when(validationUtil.getUserOrThrow(1L)).thenReturn(requester);
        Mockito.when(itemRequestRepository.findAllByRequesterId(eq(1L), any(Sort.class)))
                .thenReturn(Collections.emptyList());

        Collection<ItemRequestResponseDto> result = itemRequestService.getAllRequestByRequester(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        Mockito.verify(itemRepository, Mockito.never()).findAllByRequestIdIn(anyList());
    }

    @Test
    void getAllRequestsFromOthers_shouldReturnCollection() {
        Mockito.when(validationUtil.getUserOrThrow(1L)).thenReturn(requester);
        Mockito.when(itemRequestRepository.findAllByRequesterIdNot(eq(1L), any(Sort.class)))
                .thenReturn(List.of(itemRequest));
        Mockito.when(itemRepository.findAllByRequestIdIn(anyList())).thenReturn(List.of(item));

        Collection<ItemRequestResponseDto> result = itemRequestService.getAllRequestsFromOthers(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getRequestById_whenRequestExists_shouldReturnResponseDtoWithItems() {
        Mockito.when(validationUtil.getUserOrThrow(1L)).thenReturn(requester);
        Mockito.when(itemRequestRepository.findById(10L)).thenReturn(Optional.of(itemRequest));
        Mockito.when(itemRepository.findAllByRequestIdIn(List.of(10L))).thenReturn(List.of(item));

        ItemRequestResponseDto response = itemRequestService.getRequestById(10L, 1L);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(1, response.getItems().size());
        assertEquals(100L, response.getItems().get(0).getId());
    }

    @Test
    void getRequestById_whenRequestDoesNotExist_shouldThrowNotFoundException() {
        Mockito.when(validationUtil.getUserOrThrow(1L)).thenReturn(requester);
        Mockito.when(itemRequestRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                itemRequestService.getRequestById(999L, 1L));

        assertTrue(exception.getMessage().contains("Запрос с id 999 не найден"));
    }
}