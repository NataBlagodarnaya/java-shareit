package ru.practicum.shareit.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.util.ValidationUtil;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class ValidationUtilTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ValidationUtil validationUtil;

    @Test
    void getUserOrThrow_WhenUserExists_ShouldReturnUser() {
        User user = new User();
        user.setId(1L);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = validationUtil.getUserOrThrow(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getUserOrThrow_WhenUserDoesNotExist_ShouldThrowNotFoundException() {
        Mockito.when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> validationUtil.getUserOrThrow(999L));
    }

    @Test
    void getItemOrThrow_WhenItemExists_ShouldReturnItem() {
        Item item = new Item();
        item.setId(1L);
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        Item result = validationUtil.getItemOrThrow(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getItemOrThrow_WhenItemDoesNotExist_ShouldThrowNotFoundException() {
        Mockito.when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> validationUtil.getItemOrThrow(999L));
    }

    @Test
    void getBookingOrThrow_WhenBookingExists_ShouldReturnBooking() {
        Booking booking = new Booking();
        booking.setId(1L);
        Mockito.when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        Booking result = validationUtil.getBookingOrThrow(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getBookingOrThrow_WhenBookingDoesNotExist_ShouldThrowNotFoundException() {
        Mockito.when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> validationUtil.getBookingOrThrow(999L));
    }

    @Test
    void validateEmailUniqueness_WhenEmailIsUnique_ShouldNotThrowException() {
        Mockito.when(userRepository.existsByEmail(anyString())).thenReturn(false);

        assertDoesNotThrow(() -> validationUtil.validateEmailUniqueness("test@mail.com"));
    }

    @Test
    void validateEmailUniqueness_WhenEmailExists_ShouldThrowDuplicatedDataException() {
        Mockito.when(userRepository.existsByEmail("duplicate@mail.com")).thenReturn(true);

        assertThrows(DuplicatedDataException.class, () -> validationUtil.validateEmailUniqueness("duplicate@mail.com"));
    }

    @Test
    void validateOwner_WhenUserIsOwner_ShouldNotThrowException() {
        User owner = new User();
        owner.setId(1L);

        Item item = new Item();
        item.setOwner(owner);

        assertDoesNotThrow(() -> validationUtil.validateOwner(item, 1L));
    }

    @Test
    void validateOwner_WhenUserIsNotOwner_ShouldThrowNotFoundException() {
        User owner = new User();
        owner.setId(1L);

        Item item = new Item();
        item.setOwner(owner);
        item.setId(10L);

        assertThrows(NotFoundException.class, () -> validationUtil.validateOwner(item, 2L));
    }
}