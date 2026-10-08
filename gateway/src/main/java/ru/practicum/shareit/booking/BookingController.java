package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.dto.BookingState;
import ru.practicum.shareit.dto.NewBookingRequest;
import ru.practicum.shareit.exception.BadRequestException;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {

    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody NewBookingRequest newBookingRequest,
                                 @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Запрос на создание бронирования от пользователя {} с данными: {}", userId, newBookingRequest);
        return bookingClient.createBooking(userId, newBookingRequest);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approve(@PathVariable Long bookingId,
                                   @RequestParam Boolean approved,
                                   @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Запрос от пользователя {} на смену статуса бронирования (id {}) на {}", userId, bookingId, approved);
        return bookingClient.approveBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(@PathVariable Long bookingId,
                                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Запрос от пользователя {} на получение информации о бронировании {}", userId, bookingId);
        return bookingClient.getBookingById(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByBooker(@RequestParam(name = "state", defaultValue = "ALL") String stateParam,
                                                      @RequestHeader("X-Sharer-User-Id") Long userId) {
        BookingState state = getBookingStateOrThrow(stateParam);
        log.info("Запрос от пользователя {} на получение своих бронирований со статусом {}", userId, stateParam);
        return bookingClient.getAllByBooker(userId, state);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllByOwner(@RequestParam(name = "state", defaultValue = "ALL") String stateParam,
                                                     @RequestHeader("X-Sharer-User-Id") Long userId) {
        BookingState state = getBookingStateOrThrow(stateParam);
        log.info("Запрос от пользователя {} на получение бронирований своих вещей со статусом {}", userId, stateParam);
        return bookingClient.getAllByOwner(userId, state);
    }

    private BookingState getBookingStateOrThrow(String stateParam) {
        return BookingState.from(stateParam)
                .orElseThrow(() -> new BadRequestException("Некорректный статус: " + stateParam));
    }
}
