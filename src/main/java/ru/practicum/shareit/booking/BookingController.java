package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.dto.NewBookingRequest;

import java.util.Collection;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingResponse create(@Valid @RequestBody NewBookingRequest newBookingRequest,
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
    public Collection<BookingResponse> getAllByBooker(@RequestParam(defaultValue = "ALL") String state,
                                                      @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.getAllByBooker(userId, state);
    }

    @GetMapping("/owner")
    public Collection<BookingResponse> getAllByOwner(@RequestParam(defaultValue = "ALL") String state,
                                                     @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.getAllByOwner(userId, state);
    }
}
