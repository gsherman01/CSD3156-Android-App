package com.example.tinycell.data.local

import com.example.tinycell.data.local.entity.CategoryEntity
import com.example.tinycell.data.local.entity.FavouriteEntity
import com.example.tinycell.data.local.entity.ListingEntity
import com.example.tinycell.data.local.entity.UserEntity
import kotlinx.coroutines.flow.first

/**
 * DatabaseSeeder - Populates database with sample data for testing.
 *
 * Creates realistic marketplace data for Singapore context:
 * - Categories (Electronics, Fashion, Home & Living, Books, Sports)
 * - Users (Singapore names)
 * - Listings (realistic prices in SGD, Singapore locations)
 * - Sample favourites
 *
 * Usage:
 * Call seedDatabase() from MainActivity onCreate or Application class.
 * Only seeds once - checks if data already exists.
 */
class DatabaseSeeder(private val database: AppDatabase) {

    /**
     * Seed the database with all sample data.
     * Checks if already seeded to avoid duplicates.
     */
    suspend fun seedDatabase() {
        // Check if already seeded
        val existingCategories = database.categoryDao().getAllCategories().first()
        if (existingCategories.isNotEmpty()) {
            println("✅ Database already seeded, skipping...")
            return
        }

        println("🌱 Seeding database with sample data...")

        seedCategories()
        seedUsers()
        seedListings()
        seedFavourites()

        println("✅ Database seeding complete!")
    }

    /**
     * Seed categories.
     */
    private suspend fun seedCategories() {
        val categories = listOf(
            CategoryEntity("cat_electronics", "Electronics", "📱"),
            CategoryEntity("cat_fashion", "Fashion", "👗"),
            CategoryEntity("cat_home", "Home & Living", "🏠"),
            CategoryEntity("cat_books", "Books", "📚"),
            CategoryEntity("cat_sports", "Sports & Outdoors", "⚽")
        )
        database.categoryDao().insertAll(categories)
        println("  📁 Seeded ${categories.size} categories")
    }

    /**
     * Seed users with Singapore names.
     */
    private suspend fun seedUsers() {
        val currentTime = System.currentTimeMillis()
        val users = listOf(
            UserEntity("user_1", "Alice Tan", "alice.tan@tinycell.sg", null, currentTime),
            UserEntity("user_2", "Bob Lim", "bob.lim@tinycell.sg", null, currentTime),
            UserEntity("user_3", "Charlie Wong", "charlie.wong@tinycell.sg", null, currentTime),
            UserEntity("user_4", "Diana Chen", "diana.chen@tinycell.sg", null, currentTime),
            UserEntity("user_5", "Ethan Koh", "ethan.koh@tinycell.sg", null, currentTime),
            UserEntity("user_6", "Fiona Lee", "fiona.lee@tinycell.sg", null, currentTime),
            UserEntity("user_7", "George Ng", "george.ng@tinycell.sg", null, currentTime),
            UserEntity("user_8", "Hannah Ong", "hannah.ong@tinycell.sg", null, currentTime)
        )
        database.userDao().insertAll(users)
        println("  👥 Seeded ${users.size} users")
    }

