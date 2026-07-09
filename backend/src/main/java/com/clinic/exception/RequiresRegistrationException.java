package com.clinic.exception;

import lombok.Getter;

@Getter
public class RequiresRegistrationException extends RuntimeException {
    private final String email;
    private final String name;
    private final String picture;

    public RequiresRegistrationException(String email, String name, String picture) {
        super("REQUIRES_REGISTRATION");
        this.email = email;
        this.name = name;
        this.picture = picture;
    }
}
