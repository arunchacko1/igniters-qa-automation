package org.igniters.qa.sut.repository;

import java.util.List;
import java.util.Optional;
import org.igniters.qa.sut.domain.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    Optional<Registration> findByUserIdAndEventId(Long userId, Long eventId);

    List<Registration> findByUserId(Long userId);

    long countByEventId(Long eventId);

    // Single DELETE statement, not "load then remove one by one" — matters
    // once an event has many registrations, and keeps the delete atomic.
    @Modifying
    @Query("DELETE FROM Registration r WHERE r.eventId = :eventId")
    void deleteByEventId(@Param("eventId") Long eventId);
}
