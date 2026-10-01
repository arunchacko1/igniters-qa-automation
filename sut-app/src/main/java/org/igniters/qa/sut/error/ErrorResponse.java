package org.igniters.qa.sut.error;

/** Shape of every JSON error body the API returns: a stable code plus a human message. */
public record ErrorResponse(String code, String message) {
}
