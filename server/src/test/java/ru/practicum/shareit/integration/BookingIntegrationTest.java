package ru.practicum.shareit.integration;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.dto.BookingState;
import ru.practicum.shareit.dto.NewBookingRequest;
import ru.practicum.shareit.dto.NewItemRequest;
import ru.practicum.shareit.dto.NewUserRequest;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.user.dto.UserResponse;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingIntegrationTest {

    private final BookingService bookingService;
    private final UserService userService;
    private final ItemService itemService;

    @Test
    void approveBooking_shouldUpdateStatusInDatabase() {
        NewUserRequest ownerDto = new NewUserRequest();
        ownerDto.setName("Владелец");
        ownerDto.setEmail("owner_b1@mail.com");
        UserResponse owner = userService.createUser(ownerDto);

        NewUserRequest bookerDto = new NewUserRequest();
        bookerDto.setName("Бронирующий");
        bookerDto.setEmail("booker_b1@mail.com");
        UserResponse booker = userService.createUser(bookerDto);

        NewItemRequest itemDto = new NewItemRequest();
        itemDto.setName("Дрель");
        itemDto.setDescription("Красивая дрель");
        itemDto.setAvailable(true);
        ItemResponse item = itemService.createItem(owner.getId(), itemDto);

        NewBookingRequest bookingDto = new NewBookingRequest();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        BookingResponse booking = bookingService.createBooking(booker.getId(), bookingDto);

        BookingResponse approved = bookingService.approveBooking(owner.getId(), booking.getId(), true);

        assertEquals(BookingStatus.APPROVED, approved.getStatus());
    }

    @Test
    void getAllByBooker_shouldReturnBookingsForBookerFromDb() {
        NewUserRequest ownerDto = new NewUserRequest();
        ownerDto.setName("Владелец2");
        ownerDto.setEmail("owner_b2@mail.com");
        UserResponse owner = userService.createUser(ownerDto);

        NewUserRequest bookerDto = new NewUserRequest();
        bookerDto.setName("Бронирующий2");
        bookerDto.setEmail("booker_b2@mail.com");
        UserResponse booker = userService.createUser(bookerDto);

        NewItemRequest itemDto = new NewItemRequest();
        itemDto.setName("Мишка");
        itemDto.setDescription("Плюшевый мишка");
        itemDto.setAvailable(true);
        ItemResponse item = itemService.createItem(owner.getId(), itemDto);

        NewBookingRequest bookingDto = new NewBookingRequest();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(5));
        bookingDto.setEnd(LocalDateTime.now().plusDays(6));
        bookingService.createBooking(booker.getId(), bookingDto);

        Collection<BookingResponse> list = bookingService.getAllByBooker(booker.getId(), BookingState.FUTURE);

        assertEquals(1, list.size());
    }

    @Test
    void getAllByOwner_shouldReturnBookingsForOwnersItemsFromDb() {
        NewUserRequest ownerDto = new NewUserRequest();
        ownerDto.setName("Владелец3");
        ownerDto.setEmail("owner_b3@mail.com");
        UserResponse owner = userService.createUser(ownerDto);

        NewUserRequest bookerDto = new NewUserRequest();
        bookerDto.setName("Бронирующий3");
        bookerDto.setEmail("booker_b3@mail.com");
        UserResponse booker = userService.createUser(bookerDto);

        NewItemRequest itemDto = new NewItemRequest();
        itemDto.setName("Гантели");
        itemDto.setDescription("Гантели 5кг");
        itemDto.setAvailable(true);
        ItemResponse item = itemService.createItem(owner.getId(), itemDto);

        NewBookingRequest bookingDto = new NewBookingRequest();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        bookingService.createBooking(booker.getId(), bookingDto);

        Collection<BookingResponse> list = bookingService.getAllByOwner(owner.getId(), BookingState.ALL);

        assertEquals(1, list.size());
    }
}