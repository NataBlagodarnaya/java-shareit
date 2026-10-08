package ru.practicum.shareit.integration;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.dto.NewCommentRequest;
import ru.practicum.shareit.dto.NewItemRequest;
import ru.practicum.shareit.dto.NewUserRequest;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.CommentResponse;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserResponse;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserMapper;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemIntegrationTest {

    private final ItemService itemService;
    private final UserService userService;
    private final BookingRepository bookingRepository;

    @Test
    void getAllItemsByOwner_shouldReturnItemsWithOwnerDataFromDb() {
        NewUserRequest ownerDto = new NewUserRequest();
        ownerDto.setName("Владелец");
        ownerDto.setEmail("owner_i@mail.com");
        UserResponse owner = userService.createUser(ownerDto);

        NewItemRequest itemDto = new NewItemRequest();
        itemDto.setName("Дрель");
        itemDto.setDescription("Красивая дрель");
        itemDto.setAvailable(true);
        itemService.createItem(owner.getId(), itemDto);

        Collection<ItemResponse> items = itemService.getAllItemsByOwner(owner.getId());

        assertEquals(1, items.size());
        assertEquals("Дрель", items.iterator().next().getName());
    }

    @Test
    void searchItems_shouldReturnMatchingItemsFromDatabase() {
        NewUserRequest ownerDto = new NewUserRequest();
        ownerDto.setName("Владелец2");
        ownerDto.setEmail("owner_i2@mail.com");
        UserResponse owner = userService.createUser(ownerDto);

        NewItemRequest itemDto = new NewItemRequest();
        itemDto.setName("Мишка");
        itemDto.setDescription("Плюшевый мишка");
        itemDto.setAvailable(true);
        itemService.createItem(owner.getId(), itemDto);

        Collection<ItemResponse> found = itemService.searchItems(owner.getId(), "миш");

        assertEquals(1, found.size());
        assertEquals("Мишка", found.iterator().next().getName());
    }

    @Test
    void createComment_shouldSaveCommentInDatabaseWhenBookingIsPast() {
        NewUserRequest ownerDto = new NewUserRequest();
        ownerDto.setName("Владелец3");
        ownerDto.setEmail("owner_i3@mail.com");
        UserResponse owner = userService.createUser(ownerDto);

        NewUserRequest bookerDto = new NewUserRequest();
        bookerDto.setName("Бронирующий3");
        bookerDto.setEmail("booker_i3@mail.com");
        UserResponse booker = userService.createUser(bookerDto);

        NewItemRequest itemDto = new NewItemRequest();
        itemDto.setName("Гантели");
        itemDto.setDescription("Гантели 5кг");
        itemDto.setAvailable(true);
        ItemResponse item = itemService.createItem(owner.getId(), itemDto);

        User bookerUser = UserMapper.toUser(bookerDto);
        bookerUser.setId(booker.getId());

        Item itemEntity = new Item();
        itemEntity.setId(item.getId());
        itemEntity.setName("Гантели");

        Booking booking = new Booking();
        booking.setItem(itemEntity);
        booking.setBooker(bookerUser);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        bookingRepository.save(booking);

        NewCommentRequest commentRequest = new NewCommentRequest();
        commentRequest.setText("Удобные гантели!");
        CommentResponse comment = itemService.createComment(item.getId(), booker.getId(), commentRequest);

        assertNotNull(comment.getId());
        assertEquals("Удобные гантели!", comment.getText());
    }
}