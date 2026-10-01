package org.igniters.qa.sut.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Deliberately simple opaque-token store, kept in memory. A single SUT
 * instance is all this portfolio project ever runs, so there's no need for
 * a signed JWT or a shared token store across instances — that complexity
 * would obscure the point of this app rather than demonstrate anything.
 */
@Component
public class TokenService {

    private static final Duration TTL = Duration.ofHours(4);

    private final Map<String, TokenData> tokens = new ConcurrentHashMap<>();

    public record TokenData(String email, Instant expiresAt) {
    }

    public String issueToken(String email) {
        String token = UUID.randomUUID().toString();
        tokens.put(token, new TokenData(email, Instant.now().plus(TTL)));
        return token;
    }

    public Optional<String> resolveEmail(String token) {
        TokenData data = tokens.get(token);
        if (data == null) {
            return Optional.empty();
        }
        if (Instant.now().isAfter(data.expiresAt())) {
            tokens.remove(token);
            return Optional.empty();
        }
        return Optional.of(data.email());
    }
}
