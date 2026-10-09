package ru.practicum.shareit.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingServiceImpl;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.dto.NewBookingRequest;
import ru.practicum.shareit.dto.BookingState;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ValidationUtil validationUtil;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;
    private NewBookingRequest newBookingRequest;

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
        item.setAvailable(true);
        item.setOwner(owner);

        booking = new Booking();
        booking.setId(100L);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));

        newBookingRequest = new NewBookingRequest();
        newBookingRequest.setItemId(10L);
        newBookingRequest.setStart(LocalDateTime.now().plusDays(1));
        newBookingRequest.setEnd(LocalDateTime.now().plusDays(2));
    }

    @Test
    void createBooking_whenValid_shouldSaveAndReturnResponse() {
        Mockito.when(validationUtil.getUserOrThrow(2L)).thenReturn(booker);
        Mockito.when(validationUtil.getItemOrThrow(10L)).thenReturn(item);
        Mockito.when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponse response = bookingService.createBooking(2L, newBookingRequest);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(BookingStatus.WAITING, response.getStatus());
    }

    @Test
    void createBooking_whenItemNotAvailable_shouldThrowBadRequestException() {
        item.setAvailable(false);
        Mockito.when(validationUtil.getUserOrThrow(2L)).thenReturn(booker);
        Mockito.when(validationUtil.getItemOrThrow(10L)).thenReturn(item);

        assertThrows(BadRequestException.class, () -> bookingService.createBooking(2L, newBookingRequest));
    }

    @Test
    void createBooking_whenEndBeforeStart_shouldThrowBadRequestException() {
        newBookingRequest.setEnd(LocalDateTime.now().minusDays(1));
        Mockito.when(validationUtil.getUserOrThrow(2L)).thenReturn(booker);
        Mockito.when(validationUtil.getItemOrThrow(10L)).thenReturn(item);

        assertThrows(BadRequestException.class, () -> bookingService.createBooking(2L, newBookingRequest));
    }

    @Test
    void createBooking_whenOwnerIsBooker_shouldThrowNotFoundException() {
        Mockito.when(validationUtil.getUserOrThrow(1L)).thenReturn(owner);
        Mockito.when(validationUtil.getItemOrThrow(10L)).thenReturn(item);

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(1L, newBookingRequest));
    }

    @Test
    void approveBooking_whenApprovedTrue_shouldSetStatusApproved() {
        Mockito.when(validationUtil.getBookingOrThrow(100L)).thenReturn(booking);
        Mockito.when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponse response = bookingService.approveBooking(1L, 100L, true);

        assertNotNull(response);
        assertEquals(BookingStatus.APPROVED, response.getStatus());
    }

    @Test
    void approveBooking_whenApprovedFalse_shouldSetStatusRejected() {
        Mockito.when(validationUtil.getBookingOrThrow(100L)).thenReturn(booking);
        Mockito.when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponse response = bookingService.approveBooking(1L, 100L, false);

        assertNotNull(response);
        assertEquals(BookingStatus.REJECTED, response.getStatus());
    }

    @Test
    void approveBooking_whenUserIsNotOwner_shouldThrowBadRequestException() {
        Mockito.when(validationUtil.getBookingOrThrow(100L)).thenReturn(booking);

        assertThrows(BadRequestException.class, () -> bookingService.approveBooking(2L, 100L, true));
    }

    @Test
    void approveBooking_whenStatusIsNotWaiting_shouldThrowBadRequestException() {
        booking.setStatus(BookingStatus.APPROVED);
        Mockito.when(validationUtil.getBookingOrThrow(100L)).thenReturn(booking);

        assertThrows(BadRequestException.class, () -> bookingService.approveBooking(1L, 100L, true));
    }

    @Test
    void approveBooking_whenNotFoundExceptionTriggered_shouldThrowBadRequestException() {
        Mockito.doThrow(new NotFoundException("Not found")).when(validationUtil).getBookingOrThrow(100L);

        assertThrows(BadRequestException.class, () -> bookingService.approveBooking(1L, 100L, true));
    }

    @Test
    void getBookingById_whenRequestedByBooker_shouldReturnBooking() {
        Mockito.when(validationUtil.getBookingOrThrow(100L)).thenReturn(booking);

        BookingResponse response = bookingService.getBookingById(2L, 100L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
    }

    @Test
    void getBookingById_whenRequestedByStranger_shouldThrowNotFoundException() {
        Mockito.when(validationUtil.getBookingOrThrow(100L)).thenReturn(booking);

        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(99L, 100L));
    }

    @Test
    void getAllByBooker_withDifferentStates_shouldCallCorrespondingRepositories() {
        Mockito.when(bookingRepository.findByBooker_Id(anyLong(), any(Sort.class))).thenReturn(List.of(booking));
        Mockito.when(bookingRepository.findByBooker_IdAndStartIsBeforeAndEndIsAfter(anyLong(), any(), any(), any())).thenReturn(List.of(booking));
        Mockito.when(bookingRepository.findByBooker_IdAndEndIsBefore(anyLong(), any(), any())).thenReturn(List.of(booking));
        Mockito.when(bookingRepository.findByBooker_IdAndStartIsAfter(anyLong(), any(), any())).thenReturn(List.of(booking));
        Mockito.when(bookingRepository.findByBooker_IdAndStatus(anyLong(), eq(BookingStatus.WAITING), any())).thenReturn(List.of(booking));

        assertFalse(bookingService.getAllByBooker(2L, BookingState.ALL).isEmpty());
        assertFalse(bookingService.getAllByBooker(2L, BookingState.CURRENT).isEmpty());
        assertFalse(bookingService.getAllByBooker(2L, BookingState.PAST).isEmpty());
        assertFalse(bookingService.getAllByBooker(2L, BookingState.FUTURE).isEmpty());
        assertFalse(bookingService.getAllByBooker(2L, BookingState.WAITING).isEmpty());
    }

    @Test
    void getAllByOwner_withDifferentStates_shouldCallCorrespondingRepositories() {
        Mockito.when(bookingRepository.findByItem_Owner_Id(anyLong(), any(Sort.class))).thenReturn(List.of(booking));
        Mockito.when(bookingRepository.findByItem_Owner_IdAndStartIsBeforeAndEndIsAfter(anyLong(), any(), any(), any())).thenReturn(List.of(booking));
        Mockito.when(bookingRepository.findByItem_Owner_IdAndEndIsBefore(anyLong(), any(), any())).thenReturn(List.of(booking));
        Mockito.when(bookingRepository.findByItem_Owner_IdAndStartIsAfter(anyLong(), any(), any())).thenReturn(List.of(booking));
        Mockito.when(bookingRepository.findByItem_Owner_IdAndStatus(anyLong(), eq(BookingStatus.REJECTED), any())).thenReturn(List.of(booking));

        assertFalse(bookingService.getAllByOwner(1L, BookingState.ALL).isEmpty());
        assertFalse(bookingService.getAllByOwner(1L, BookingState.CURRENT).isEmpty());
        assertFalse(bookingService.getAllByOwner(1L, BookingState.PAST).isEmpty());
        assertFalse(bookingService.getAllByOwner(1L, BookingState.FUTURE).isEmpty());
        assertFalse(bookingService.getAllByOwner(1L, BookingState.REJECTED).isEmpty());
    }
}