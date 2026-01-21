package com.example.tinycell.ui.navigation

/**
 * [NAV_ROUTE_DOCUMENTATION]
 * Sealed class defining all available routes in the app.
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object CreateListing : Screen("create_listing")
    object Profile : Screen("profile") // Added Profile route
    object ListingDetail : Screen("listing_detail/{listingId}") {
        fun createRoute(listingId: String) = "listing_detail/$listingId"
    }
}
