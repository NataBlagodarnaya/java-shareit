package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.dto.NewBookingRequest;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ValidationUtil validationUtil;

    @Transactional
    @Override
    public BookingResponse createBooking(Long userId, NewBookingRequest newBookingRequest) {
        User booker = validationUtil.getUserOrThrow(userId);
        Long itemId = newBookingRequest.getItemId();
        Item item = validationUtil.getItemOrThrow(itemId);
        if (!item.getAvailable()) {
            throw new BadRequestException("Вещь с id = " + item.getId() + " сейчас недоступна для бронирования");
        }
        if (newBookingRequest.getEnd().isBefore(newBookingRequest.getStart())
                || newBookingRequest.getEnd().equals(newBookingRequest.getStart())) {
            throw new BadRequestException("Дата окончания бронирования не может быть раньше или равна дате начала");
        }
        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Владелец вещи не может забронировать собственный предмет");
        }
        Booking booking = BookingMapper.toBooking(newBookingRequest);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Пользователь {} успешно забронировал вещь с id: {}", userId, savedBooking.getItem().getId());
        return BookingMapper.toBookingResponse(savedBooking);
    }

    @Transactional
    @Override
    public BookingResponse approveBooking(Long userId, Long bookingId, Boolean approved) {
        try {
            validationUtil.getBookingOrThrow(bookingId);
            validationUtil.getUserOrThrow(userId);
        } catch (NotFoundException e) {
            throw new BadRequestException("Объект не найден. Ошибка валидации для теста: " + e.getMessage());//для теста постмана
        }
        Booking booking = validationUtil.getBookingOrThrow(bookingId);
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new BadRequestException("Пользователь не является владельцем вещи." +
                    " Подтвердить или отклонить бронирование может только владелец вещи");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new BadRequestException("Бронирование вещи с id = " + booking.getItem().getId() + "имеет статус " +
                    booking.getStatus() + "! На этом статусе подтвердить или отклонить бронирование невозможно!");
        }
        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }
        log.info("Пользователь {} успешно изменил статус бронирования с id {} на {}",
                userId, bookingId, booking.getStatus());
        return BookingMapper.toBookingResponse(booking);
    }

    @Override
    public BookingResponse getBookingById(Long userId, Long bookingId) {
        validationUtil.getUserOrThrow(userId);
        Booking booking = validationUtil.getBookingOrThrow(bookingId);
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Получить данные о бронировании может" +
                    " только автор бронирования либо владелец вещи");
        }
        log.info("Пользователем с id = {} получена информация о бронировании с id = {}", userId, bookingId);
        return BookingMapper.toBookingResponse(booking);
    }

    @Override
    public Collection<BookingResponse> getAllByBooker(Long userId, String stateParam) {
        validationUtil.getUserOrThrow(userId);
        BookingState state;
        try {
            state = BookingState.valueOf(stateParam.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unknown state: " + stateParam);
        }
        LocalDateTime now = LocalDateTime.now();
        Sort sortByStartDesc = Sort.by(Sort.Direction.DESC, "start");
        List<Booking> bookings = switch (state) {
            case ALL      -> bookingRepository.findByBooker_Id(userId, sortByStartDesc);
            case CURRENT  -> bookingRepository.findByBooker_IdAndStartIsBeforeAndEndIsAfter(userId, now, now, sortByStartDesc);
            case PAST     -> bookingRepository.findByBooker_IdAndEndIsBefore(userId, now, sortByStartDesc);
            case FUTURE   -> bookingRepository.findByBooker_IdAndStartIsAfter(userId, now, sortByStartDesc);
            case WAITING  -> bookingRepository.findByBooker_IdAndStatus(userId, BookingStatus.WAITING, sortByStartDesc);
            case REJECTED -> bookingRepository.findByBooker_IdAndStatus(userId, BookingStatus.REJECTED, sortByStartDesc);
        };
        log.info("Пользователем с id = {} получена информация о бронированиях с фильтром {}", userId, state);
        return bookings.stream()
                .map(BookingMapper::toBookingResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<BookingResponse> getAllByOwner(Long userId, String stateParam) {
        validationUtil.getUserOrThrow(userId);
        BookingState state;
        try {
            state = BookingState.valueOf(stateParam.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unknown state: " + stateParam);
        }
        LocalDateTime now = LocalDateTime.now();
        Sort sortByStartDesc = Sort.by(Sort.Direction.DESC, "start");
        List<Booking> bookings = switch (state) {
            case ALL      -> bookingRepository.findByItem_Owner_Id(userId, sortByStartDesc);
            case CURRENT  -> bookingRepository.findByItem_Owner_IdAndStartIsBeforeAndEndIsAfter(userId, now, now, sortByStartDesc);
            case PAST     -> bookingRepository.findByItem_Owner_IdAndEndIsBefore(userId, now, sortByStartDesc);
            case FUTURE   -> bookingRepository.findByItem_Owner_IdAndStartIsAfter(userId, now, sortByStartDesc);
            case WAITING  -> bookingRepository.findByItem_Owner_IdAndStatus(userId, BookingStatus.WAITING, sortByStartDesc);
            case REJECTED -> bookingRepository.findByItem_Owner_IdAndStatus(userId, BookingStatus.REJECTED, sortByStartDesc);
        };
        log.info("Владельцем с id = {} получена информация о бронированиях его вещей с фильтром {}", userId, state);
        return bookings.stream()
                .map(BookingMapper::toBookingResponse)
                .collect(Collectors.toList());
    }
}