package com.pranith73.meridian.shared.validation;

import com.pranith73.meridian.shared.error.ValidationException;

public final class Require {

    private Require() {
    }

    public static <T> T notNull(T value, String message) {
        if (value == null) {
            throw new ValidationException(message);
        }
        return value;
    }

    public static String notBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(message);
        }
        return value;
    }

    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new ValidationException(message);
        }
    }
}