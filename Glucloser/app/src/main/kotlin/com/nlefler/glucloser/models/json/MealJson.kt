package com.nlefler.glucloser.models.json

import com.nlefler.glucloser.models.BloodSugar
import com.nlefler.glucloser.models.BolusPattern
import com.nlefler.glucloser.models.Food
import com.nlefler.glucloser.models.Place
import java.util.*

/**
 * Created by nathan on 1/31/16.
 */
public data class MealJson(
        val primaryId: String,
        val date: Date,
        val bolusPattern: BolusPattern?,
        val carbs: Int,
        val insulin: Float,
        val beforeSugar: BloodSugar?,
        val isCorrection: Boolean,
        val foods: List<Food>,
        val place: Place?
) {
}
