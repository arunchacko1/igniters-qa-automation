package org.igniters.qa.sut.repository;

import org.igniters.qa.sut.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}
