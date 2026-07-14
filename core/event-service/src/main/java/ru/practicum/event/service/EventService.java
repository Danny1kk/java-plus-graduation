package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.CategoryClient;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.event.dto.*;
import ru.practicum.event.enums.EventState;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exeption.BadRequestException;
import ru.practicum.exeption.ConflictException;
import ru.practicum.exeption.NotFoundException;
import ru.practicum.request.RequestClient;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.user.UserClient;
import ru.practicum.user.dto.UserDto;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Comparator;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final UserClient userClient;
    private final CategoryClient categoryClient;
    private final StatsClient statsClient;
    private final RequestClient requestClient;
    private final EventMapper eventMapper;

    @Transactional
    public EventFullDto create(Long userId, NewEventDto dto) {
        if (!dto.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Дата события должна быть не ранее чем через 2 часа от текущего момента");
        }

        UserDto initiator = userClient.getUser(userId);
        if (initiator == null) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        CategoryDto category = categoryClient.getCategory(dto.getCategory());
        if (category == null) {
            throw new NotFoundException("Категория с id=" + dto.getCategory() + " не найдена");
        }

        Event event = new Event();
        event.setAnnotation(dto.getAnnotation());
        event.setCategoryId(category.getId());
        event.setDescription(dto.getDescription());
        event.setEventDate(dto.getEventDate());
        event.setLocation(dto.getLocation());
        event.setPaid(dto.getPaid() != null ? dto.getPaid() : false);
        event.setParticipantLimit(dto.getParticipantLimit() != null ? dto.getParticipantLimit() : 0);
        event.setRequestModeration(dto.getRequestModeration() != null ? dto.getRequestModeration() : true);
        event.setTitle(dto.getTitle());
        event.setInitiatorId(userId);
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());

        Event saved = eventRepository.save(event);
        return eventMapper.toFullDto(saved, 0L, 0L);
    }

    public List<EventShortDto> getByUser(Long userId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return eventRepository.findByInitiatorId(userId, pageable)
                .stream()
                .map(event -> {
                    Long confirmed = requestClient.countByEventIdAndStatus(event.getId(), "CONFIRMED");
                    return eventMapper.toShortDto(event, confirmed, event.getViews());
                })
                .collect(Collectors.toList());
    }

    public EventFullDto getByUserAndEvent(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(
                        "Событие с id=" + eventId + " не найдено у пользователя с id=" + userId));
        Long confirmed = requestClient.countByEventIdAndStatus(event.getId(), "CONFIRMED");
        return eventMapper.toFullDto(event, confirmed, event.getViews());
    }

    @Transactional
    public EventFullDto updateByUser(Long userId, Long eventId, UpdateEventUserRequest dto) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(
                        "Событие с id=" + eventId + " не найдено у пользователя с id=" + userId));

        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new ConflictException("Редактировать можно только отменённые или ожидающие модерацию события");
        }

        if (dto.getAnnotation() != null) event.setAnnotation(dto.getAnnotation());
        if (dto.getCategory() != null) {
            CategoryDto category = categoryClient.getCategory(dto.getCategory());
            if (category == null) {
                throw new NotFoundException("Категория с id=" + dto.getCategory() + " не найдена");
            }
            event.setCategoryId(category.getId());
        }
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getEventDate() != null) {
            if (dto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new BadRequestException("Дата события должна быть не ранее чем через 2 часа от текущего момента");
            }
            event.setEventDate(dto.getEventDate());
        }
        if (dto.getLocation() != null) event.setLocation(dto.getLocation());
        if (dto.getPaid() != null) event.setPaid(dto.getPaid());
        if (dto.getParticipantLimit() != null) event.setParticipantLimit(dto.getParticipantLimit());
        if (dto.getRequestModeration() != null) event.setRequestModeration(dto.getRequestModeration());
        if (dto.getTitle() != null) event.setTitle(dto.getTitle());

        if (dto.getStateAction() != null) {
            if (dto.getStateAction().equals("SEND_TO_REVIEW")) {
                event.setState(EventState.PENDING);
            } else if (dto.getStateAction().equals("CANCEL_REVIEW")) {
                event.setState(EventState.CANCELED);
            }
        }

        Event saved = eventRepository.save(event);
        Long confirmed = requestClient.countByEventIdAndStatus(saved.getId(), "CONFIRMED");
        return eventMapper.toFullDto(saved, confirmed, saved.getViews());
    }

    public List<EventShortDto> searchPublic(String text, List<Long> categories, Boolean paid,
                                            LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                            Boolean onlyAvailable, String sort, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        final LocalDateTime start = (rangeStart == null && rangeEnd == null) ? LocalDateTime.now() : rangeStart;
        final LocalDateTime end = rangeEnd;

        if (start != null && end != null && start.isAfter(end)) {
            throw new BadRequestException("Дата начала диапазона не может быть позже даты конца");
        }

        Specification<Event> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("state"), EventState.PUBLISHED));

            if (text != null && !text.isBlank()) {
                String search = "%" + text.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("annotation")), search),
                        cb.like(cb.lower(root.get("description")), search)
                ));
            }
            if (categories != null && !categories.isEmpty()) {
                predicates.add(root.get("categoryId").in(categories));
            }
            if (paid != null) {
                predicates.add(cb.equal(root.get("paid"), paid));
            }
            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), start));
            }
            if (end != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), end));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Event> eventPage = eventRepository.findAll(spec, pageable);
        List<Event> events = eventPage.getContent();
        Map<Long, Long> viewsMap = getViewsMap(events);

        List<EventShortDto> result = events.stream()
                .map(event -> {
                    Long confirmed = requestClient.countByEventIdAndStatus(event.getId(), "CONFIRMED");
                    Long views = viewsMap.getOrDefault(event.getId(), 0L);
                    return eventMapper.toShortDto(event, confirmed, views);
                })
                .filter(dto -> {
                    if (onlyAvailable == null || !onlyAvailable) return true;
                    Event original = events.stream().filter(e -> e.getId().equals(dto.getId())).findFirst().orElse(null);
                    return original == null || original.getParticipantLimit() == 0 || dto.getConfirmedRequests() < original.getParticipantLimit();
                })
                .collect(Collectors.toList());

        if (sort != null) {
            if (sort.equalsIgnoreCase("EVENT_DATE")) {
                result.sort(Comparator.comparing(EventShortDto::getEventDate));
            } else if (sort.equalsIgnoreCase("VIEWS")) {
                result.sort(Comparator.comparing(EventShortDto::getViews).reversed());
            }
        }

        return result;
    }

    public List<EventFullDto> searchAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                          LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                          int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        Specification<Event> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (users != null && !users.isEmpty()) {
                predicates.add(root.get("initiatorId").in(users));
            }
            if (states != null && !states.isEmpty()) {
                predicates.add(root.get("state").in(states));
            }
            if (categories != null && !categories.isEmpty()) {
                predicates.add(root.get("categoryId").in(categories));
            }
            if (rangeStart != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
            }
            if (rangeEnd != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<Event> events = eventRepository.findAll(spec, pageable).getContent();
        Map<Long, Long> viewsMap = getViewsMap(events);

        return events.stream()
                .map(event -> {
                    Long confirmed = requestClient.countByEventIdAndStatus(event.getId(), "CONFIRMED");
                    Long views = viewsMap.getOrDefault(event.getId(), 0L);
                    return eventMapper.toFullDto(event, confirmed, views);
                })
                .collect(Collectors.toList());
    }

