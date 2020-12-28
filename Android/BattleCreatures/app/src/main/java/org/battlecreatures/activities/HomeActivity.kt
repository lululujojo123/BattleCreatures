/*
 * Copyright (c) 2020 lululujojo123
 *
 * HomeActivity.kt
 *
 * created by: Andreas G.
 * last edit \ by: 2020/12/28 \ Andreas G.
 */

package org.battlecreatures.activities

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import org.battlecreatures.R

/**
 * The home activity providing the main entrance point to the game and other functions
 */
class HomeActivity : AppCompatActivity() {
    companion object{
        /**
         * Static constant with the class name for logging purposes
         */
        private const val TAG: String = "HomeActivity"
    }

    /**
     * Android related onCreate method preparing all the views from xml file
     *
     * @param savedInstanceState Bundle with the saved instance state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Super classes onCreate method
        super.onCreate(savedInstanceState)

        // Loading the layout from xml file
        setContentView(R.layout.activity_home)
    }

    /**
     * Android related onBackPressed method for overriding the normal back button functionality
     */
    override fun onBackPressed() {
        // Creating the onClickListener for the AlertDialog
        val dialogClickListener: DialogInterface.OnClickListener = DialogInterface.OnClickListener { _: DialogInterface, which: Int ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    // Close the application
                    finish()
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                    // Do nothing
                }
            }
        }

        // Creating the alert dialog builder
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.dialog_exit_application_text)).setPositiveButton(getString(R.string.yes), dialogClickListener)
            .setNegativeButton(getString(R.string.no), dialogClickListener)

        // Creating the alert dialog object and disable the touch event while touching outside of the dialog
        val alert: AlertDialog = builder.create()
        alert.setCanceledOnTouchOutside(false)
        alert.show()
    }
}