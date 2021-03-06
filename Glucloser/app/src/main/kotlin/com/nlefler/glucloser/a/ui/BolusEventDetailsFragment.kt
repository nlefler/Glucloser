package com.nlefler.glucloser.a.ui

import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import com.nlefler.glucloser.a.GlucloserApplication
import com.nlefler.glucloser.a.R
import com.nlefler.glucloser.a.dataSource.*
import com.nlefler.glucloser.a.models.*
import com.nlefler.glucloser.a.models.BolusEventDetailDelegate
import com.nlefler.glucloser.a.models.BolusPattern
import com.nlefler.glucloser.a.models.parcelable.BloodSugarParcelable
import com.nlefler.glucloser.a.models.parcelable.BolusEventParcelable
import com.nlefler.glucloser.a.models.parcelable.BolusPatternParcelable
import com.nlefler.glucloser.a.models.Food
import com.nlefler.glucloser.a.models.FoodDetailDelegate
import com.nlefler.glucloser.a.models.parcelable.FoodParcelable
import java.util.*

/**
 * Created by Nathan Lefler on 12/24/14.
 */
public class BolusEventDetailsFragment : Fragment() {
    var bolusPatternFactory: BolusPatternFactory? = null
    var foodFactory: FoodFactory? = null
    var pumpDataFactory: PumpDataFactory? = null

    private var placeName: String? = null
    private var bolusEventParcelable: BolusEventParcelable? = null

    private var carbValueField: EditText? = null
    private var insulinValueField: EditText? = null
    private var beforeSugarValueField: EditText? = null
    private var correctionValueBox: CheckBox? = null

    private var addFoodNameField: EditText? = null
    private var addFoodCarbField: EditText? = null
    private var foodListView: RecyclerView? = null
    private var foodListLayoutManager: RecyclerView.LayoutManager? = null
    private var foodListAdapter: FoodListRecyclerAdapter? = null
    private var foods: MutableList<Food> = ArrayList<Food>()

    private var totalCarbs = 0
    private var totalInsulin = 0f

