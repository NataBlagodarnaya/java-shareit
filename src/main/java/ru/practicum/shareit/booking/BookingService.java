package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.dto.NewBookingRequest;

import java.util.Collection;

public interface BookingService {

    BookingResponse createBooking(Long userId, NewBookingRequest newBookingRequest);

    BookingResponse approveBooking(Long userId, Long bookingId, Boolean approved);

    BookingResponse getBookingById(Long userId, Long bookingId);

    Collection<BookingResponse> getAllByBooker(Long userId, String state);

    Collection<BookingResponse> getAllByOwner(Long userId, String state);

}