package com.example.tinycell.data.repository

import com.example.tinycell.data.model.Listing

// this is hard coded data, to be changed?
class ListingRepository {

    fun getListings(): List<Listing> {
        return listOf(
            Listing("1", "iPhone 13", 800.0, "Electronics", "Alice"),
            Listing("2", "Gaming Chair", 150.0, "Furniture", "Bob"),
            Listing("3", "Textbooks", 40.0, "Books", "Charlie")
        )
    }

    fun getListingById(id: String): Listing? {
        return getListings().find { it.id == id }
    }
}
