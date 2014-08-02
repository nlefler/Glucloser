package com.nlefler.glucloser.util.database.upgrade;

import com.nlefler.glucloser.model.bolus.BolusCreationSQL;
import com.nlefler.glucloser.model.food.FoodCreationSQL;
import com.nlefler.glucloser.model.meal.MealCreationSQL;
import com.nlefler.glucloser.model.meterdata.MeterDataCreationSQL;
import com.nlefler.glucloser.model.place.PlaceCreationSQL;

import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.sprinkles.annotations.Table;

public class ZeroToOne extends DatabaseUpgrader {
    @Override
    protected String[] getUpgradeCommands() {
        String[] tableCreationSQLs = new String[] {
                BolusCreationSQL.creationSQL,
                FoodCreationSQL.creationSQL,
                MealCreationSQL.creationSQL,
                MeterDataCreationSQL.creationSQL,
                PlaceCreationSQL.creationSQL
        };

        return tableCreationSQLs;
    }
}
