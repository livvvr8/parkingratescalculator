package com.recio.parkingratecalculator.activities.main

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.text.format.DateUtils
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.recio.parkingratecalculator.R
import com.recio.parkingratecalculator.activities.base.BaseActivity
import com.recio.parkingratecalculator.classes.DatabaseHelper
import com.recio.parkingratecalculator.classes.DatabaseHelper.Companion.ADDITIONAL_FEE_COLUMN
import com.recio.parkingratecalculator.classes.DatabaseHelper.Companion.INITIAL_FEE_COLUMN
import com.recio.parkingratecalculator.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*


class MainActivity : BaseActivity<ActivityMainBinding>() {

    private lateinit var timeIn: Date
    private lateinit var timeOut: Date

    private var timeInMilliseconds: Long = 0
    private var timeOutMilliseconds: Long = 0

    private var timeInText: String = ""
    private var timeOutText: String = ""

    private var carInitialRate: Int = 0
    private var carAdditionalRate: Int = 0

    private var motorcycleInitialRate: Int = 0
    private var motorcycleAdditionalRate: Int = 0

    override fun getActivityBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        retrieveParkingRates()
        buttonClickListener()
    }

    private fun retrieveParkingRates() {
        val db = DatabaseHelper(this, null)
        val cursor = db.getRates()
        if (cursor==null || cursor.count<=0) {
            db.addRates("car", 50, 10)
            db.addRates("motorcycle", 30, 30)
        }

        db.getCarRates()
        val carRatesCursor = db.getCarRates()
        if (carRatesCursor!=null) {
            carRatesCursor.moveToFirst()
            carInitialRate = carRatesCursor.getInt(carRatesCursor.getColumnIndexOrThrow(INITIAL_FEE_COLUMN))
            carAdditionalRate = carRatesCursor.getInt(carRatesCursor.getColumnIndexOrThrow(ADDITIONAL_FEE_COLUMN))
        }

        db.getMotorcycleRates()
        val motorcycleRatesCursor = db.getMotorcycleRates()
        if (motorcycleRatesCursor!=null) {
            motorcycleRatesCursor.moveToFirst()
            motorcycleInitialRate = motorcycleRatesCursor.getInt(motorcycleRatesCursor.getColumnIndexOrThrow(INITIAL_FEE_COLUMN))
            motorcycleAdditionalRate = motorcycleRatesCursor.getInt(motorcycleRatesCursor.getColumnIndexOrThrow(ADDITIONAL_FEE_COLUMN))
        }
    }

    private fun buttonClickListener() {
        binding.icRates.setOnClickListener {
            displayParkingRatesDialogBox()
        }

        binding.textDateIn.setOnClickListener {
            val currentCalendar = Calendar.getInstance()

            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                currentCalendar.set(Calendar.YEAR, year)
                currentCalendar.set(Calendar.MONTH, monthOfYear)
                currentCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val timePicker =
                    MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_12H)
                        .setHour(currentCalendar.get(Calendar.HOUR))
                        .setMinute(currentCalendar.get(Calendar.MINUTE))
                        .setTitleText(getString(R.string.select_time_in))
                        .build()
                timePicker.show(supportFragmentManager, "")

                timePicker.addOnPositiveButtonClickListener {
                    currentCalendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                    currentCalendar.set(Calendar.MINUTE, timePicker.minute)

                    val selectedDate = currentCalendar.time
                    val printDateFormat = SimpleDateFormat("MM/dd/yyyy, hh:mm aa", Locale.ENGLISH)
                    timeInText = printDateFormat.format(selectedDate)
                    timeIn = selectedDate
                    timeInMilliseconds = currentCalendar.timeInMillis

                    binding.textDateIn.setText(timeInText as CharSequence, TextView.BufferType.NORMAL)
                }
            }

            DatePickerDialog(this, dateSetListener,
                currentCalendar.get(Calendar.YEAR),
                currentCalendar.get(Calendar.MONTH),
                currentCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.textDateOut.setOnClickListener {
            if (this::timeIn.isInitialized && binding.textDateIn.text.toString().isNotEmpty()) {
                val currentCalendar = Calendar.getInstance()

                val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                    currentCalendar.set(Calendar.YEAR, year)
                    currentCalendar.set(Calendar.MONTH, monthOfYear)
                    currentCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    val timePicker =
                        MaterialTimePicker.Builder()
                            .setTimeFormat(TimeFormat.CLOCK_12H)
                            .setHour(currentCalendar.get(Calendar.HOUR))
                            .setMinute(currentCalendar.get(Calendar.MINUTE))
                            .setTitleText(getString(R.string.select_time_in))
                            .build()
                    timePicker.show(supportFragmentManager, "")

                    timePicker.addOnPositiveButtonClickListener {
                        currentCalendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                        currentCalendar.set(Calendar.MINUTE, timePicker.minute)

                        val selectedDate = currentCalendar.time
                        val printDateFormat = SimpleDateFormat("MM/dd/yyyy, hh:mm aa", Locale.ENGLISH)
                        timeOutText = printDateFormat.format(selectedDate)
                        timeOut = selectedDate
                        timeOutMilliseconds = currentCalendar.timeInMillis

                        if (timeOut.after(timeIn) && timeOutText!=timeInText) {
                            binding.textDateOut.setText(timeOutText as CharSequence, TextView.BufferType.NORMAL)
                        } else {
                            displayDialogBoxMessage(getString(R.string.error), getString(R.string.message_time_out_must_be_after_time_in), getString(R.string.okay))
                        }
                    }
                }

                DatePickerDialog(this, dateSetListener,
                    currentCalendar.get(Calendar.YEAR),
                    currentCalendar.get(Calendar.MONTH),
                    currentCalendar.get(Calendar.DAY_OF_MONTH)).show()
            } else {
                displayToastMessage(getString(R.string.message_select_time_in_date_first))
            }
        }

        binding.btnCompute.setOnClickListener {
            when {
                binding.radioButtonCar.isChecked -> {
                    val relativeTimeStr = DateUtils.getRelativeTimeSpanString(timeInMilliseconds, timeOutMilliseconds, DateUtils.SECOND_IN_MILLIS)
                    val timeDifference = Integer.parseInt(relativeTimeStr[0].toString())
                    val additionalHours = timeDifference-2
                    val additionalFee = additionalHours*carAdditionalRate
                    val totalAmount = carInitialRate + additionalFee

                    val dialogMessage = "Time In: $timeInText\nTime Out: $timeOutText\nHours: $timeDifference\nTotal Amount: $totalAmount"
                    displayParkingFeeDialogBoxMessage(getString(R.string.parking_fee), dialogMessage, getString(R.string.close))
                }
                binding.radioButtonMotorcycle.isChecked -> {
                    val timeInCalendar = Calendar.getInstance()
                    val timeOutCalendar = Calendar.getInstance()
                    timeInCalendar.time = timeIn
                    timeOutCalendar.time = timeOut
                    val sameDay = timeInCalendar[Calendar.DAY_OF_YEAR] == timeOutCalendar[Calendar.DAY_OF_YEAR] &&
                            timeInCalendar[Calendar.YEAR] == timeOutCalendar[Calendar.YEAR]

                    if (sameDay) {
                        val totalAmount = motorcycleInitialRate

                        val dialogMessage = "Time In: $timeInText\nTime Out: $timeOutText\nTotal Amount: $totalAmount"
                        displayParkingFeeDialogBoxMessage(getString(R.string.parking_fee), dialogMessage, getString(R.string.close))
                    } else {
                        val days = ChronoUnit.DAYS.between(
                                timeIn.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                                timeOut.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                            ) + 1
                        val totalDays = Integer.parseInt(days.toString())
                        val totalAmount = totalDays*motorcycleAdditionalRate

                        val dialogMessage = "Time In: $timeInText\nTime Out: $timeOutText\nTotal cut offs: $totalDays\nTotal Amount: $totalAmount"
                        displayParkingFeeDialogBoxMessage(getString(R.string.parking_fee), dialogMessage, getString(R.string.close))
                    }

                }
                binding.radioButtonEmployeeVehicle.isChecked -> {
                    val relativeTimeStr = DateUtils.getRelativeTimeSpanString(timeInMilliseconds, timeOutMilliseconds, DateUtils.SECOND_IN_MILLIS)
                    val timeDifference = Integer.parseInt(relativeTimeStr[0].toString())
                    val additionalHours = timeDifference-2
                    val additionalFee = additionalHours*carAdditionalRate
                    val totalAmount = carInitialRate + additionalFee
                    val discount = totalAmount*.2
                    val finalAmount = totalAmount-discount

                    val dialogMessage = "Time In: $timeInText\nTime Out: $timeOutText\nHours: $timeDifference\nSubtotal: $totalAmount\nDiscount: $discount\nTotal Amount: $finalAmount"
                    displayParkingFeeDialogBoxMessage(getString(R.string.parking_fee), dialogMessage, getString(R.string.close))
                }
            }
        }
    }

    private fun displayParkingFeeDialogBoxMessage(
        dialogTitle: String,
        dialogMessage: String,
        positiveButtonLabel: String
    ) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(dialogTitle)
        builder.setMessage(dialogMessage)
        builder.setPositiveButton(positiveButtonLabel) { dialog, _ ->
            binding.textDateIn.text?.clear()
            binding.textDateOut.text?.clear()
            binding.radioButtonCar.isSelected = true

            dialog.dismiss()
        }
        builder.setCancelable(false)
        builder.show()
    }

    private fun displayParkingRatesDialogBox() {
        val dialog = Dialog(this)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_parking_rates)

        val btnClose = dialog.findViewById(R.id.btn_close) as LinearLayout

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setCancelable(true)
        dialog.show()
    }

    override fun onBackPressed() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.confirm_exit_dialog_title))
        builder.setMessage(getString(R.string.confirm_exit_dialog_message))
        builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
            super.onBackPressed()
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        val alert: AlertDialog = builder.create()
        alert.show()
    }

}