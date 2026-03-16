package com.example.tinycell.ui.navigation

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Sealed class defining all available routes in the app.
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object CreateListing : Screen("create_listing")
    object Profile : Screen("profile")
    object AllChats : Screen("all_chats")
    object ListingDetail : Screen("listing_detail/{listingId}") {
        fun createRoute(listingId: String) = "listing_detail/$listingId"
    }
    object Chat : Screen("chat/{chatRoomId}/{listingId}/{listingTitle}/{otherUserId}/{otherUserName}") {
        fun createRoute(
            chatRoomId: String,
            listingId: String,
            listingTitle: String,
            otherUserId: String,
            otherUserName: String
        ): String {
            val encodedTitle = URLEncoder.encode(listingTitle, StandardCharsets.UTF_8.toString())
            val encodedName = URLEncoder.encode(otherUserName, StandardCharsets.UTF_8.toString())
            return "chat/$chatRoomId/$listingId/$encodedTitle/$otherUserId/$encodedName"
        }

        fun decodeTitle(encoded: String): String = URLDecoder.decode(encoded, StandardCharsets.UTF_8.toString())
        fun decodeName(encoded: String): String = URLDecoder.decode(encoded, StandardCharsets.UTF_8.toString())
    }

    object MyListings : Screen("my_listings")
    object MyFavorites : Screen("my_favorites")

    object ListingChats : Screen("listing_chats/{listingId}/{listingTitle}") {
        fun createRoute(listingId: String, listingTitle: String): String {
            val encodedTitle = URLEncoder.encode(listingTitle, StandardCharsets.UTF_8.toString())
            return "listing_chats/$listingId/$encodedTitle"
        }
        fun decodeTitle(encoded: String): String = URLDecoder.decode(encoded, StandardCharsets.UTF_8.toString())
    }

    // [NEW]: Public Profile route
    object PublicProfile : Screen("public_profile/{userId}/{userName}") {
        fun createRoute(userId: String, userName: String): String {
            val encodedName = URLEncoder.encode(userName, StandardCharsets.UTF_8.toString())
            return "public_profile/$userId/$encodedName"
        }
        fun decodeName(encoded: String): String = URLDecoder.decode(encoded, StandardCharsets.UTF_8.toString())
    }

    // [NEW]: Notifications route
    object Notifications : Screen("notifications")
}