    private var bolusPattern: BolusPattern? = null

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)

        val dataFactory = GlucloserApplication.sharedApplication?.rootComponent
        bolusPatternFactory = dataFactory?.bolusPatternFactory()
        foodFactory = dataFactory?.foodFactory()
        pumpDataFactory = dataFactory?.pumpDataFactory()

        this.bolusEventParcelable = getBolusEventParcelableFromBundle(bundle, getArguments(), getActivity().getIntent().getExtras())
        this.placeName = getPlaceNameFromBundle(bundle, getArguments(), getActivity().getIntent().getExtras())

        val bolusPatternParcelable = getBolusPatternFromBundle(bundle, getArguments(), getActivity().getIntent().getExtras())

        if (bolusPatternParcelable != null) {
            this.bolusPattern = bolusPatternFactory?.bolusPatternFromParcelable(bolusPatternParcelable)
        }
        else {
            val uuid = dataFactory?.userManager()?.sessionID()
            if (uuid != null) {
                pumpDataFactory?.currentCarbRatios(uuid)?.subscribe { pattern ->
                    bolusPattern = pattern
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        if (this.placeName != null) {
            outState?.putString(BolusEventDetailsFragment.Companion.BolusEventDetailPlaceNameBundleKey, this.placeName)
        }
        if (this.bolusEventParcelable != null) {
            outState?.putParcelable(BolusEventDetailsFragment.Companion.BolusEventDetailBolusEventParcelableBundleKey, this.bolusEventParcelable)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater!!.inflate(R.layout.fragment_bolus_event_edit_details, container, false)

        val placeNameField = rootView.findViewById(R.id.meal_edit_detail_place_name) as TextView
        this.carbValueField = rootView.findViewById(R.id.meal_edit_detail_total_carb_value) as EditText
        this.insulinValueField = rootView.findViewById(R.id.meal_edit_detail_total_insulin_value) as EditText
        this.beforeSugarValueField = rootView.findViewById(R.id.meal_edit_detail_blood_sugar_before_value) as EditText
        this.correctionValueBox = rootView.findViewById(R.id.meal_edit_detail_correction_value) as CheckBox

        val saveButton = rootView.findViewById(R.id.meal_edit_detail_save_button) as Button
        saveButton.setOnClickListener {v: View -> saveEventClicked() }

        if (this.placeName != null) {
            placeNameField.setText(this.placeName)
        }

        this.addFoodNameField = rootView.findViewById(R.id.meal_edit_detail_food_name_value) as EditText
        this.addFoodCarbField = rootView.findViewById(R.id.meal_edit_detail_food_carb_value) as EditText
        this.addFoodCarbField?.setOnEditorActionListener { textView, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                addFoodFromFields()
                this.addFoodNameField!!.requestFocus()
                true
            }
            else {
                false
            }
        }

        this.foodListView = rootView.findViewById(R.id.bolus_event_detail_food_list) as RecyclerView

        this.foodListLayoutManager = LinearLayoutManager(getActivity())
        this.foodListView!!.setLayoutManager(this.foodListLayoutManager)

        this.foodListAdapter = FoodListRecyclerAdapter(this.foods)
        this.foodListView!!.setAdapter(this.foodListAdapter)

        return rootView
    }

    fun addFoodFromFields() {
        if (activity !is FoodDetailDelegate) {
            return
        }

        val foodParcelable = FoodParcelable()
        foodParcelable.foodId = UUID.randomUUID().toString()

        val foodNameString = this.addFoodNameField!!.getText().toString()
        if (!foodNameString.isEmpty()) {
            foodParcelable.setFoodName(foodNameString)
        }
        else {
            return
        }

        val foodCarbString = this.addFoodCarbField!!.getText().toString()
        if (!foodCarbString.isEmpty()) {
            val carbValue = java.lang.Integer.valueOf(foodCarbString)
            foodParcelable.setCarbs(carbValue)
            addToTotalCarbs(carbValue)
        }
        else {
            foodParcelable.setCarbs(0)
        }

        val food = foodFactory?.foodFromParcelable(foodParcelable)
        if (food != null) {
            this.foods.add(food)
            this.foodListAdapter?.setFoods(this.foods)
        }

        (getActivity() as FoodDetailDelegate).foodDetailUpdated(foodParcelable)

        this.addFoodNameField!!.setText("")
        this.addFoodCarbField!!.setText("")
    }

    internal fun saveEventClicked() {
        if (getActivity() !is BolusEventDetailDelegate || this.bolusEventParcelable == null) {
            return
        }

        addFoodFromFields()

        this.bolusEventParcelable!!.date = Date()

        val beforeSugarString = this.beforeSugarValueField!!.getText().toString()
        if (!beforeSugarString.isEmpty()) {
            val beforeSugarParcelable = BloodSugarParcelable()
            beforeSugarParcelable.date = this.bolusEventParcelable!!.date
            beforeSugarParcelable.value = Integer.valueOf(beforeSugarString)
            this.bolusEventParcelable!!.bloodSugarParcelable = beforeSugarParcelable
        }

        if (this.insulinValueField!!.getText() != null && this.insulinValueField!!.getText().length > 0) {
            this.bolusEventParcelable!!.insulin = java.lang.Float.valueOf(this.insulinValueField!!.getText().toString())
        }
        if (this.carbValueField!!.getText() != null && this.carbValueField!!.getText().length > 0) {
            this.bolusEventParcelable!!.carbs = Integer.valueOf(this.carbValueField!!.getText().toString())!!
        }
        this.bolusEventParcelable!!.isCorrection = this.correctionValueBox!!.isSelected()

        if (this.bolusPattern != null) {
            this.bolusEventParcelable!!.bolusPatternParcelable = bolusPatternFactory?.parcelableFromBolusPattern(this.bolusPattern!!)
        }

        (getActivity() as BolusEventDetailDelegate).bolusEventDetailUpdated(this.bolusEventParcelable!!)
    }

    private fun getPlaceNameFromBundle(savedInstanceState: Bundle?, args: Bundle?, extras: Bundle?): String {
        for (bundle in arrayOf<Bundle?>(savedInstanceState, args, extras)) {
            if (bundle?.containsKey(BolusEventDetailsFragment.Companion.BolusEventDetailPlaceNameBundleKey) == true) {
                return bundle?.getString(BolusEventDetailsFragment.Companion.BolusEventDetailPlaceNameBundleKey) ?: ""
            }
        }

        return ""
    }

    private fun getBolusPatternFromBundle(savedInstanceState: Bundle?, args: Bundle?, extras: Bundle?): BolusPatternParcelable? {
        for (bundle in arrayOf<Bundle?>(savedInstanceState, args, extras)) {
            if (bundle?.containsKey(BolusEventDetailsFragment.Companion.BolusEventDetailBolusPatternParcelableBundleKey) == true) {
                return bundle?.getParcelable<BolusPatternParcelable>(BolusEventDetailsFragment.Companion.BolusEventDetailBolusPatternParcelableBundleKey) ?: null
            }
        }

        return null
    }

    private fun getBolusEventParcelableFromBundle(savedInstanceState: Bundle?, args: Bundle?, extras: Bundle?): BolusEventParcelable? {
        for (bundle in arrayOf<Bundle?>(savedInstanceState, args, extras)) {
            if (bundle?.containsKey(BolusEventDetailsFragment.Companion.BolusEventDetailBolusEventParcelableBundleKey) ?: null != null) {
                return bundle!!.getParcelable<Parcelable>(BolusEventDetailsFragment.Companion.BolusEventDetailBolusEventParcelableBundleKey) as BolusEventParcelable?
            }
        }
        return null
    }

    private fun addToTotalCarbs(carbValue: Int) {
        this.totalCarbs += carbValue
        this.carbValueField!!.setText(java.lang.String.valueOf(this.totalCarbs))
        updateInsulinWithCarbs(carbValue)
    }

    private fun updateInsulinWithCarbs(carbValue: Int) {
        if (this.bolusPattern == null) {
            return;
        }
        this.totalInsulin += BolusPatternUtils.InsulinForCarbsAtCurrentTime(this.bolusPattern!!, carbValue)
        val frmtr = Formatter()
        frmtr.format("%.2f", this.totalInsulin);
        this.insulinValueField!!.setText(frmtr.out().toString())
    }

    companion object {
        private val LOG_TAG = "BolusEventDetailsFragment"

        public val BolusEventDetailPlaceNameBundleKey: String = "MealDetailPlaceNameBundleKey"
        public val BolusEventDetailBolusEventParcelableBundleKey: String = "MealDetailBolusEventParcelableBundleKey"
        public val BolusEventDetailBolusPatternParcelableBundleKey: String = "MealDetailBolusPatternParcelableBundleKey"
    }
}
