package dev.outfix.tag;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serves the predefined list of clothing tags.
 *
 * Tags are defined here in the backend (not the frontend) to prevent typos
 * and ensure all wardrobe items use consistent, searchable categories.
 * This endpoint is public — no login required.
 */
@RestController
@RequestMapping("/api/tags")
public class TagController {

    /** The full list of available tags the frontend can show in dropdowns. */
    private static final List<String> AVAILABLE_TAGS = List.of(
            "Men", "Women", "Unisex",
            "Top", "Bottom", "Outerwear",
            "Footwear", "Accessories",
            "Formal", "Casual", "Sportswear");

    /**
     * GET /api/tags
     * Returns the list of available clothing tags.
     */
    @GetMapping
    public ResponseEntity<List<String>> getAvailableTags() {
        return ResponseEntity.ok(AVAILABLE_TAGS);
    }
}
