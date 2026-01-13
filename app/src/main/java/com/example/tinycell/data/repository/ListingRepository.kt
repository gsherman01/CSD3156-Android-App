package com.example.tinycell.data.repository

import com.example.tinycell.data.model.Listing

/**
 * LISTING REPOSITORY
 *
 * Current: In-memory mock data for development/testing
 * Future: Replace with Room Database + Retrofit API calls
 *
 * Migration path:
 * 1. Add Room entities and DAOs for offline caching
 * 2. Add Retrofit API service for remote data
 * 3. Implement repository pattern: API -> Cache -> UI
 * 4. Add StateFlow for reactive updates
 */
class ListingRepository {

    /**
     * Get all marketplace listings.
     *
     * Future implementation:
     * - Fetch from remote API (Retrofit)
     * - Cache in local database (Room)
     * - Return Flow<List<Listing>> for reactive updates
     * - Add pagination support
     * - Add filtering/sorting parameters
     */
    fun getListings(): List<Listing> {
        return listOf(
            Listing(
                id = "1",
                title = "iPhone 13 Pro - 256GB (Like New)",
                price = 799.99,
                category = "Electronics",
                sellerName = "Alice",
                description = "Excellent condition iPhone 13 Pro in Sierra Blue. " +
                        "256GB storage, battery health at 95%. " +
                        "Includes original box, charger, and case. " +
                        "Screen protector applied since day one. No scratches or dents. " +
                        "Selling because I upgraded to iPhone 15.",
                imageUrl = null  // Placeholder for future image URL
            ),
            Listing(
                id = "2",
                title = "Ergonomic Gaming Chair - RGB Lighting",
                price = 149.50,
                category = "Furniture",
                sellerName = "Bob",
                description = "High-quality gaming chair with lumbar support and adjustable armrests. " +
                        "Features RGB lighting (can be turned off). " +
                        "Used for 6 months, in great condition. " +
                        "Maximum weight capacity: 150kg. " +
                        "Perfect for long gaming or work sessions. " +
                        "Pickup only - located near downtown.",
                imageUrl = null
            ),
            Listing(
                id = "3",
                title = "Computer Science Textbooks Bundle",
                price = 45.00,
                category = "Books",
                sellerName = "Charlie",
                description = "Bundle of 3 CS textbooks from university courses:\n" +
                        "• Introduction to Algorithms (3rd Edition)\n" +
                        "• Clean Code by Robert Martin\n" +
                        "• Design Patterns: Elements of Reusable Object-Oriented Software\n\n" +
                        "All in good condition with minimal highlighting. " +
                        "Great for students or self-learners. " +
                        "Will sell separately if needed.",
                imageUrl = null
            ),
            Listing(
                id = "4",
                title = "Mechanical Keyboard - Cherry MX Blue",
                price = 89.99,
                category = "Electronics",
                sellerName = "David",
                description = "Custom mechanical keyboard with Cherry MX Blue switches. " +
                        "RGB backlight with multiple modes. " +
                        "Used for 1 year, fully functional. " +
                        "Keycaps have slight shine but no functional issues. " +
                        "Great for typing and gaming.",
                imageUrl = null
            ),
            Listing(
                id = "5",
                title = "Vintage Leather Jacket - Size M",
                price = 65.00,
                category = "Fashion",
                sellerName = "Emma",
                description = "Genuine leather jacket from the 90s. " +
                        "Classic style, size Medium (fits like modern Small). " +
                        "Well-maintained, leather is soft and supple. " +
                        "Minor wear on elbows adds to the vintage aesthetic. " +
                        "Perfect for cool weather or motorcycle riding.",
                imageUrl = null
            ),
            Listing(
                id = "6",
                title = "Standing Desk Converter - Bamboo",
                price = 120.00,
                category = "Furniture",
                sellerName = "Frank",
                description = "Eco-friendly bamboo standing desk converter. " +
                        "Adjustable height mechanism works smoothly. " +
                        "Fits monitors up to 32 inches. " +
                        "Used for 3 months in home office setup. " +
                        "Great for improving posture and reducing back pain. " +
                        "Dimensions: 80cm x 40cm.",
                imageUrl = null
            )
        )
    }

    /**
     * Get a single listing by ID.
     *
     * Future implementation:
     * - Query from local Room database first
     * - Fetch from API if not cached
     * - Return Flow<Listing?> for reactive updates
     */
    fun getListingById(id: String): Listing? {
        return getListings().find { it.id == id }
    }
}
