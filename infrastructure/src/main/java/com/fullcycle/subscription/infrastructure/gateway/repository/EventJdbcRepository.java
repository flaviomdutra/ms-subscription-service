package com.fullcycle.subscription.infrastructure.gateway.repository;

import com.fullcycle.subscription.domain.DomainEvent;
import com.fullcycle.subscription.domain.utils.InstantUtils;
import com.fullcycle.subscription.infrastructure.json.Json;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;

@Repository
public class EventJdbcRepository {
    private final JdbcClient jdbcClient;

    public EventJdbcRepository(final JdbcClient jdbcClient) {
        this.jdbcClient = Objects.requireNonNull(jdbcClient);
    }

    public List<DomainEvent> allEventsOfAggregate(final String aggregateId, final String aggregateType) {
        final var sql = "SELECT event_id, processed, aggregate_id, aggregate_type, event_type, event_date, event_data FROM events WHERE aggregate_id = :aggregateId and aggregate_type = :aggregateType";
        final var params = Map.of("aggregateId", aggregateId, "aggregateType", aggregateType);

        return this.jdbcClient.sql(sql).params(params).query(eventMapper())
                .stream()
                .map(this::toDomainEvent)
                .toList();
    }

    private DomainEvent toDomainEvent(final Event event) {
        try {
            return (DomainEvent) Json.readValue(event.eventData(), Class.forName(event.eventType()));

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private RowMapper<Event> eventMapper() {
        return (rs, num) ->
                new Event(
                        rs.getLong("event_id"),
                        rs.getBoolean("processed"),
                        rs.getString("aggregate_id"),
                        rs.getString("aggregate_type"),
                        rs.getString("event_type"),
                        rs.getObject("event_date", Instant.class),
                        rs.getBytes("event_data")
                );
    }

    public void saveAll(final Collection<DomainEvent> events) {
        for (var ev : events) {
            this.insertEvent(Event.newEvent(ev.aggregateId(), ev.aggregateType(), ev.getClass().getCanonicalName(), Json.writeValueAsBytes(ev)));
        }
    }

    private void insertEvent(final Event event) {
        final var sql = """
                INSERT INTO events (processed, aggregate_id, aggregate_type, event_type, event_date, event_data)
                VALUES (:processed, :aggregateId, :aggregateType, :eventType, :eventDate, :eventData)
                """;

        final var params = new HashMap<String, Object>();
        params.put("processed", event.processed());
        params.put("aggregateId", event.aggregateId());
        params.put("aggregateType", event.aggregateType());
        params.put("eventType", event.eventType());
        params.put("eventDate", event.eventDate());
        params.put("eventData", event.eventData());

        try {
            this.jdbcClient.sql(sql).params(params).update();
        } catch (DataIntegrityViolationException ex) {
            throw ex;
        }
    }

    private record Event(Long eventId, boolean processed, String aggregateId, String aggregateType, String eventType,
                         Instant eventDate, byte[] eventData) {

        public static Event newEvent(String aggregateId, String aggregateType, String eventType, byte[] data) {
            return new Event(null, false, aggregateId, aggregateType, eventType, InstantUtils.now(), data);
        }
    }
}

