package ru.practicum.shareit.request;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    Collection<ItemRequest> findAllByRequesterId(Long requesterId, Sort sort);

    Collection<ItemRequest> findAllByRequesterIdNot(Long userId, Sort sort);
}
