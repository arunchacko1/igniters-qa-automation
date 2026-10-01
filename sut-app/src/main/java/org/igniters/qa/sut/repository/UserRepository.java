package org.igniters.qa.sut.repository;

import java.util.Optional;
import org.igniters.qa.sut.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
