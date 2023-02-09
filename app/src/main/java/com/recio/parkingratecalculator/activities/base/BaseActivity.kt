package com.recio.parkingratecalculator.activities.base

import android.os.Bundle
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity <AVBinding : ViewBinding> : AppCompatActivity() {

    protected lateinit var binding: AVBinding

    protected abstract fun getActivityBinding(): AVBinding

    private fun initializeActivityViewBinding() {
        binding = getActivityBinding()
        setContentView(binding.root)
    }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeActivityViewBinding()
        supportActionBar?.hide()
    }

    protected fun displayDialogBoxMessage(
        dialogTitle: String,
        dialogMessage: String,
        positiveButtonLabel: String
    ) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(dialogTitle)
        builder.setMessage(dialogMessage)
        builder.setPositiveButton(positiveButtonLabel) { dialog, _ ->
            dialog.dismiss()
        }
        builder.setCancelable(false)
        builder.show()
    }

    protected fun displayToastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}