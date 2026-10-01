package org.igniters.qa.sut.dto;

public record LoginResponse(String token, String role, String name) {
}
