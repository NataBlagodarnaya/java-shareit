package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.dto.NewBookingRequest;
import ru.practicum.shareit.dto.BookingState;
import ru.practicum.shareit.exception.BadRequestException;

import java.util.Collection;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingResponse create(@RequestBody NewBookingRequest newBookingRequest,
                                  @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.createBooking(userId, newBookingRequest);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponse approve(@PathVariable Long bookingId,
                                   @RequestParam Boolean approved,
                                   @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.approveBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponse getBookingById(@PathVariable Long bookingId,
                                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public Collection<BookingResponse> getAllByBooker(@RequestParam(name = "state", defaultValue = "ALL") String stateParam,
                                                      @RequestHeader("X-Sharer-User-Id") Long userId) {
        BookingState state = getBookingStateOrThrow(stateParam);
        return bookingService.getAllByBooker(userId, state);
    }

    @GetMapping("/owner")
    public Collection<BookingResponse> getAllByOwner(@RequestParam(name = "state", defaultValue = "ALL") String stateParam,
                                                     @RequestHeader("X-Sharer-User-Id") Long userId) {
        BookingState state = getBookingStateOrThrow(stateParam);
        return bookingService.getAllByOwner(userId, state);
    }

    private BookingState getBookingStateOrThrow(String stateParam) {
        return BookingState.from(stateParam)
                .orElseThrow(() -> new BadRequestException("Некорректный статус: " + stateParam));
    }
}