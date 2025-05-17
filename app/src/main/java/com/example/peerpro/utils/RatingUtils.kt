package com.example.peerpro.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object RatingsUtils {
    private val firestore = FirebaseFirestore.getInstance()


    suspend fun fetchAverageRating(ratedUserId: String): Pair<Float, Int> {
        return try {
            val allRatings = firestore.collection("ratings")
                .get()
                .await()

            val relevantRatings = allRatings.documents
                .filter { it.id.endsWith(ratedUserId) } // Check if rating ID ends with ratedUserId
                .mapNotNull { it.getLong("rating")?.toInt() }

            calculateAverageRating(relevantRatings)
        } catch (e: Exception) {
            Log.e("RatingsUtils", "Error fetching ratings", e)
            Pair(0f, 0)
        }
    }


    suspend fun getExistingRating(ratingId: String): Int? {
        return try {
            val document = firestore.collection("ratings")
                .document(ratingId)
                .get()
                .await()
            document.getLong("rating")?.toInt()
        } catch (e: Exception) {
            Log.e("RatingsUtils", "Error getting rating value", e)
            null
        }
    }

    private fun calculateAverageRating(ratings: List<Int>): Pair<Float, Int> {
        if (ratings.isEmpty()) return Pair(0f, 0)
        val sum = ratings.sum().toFloat()
        val count = ratings.size
        return Pair(sum / count, count)
    }
}