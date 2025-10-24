package com.example.backend.common.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

/**
 * Normalizes untrusted HTML to a conservative allow list so stored content
 * cannot execute scripts or inline event handlers when rendered.
 */
public final class HtmlSanitizer {

    private static final Safelist SAFE_LIST = Safelist.relaxed()
            .addAttributes("img", "src", "alt", "title")
            .addProtocols("img", "src", "http", "https", "data")
            .addAttributes("a", "target")
            .addEnforcedAttribute("a", "rel", "noopener noreferrer")
            .addAttributes(":all", "class");

    private static final Document.OutputSettings OUTPUT_SETTINGS = new Document.OutputSettings()
            .prettyPrint(false);

    private HtmlSanitizer() {
    }

    public static String sanitize(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        return Jsoup.clean(html, "", SAFE_LIST, OUTPUT_SETTINGS);
    }
}
