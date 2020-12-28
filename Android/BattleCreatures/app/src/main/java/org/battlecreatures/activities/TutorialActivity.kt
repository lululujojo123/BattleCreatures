/*
 * Copyright (c) 2020 lululujojo123
 *
 * TutorialActivity.kt
 *
 * created by: Andreas G.
 * last edit \ by: 2020/12/28 \ Andreas G.
 */

package org.battlecreatures.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import org.battlecreatures.R
import org.battlecreatures.adapters.TutorialPagerAdapter

/**
 * Tutorial activity just providing the view pager for the tutorial slides
 */
class TutorialActivity : AppCompatActivity() {
    companion object{
        /**
         * Static constant with the class name for logging purposes
         */
        private const val TAG: String = "TutorialActivity"
    }

    /**
     * Android related onCreate method preparing all views and adding the pager adapter
     *
     * @param savedInstanceState The bundle with the saved instance state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Super classes onCreate method
        super.onCreate(savedInstanceState)

        // Set the activity's layout by xml file
        setContentView(R.layout.activity_tutorial)

        // Add the tutorial pager adapter to the ViewPager object
        findViewById<ViewPager>(R.id.tutorialPager).adapter = TutorialPagerAdapter(supportFragmentManager, baseContext)
    }

    /**
     * Android related onRequestPermissionsResult method handling the result of the permission request dialog
     *
     * @param requestCode The code provided at request dialog creation
     * @param permissions The permissions which were requested
     * @param grantResults The results of all the permission requests
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        // Super classes onRequestPermissionsResult
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Switching through the possible requestCodes
        when (requestCode) {
            // Request code of CAMERA permission request
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted.
                    // Set the shared preference for the completion of the tutorial.
                    val shrdPref: SharedPreferences = getSharedPreferences(getString(R.string.application_shared_preferences), Context.MODE_PRIVATE)
                    shrdPref.edit().putBoolean(getString(R.string.sp_value_tutorial_completed), true).apply()

                    // Switch to home activity.
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
            }
        }
    }
}