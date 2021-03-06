package com.nlefler.glucloser.a.dataSource

import com.nlefler.glucloser.a.models.BolusPattern
import com.nlefler.glucloser.a.models.BolusRate
import java.util.*

/**
 * Created by nathan on 10/10/15.
 */
public class BolusPatternUtils {
    companion object {
        public fun InsulinForCarbsAtCurrentTime(bolusPattern: BolusPattern, carbValue: Int): Float {
            val cal = Calendar.getInstance()
            val curMilSecs = cal.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000 +
                            cal.get(Calendar.MINUTE) * 60 * 1000 + cal.get(Calendar.SECOND) * 1000

            var sortedRates = bolusPattern.rates.sortedBy { rate -> rate.ordinal }
            if (sortedRates.isEmpty()) {
                return 0f
            }

            var activeRate: BolusRate? = null
            for (rate in sortedRates) {
                if (rate.startTime <= curMilSecs) {
                    activeRate = rate
                }
                if (curMilSecs < rate.startTime) {
                    break;
                }
            }
            if (activeRate == null && sortedRates.last().startTime.compareTo(curMilSecs) != 1) {
                activeRate = sortedRates.last()
            }

            if (activeRate?.carbsPerUnit == 0) {
                return 0f
            }
            return carbValue.toFloat() / activeRate?.carbsPerUnit!!.toFloat()
        }
    }
}
