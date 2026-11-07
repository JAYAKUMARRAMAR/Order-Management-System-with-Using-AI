package com.jayakumar.auth.dto;

import java.util.Locale;

final class AuthInputNormalizer {

    private AuthInputNormalizer() {
    }

    static String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }
}
