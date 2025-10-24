package com.example.backend.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HtmlSanitizerTest {

    @Test
    void stripsDisallowedMarkup() {
        String sanitized = HtmlSanitizer.sanitize("<img src='x' onerror='alert(1)'>Hello<script>alert(1)</script>");

        assertTrue(sanitized.contains("<img"));
        assertTrue(sanitized.contains("Hello"));
        assertFalse(sanitized.contains("onerror"));
        assertFalse(sanitized.toLowerCase().contains("<script"));
    }

    @Test
    void returnsEmptyStringForNullOrBlank() {
        assertEquals("", HtmlSanitizer.sanitize(null));
        assertEquals("", HtmlSanitizer.sanitize("   "));
    }
}
