package com.example.backend.util;

import org.springframework.data.repository.Repository;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class SlugConverter {
    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    public static String toSlug(String input) {
        String whitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(whitespace, Normalizer.Form.NFD);
        String slug = NON_LATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }
}
