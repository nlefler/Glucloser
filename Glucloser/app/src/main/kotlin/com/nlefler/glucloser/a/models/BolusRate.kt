package com.nlefler.glucloser.a.models

import io.requery.*
import java.util.*

/**
 * Created by nathan on 8/30/15.
 */
@Entity
interface BolusRate: Persistable {
    @get:Key
    var primaryId: String
    var ordinal: Int
    var carbsPerUnit: Int
    var startTime: Int

    // Trying to resolve requery annotation parsing issues
    var pattern: BolusPattern?
}