    /**
     * Seed marketplace listings with realistic Singapore data.
     */
    private suspend fun seedListings() {
        val currentTime = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000L

        val listings = listOf(
            // Electronics
            ListingEntity(
                id = "list_1",
                title = "iPhone 13 Pro - 256GB (Like New)",
                description = "Excellent condition iPhone 13 Pro in Sierra Blue. 256GB storage, battery health at 95%. Includes original box, charger, and case. Screen protector applied since day one. No scratches or dents. Selling because I upgraded to iPhone 15.",
                price = 899.00,
                userId = "user_1",
                categoryId = "cat_electronics",
                location = "Orchard",
                imageUrls = "",
                createdAt = currentTime - (oneDay * 2),
                isSold = false
            ),
            ListingEntity(
                id = "list_2",
                title = "MacBook Air M1 - 8GB/256GB",
                description = "2020 MacBook Air with M1 chip. Perfect for students and light work. Battery cycle count: 45. Comes with original charger and sleeve. Minor scratches on bottom case, screen is flawless.",
                price = 850.00,
                userId = "user_2",
                categoryId = "cat_electronics",
                location = "Tampines",
                imageUrls = "",
                createdAt = currentTime - (oneDay * 5),
                isSold = false
            ),
            ListingEntity(
                id = "list_3",
                title = "Sony WH-1000XM4 Headphones",
                description = "Industry-leading noise cancelling headphones. Used for 6 months, excellent condition. Includes carrying case, cables, and original packaging. Perfect for commuting.",
                price = 250.00,
                userId = "user_3",
                categoryId = "cat_electronics",
                location = "Jurong East",
                imageUrls = "",
                createdAt = currentTime - (oneDay * 1),
                isSold = false
            ),
            ListingEntity(
                id = "list_4",
                title = "iPad 9th Gen - 64GB WiFi",
                description = "Perfect for note-taking and entertainment. Comes with Apple Pencil 1st gen and case. Battery health excellent. Ideal for students.",
                price = 380.00,
                userId = "user_4",
                categoryId = "cat_electronics",
                location = "Bishan",
                imageUrls = "",
                createdAt = currentTime - (oneDay * 3),
                isSold = false
            ),

            // Fashion
            ListingEntity(
                id = "list_5",
                title = "Uniqlo Winter Jacket - Size M",
                description = "Brand new with tags. Ultra Light Down jacket in navy blue. Perfect for air-conditioned offices or travel to cold countries. Size M fits like regular Medium.",
                price = 45.00,
                userId = "user_5",
                categoryId = "cat_fashion",
                location = "Bugis",
                imageUrls = "",
                createdAt = currentTime - (oneDay * 4),
                isSold = false
            ),
            ListingEntity(
                id = "list_6",
                title = "Nike Air Max 90 - UK 9",
                description = "Authentic Nike Air Max 90 in white/grey colorway. Worn 3 times only. Bought from official Nike store at Orchard. Comes with original box and receipt.",
                price = 120.00,
                userId = "user_6",
                categoryId = "cat_fashion",
                location = "Clementi",
                imageUrls = "",
                createdAt = currentTime - (oneDay * 7),
                isSold = false
            ),
            ListingEntity(
                id = "list_7",
                title = "Vintage Denim Jacket - Unisex",
                description = "Classic 90s style denim jacket. Oversized fit. Perfect for Singapore weather. Has character with slight distressing. One of a kind piece!",
                price = 35.00,
                userId = "user_7",
                categoryId = "cat_fashion",
                location = "Tiong Bahru",
                imageUrls = "",
                createdAt = currentTime - (oneDay * 2),
                isSold = false
            ),

            // Home & Living
            ListingEntity(
                id = "list_8",
                title = "IKEA Study Desk - White",
                description = "IKEA MICKE desk in white. 73x50cm, perfect for small HDB rooms. Includes drawer unit. Self-collection only. Dismantled for easy transport.",
                price = 60.00,
                userId = "user_8",
                categoryId = "cat_home",
                location = "Punggol",
                imageUrls = "",
                createdAt = currentTime - (oneDay * 6),
                isSold = false
            ),
            ListingEntity(
                id = "list_9",
                title = "Dyson V8 Vacuum Cleaner",
                description = "Cordless vacuum in working condition. 2 years old, well maintained. Includes all attachments and wall mount. Battery lasts 30-40 mins.",
                price = 280.00,
                userId = "user_1",
                categoryId = "cat_home",
                location = "Ang Mo Kio",
                imageUrls = "",
                createdAt = currentTime - (oneDay * 8),
                isSold = false
            ),
            ListingEntity(
                id = "list_10",
                title = "Standing Desk Converter - Bamboo",
                description = "Eco-friendly bamboo standing desk converter. Adjustable height, fits monitors up to 32 inches. Great for improving posture. Used for 3 months.",
                price = 90.00,
                userId = "user_2",
                categoryId = "cat_home",
                location = "Serangoon",
                imageUrls = "",
                createdAt = currentTime - (oneDay * 3),
                isSold = false
            ),

            // Books
            ListingEntity(
                id = "list_11",
                title = "Computer Science Textbooks (Bundle of 3)",
                description = "Bundle includes: Introduction to Algorithms (3rd Ed), Clean Code, and Design Patterns. Perfect for CS students. Minimal highlighting, great condition.",
                price = 60.00,
                userId = "user_3",
                categoryId = "cat_books",
                location = "NUS",
                imageUrls = "",
                createdAt = currentTime - (oneDay * 5),
                isSold = false
            ),
            ListingEntity(
                id = "list_12",
                title = "Harry Potter Complete Set (Hardcover)",
                description = "All 7 books in hardcover. British edition with original artwork. Great condition, read once. Perfect gift or collection piece.",
                price = 120.00,
                userId = "user_4",
                categoryId = "cat_books",
                location = "Woodlands",
                imageUrls = "",
                createdAt = currentTime - (oneDay * 10),
                isSold = false
            ),
            ListingEntity(
                id = "list_13",
                title = "The Lean Startup - Eric Ries",
                description = "Entrepreneurship must-read. Paperback in excellent condition. Minimal wear. Great for startup founders and business students.",
                price = 15.00,
                userId = "user_5",
                categoryId = "cat_books",
                location = "Raffles Place",
                imageUrls = "",
                createdAt = currentTime - (oneDay * 1),
                isSold = false
            ),

            // Sports
            ListingEntity(
                id = "list_14",
                title = "Decathlon Mountain Bike - 21 Speed",
                description = "Rockrider 340 mountain bike. 21-speed gears, front suspension. Used occasionally for Park Connector rides. Well maintained, just serviced.",
                price = 180.00,
                userId = "user_6",
                categoryId = "cat_sports",
                location = "Bedok",
                imageUrls = "",
                createdAt = currentTime - (oneDay * 4),
                isSold = false
            ),
            ListingEntity(
                id = "list_15",
                title = "Yoga Mat - 6mm Thick (Manduka)",
                description = "Premium Manduka yoga mat. 6mm thickness, non-slip surface. Used for home practice. Includes carrying strap. Like new condition.",
                price = 45.00,
                userId = "user_7",
                categoryId = "cat_sports",
                location = "Tanjong Pagar",
                imageUrls = "",
                createdAt = currentTime - (oneDay * 2),
                isSold = false
            ),
            ListingEntity(
                id = "list_16",
                title = "Gym Dumbbell Set - 5kg to 20kg",
                description = "Adjustable dumbbell set with rack. Space-saving design perfect for HDB. Includes 5kg, 10kg, 15kg, 20kg weights. Barely used.",
                price = 150.00,
                userId = "user_8",
                categoryId = "cat_sports",
                location = "Yishun",
                imageUrls = "",
                createdAt = currentTime - (oneDay * 9),
                isSold = false
            ),

            // More Electronics
            ListingEntity(
                id = "list_17",
                title = "Nintendo Switch OLED - White",
                description = "Barely used Switch OLED. Comes with 3 games (Mario Kart, Animal Crossing, Zelda). Includes case and screen protector. Perfect condition.",
                price = 380.00,
                userId = "user_1",
                categoryId = "cat_electronics",
                location = "Kallang",
                imageUrls = "",
                createdAt = currentTime - (oneDay * 6),
                isSold = false
            ),
            ListingEntity(
                id = "list_18",
                title = "Mechanical Keyboard - Cherry MX Blue",
                description = "Custom mechanical keyboard with Cherry MX Blue switches. RGB backlight. Great for typing and gaming. Keycaps have slight shine.",
                price = 95.00,
                userId = "user_2",
                categoryId = "cat_electronics",
                location = "Queenstown",
                imageUrls = "",
                createdAt = currentTime - (oneDay * 11),
                isSold = false
            ),

            // More Fashion
            ListingEntity(
                id = "list_19",
                title = "Adidas Originals Track Jacket - Size L",
                description = "Classic 3-stripes track jacket in black. Size L fits true to size. Vintage 2000s style. Rare piece in excellent condition.",
                price = 55.00,
                userId = "user_3",
                categoryId = "cat_fashion",
                location = "Hougang",
                imageUrls = "",
                createdAt = currentTime - (oneDay * 7),
                isSold = false
            ),
            ListingEntity(
                id = "list_20",
                title = "Levi's 511 Jeans - W32 L32",
                description = "Slim fit dark blue jeans. Worn a few times, washed once. No fading or damage. Classic versatile style.",
                price = 40.00,
                userId = "user_4",
                categoryId = "cat_fashion",
                location = "Marina Bay",
                imageUrls = "",
                createdAt = currentTime - (oneDay * 3),
                isSold = false
            )
        )

        database.listingDao().insertAll(listings)
        println("  🏷️  Seeded ${listings.size} listings")
    }

    /**
     * Seed some sample favourites.
     */
    private suspend fun seedFavourites() {
        val currentTime = System.currentTimeMillis()
        val favourites = listOf(
            // User 1 favourites
            FavouriteEntity(userId = "user_1", listingId = "list_5", createdAt = currentTime),
            FavouriteEntity(userId = "user_1", listingId = "list_11", createdAt = currentTime),
            FavouriteEntity(userId = "user_1", listingId = "list_14", createdAt = currentTime),

            // User 2 favourites
            FavouriteEntity(userId = "user_2", listingId = "list_3", createdAt = currentTime),
            FavouriteEntity(userId = "user_2", listingId = "list_6", createdAt = currentTime),

            // User 3 favourites
            FavouriteEntity(userId = "user_3", listingId = "list_1", createdAt = currentTime),
            FavouriteEntity(userId = "user_3", listingId = "list_17", createdAt = currentTime)
        )

        favourites.forEach { database.favouriteDao().addFavourite(it) }
        println("  ❤️  Seeded ${favourites.size} favourites")
    }
}