//    public List<EventShortDto> searchPublic(String text, List<Long> categories, Boolean paid,
//                                            LocalDateTime rangeStart, LocalDateTime rangeEnd,
//                                            Boolean onlyAvailable, String sort, int from, int size) {
//
//        Pageable pageable = PageRequest.of(from / size, size);
//
//        if (categories != null && categories.isEmpty()) {
//            categories = null;
//        }
//
//        if (rangeStart == null && rangeEnd == null) {
//            rangeStart = LocalDateTime.now();
//        }
//
//        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
//            throw new BadRequestException("Дата начала диапазона не может быть позже даты конца");
//        }
//
//        Page<Event> eventPage = eventRepository.searchPublic(text, categories, paid, rangeStart, rangeEnd, pageable);
//        List<Event> events = eventPage.getContent();
//        Map<Long, Long> viewsMap = getViewsMap(events);
//
//        return events.stream()
//                .map(event -> {
//                    Long confirmed = requestClient.countByEventIdAndStatus(event.getId(), "CONFIRMED");
//                    Long views = viewsMap.getOrDefault(event.getId(), 0L);
//                    return eventMapper.toShortDto(event, confirmed, views);
//                })
//                .collect(Collectors.toList());
//    }


//    public List<EventFullDto> searchAdmin(List<Long> users, List<EventState> states, List<Long> categories,
//                                          LocalDateTime rangeStart, LocalDateTime rangeEnd,
//                                          int from, int size) {
//        Pageable pageable = PageRequest.of(from / size, size);
//
//        if (users != null && users.isEmpty()) users = null;
//        if (states != null && states.isEmpty()) states = null;
//        if (categories != null && categories.isEmpty()) categories = null;
//
//        if (rangeStart == null) rangeStart = LocalDateTime.now().minusYears(100);
//        if (rangeEnd == null) rangeEnd = LocalDateTime.now().plusYears(100);
//
//        List<Event> events = eventRepository.searchAdmin(users, states, categories, rangeStart, rangeEnd, pageable).getContent();
//        Map<Long, Long> viewsMap = getViewsMap(events);
//
//        return events.stream()
//                .map(event -> {
//                    Long confirmed = requestClient.countByEventIdAndStatus(event.getId(), "CONFIRMED");
//                    Long views = viewsMap.getOrDefault(event.getId(), 0L);
//                    return eventMapper.toFullDto(event, confirmed, views);
//                })
//                .collect(Collectors.toList());
//    }

    public EventFullDto getPublic(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено");
        }

        Map<Long, Long> viewsMap = getViewsMap(List.of(event));
        Long views = viewsMap.getOrDefault(event.getId(), 0L);

        Long confirmed = requestClient.countByEventIdAndStatus(event.getId(), "CONFIRMED");
        return eventMapper.toFullDto(event, confirmed, views);
    }

    @Transactional
    public EventFullDto updateByAdmin(Long eventId, UpdateEventAdminRequest dto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));

        if (dto.getAnnotation() != null) event.setAnnotation(dto.getAnnotation());
        if (dto.getCategory() != null) {
            CategoryDto category = categoryClient.getCategory(dto.getCategory());
            if (category == null) {
                throw new NotFoundException("Категория с id=" + dto.getCategory() + " не найдена");
            }
            event.setCategoryId(category.getId());
        }
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getEventDate() != null) {
            if (dto.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                throw new BadRequestException("Дата начала события должна быть не ранее чем за час от даты публикации");
            }
            event.setEventDate(dto.getEventDate());
        }
        if (dto.getLocation() != null) event.setLocation(dto.getLocation());
        if (dto.getPaid() != null) event.setPaid(dto.getPaid());
        if (dto.getParticipantLimit() != null) event.setParticipantLimit(dto.getParticipantLimit());
        if (dto.getRequestModeration() != null) event.setRequestModeration(dto.getRequestModeration());
        if (dto.getTitle() != null) event.setTitle(dto.getTitle());

        if (dto.getStateAction() != null) {
            if (dto.getStateAction().equals("PUBLISH_EVENT")) {
                if (event.getState() != EventState.PENDING) {
                    throw new ConflictException("Опубликовать можно только событие в статусе PENDING");
                }

                if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                    throw new ConflictException("Дата начала события должна быть не ранее чем за час от даты публикации");
                }

                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (dto.getStateAction().equals("REJECT_EVENT")) {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException("Нельзя отклонить уже опубликованное событие");
                }
                event.setState(EventState.CANCELED);
            }
        }

        Event saved = eventRepository.save(event);
        Long confirmed = requestClient.countByEventIdAndStatus(saved.getId(), "CONFIRMED");
        return eventMapper.toFullDto(saved, confirmed, saved.getViews());
    }

    private Map<Long, Long> getViewsMap(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());

        try {
            List<ViewStatsDto> stats = statsClient.getStats(
                    LocalDateTime.now().minusYears(10),
                    LocalDateTime.now().plusYears(1),
                    uris,
                    true
            );

            return stats.stream()
                    .collect(Collectors.toMap(
                            dto -> {
                                String uri = dto.getUri();
                                return Long.parseLong(uri.substring(uri.lastIndexOf("/") + 1));
                            },
                            ViewStatsDto::getHits,
                            (a, b) -> a > b ? a : b
                    ));
        } catch (Exception e) {
            System.err.println("Не удалось получить статистику просмотров: " + e.getMessage());
            return Map.of();
        }
    }
}