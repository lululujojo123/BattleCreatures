/*
 * Copyright (c) 2020 lululujojo123
 *
 * TutorialPagerAdapter.kt
 *
 * created by: Andreas G.
 * last edit \ by: 2020/12/27 \ Andreas G.
 */

package org.battlecreatures.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import org.battlecreatures.R
import org.battlecreatures.fragments.AllowPermissionsTutorialFragment
import org.battlecreatures.fragments.ScanQRTutorialFragment
import org.battlecreatures.fragments.StartPlayingTutorialFragment

/**
 * The tutorial pager adapter controlling the view pager logics for the tutorial activity
 *
 * @constructor Creates a tutorial pager adapter with the provided fragment manager
 * @param fm The fragment manager to be used for this view pager adapter
 * @param c The context to be used for the resources access
 */
class TutorialPagerAdapter(fm: FragmentManager, private val c: Context): FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    companion object{
        /**
         * Static constant with the class name for logging purposes
         */
        private const val TAG: String = "TutorialPagerAdapter"
    }

    /**
     * Method providing the count of pages within this view pager
     *
     * @return The number of pages as an Integer
     */
    override fun getCount(): Int  = 3

    /**
     * Method providing the respective fragment for the specific page
     *
     * @param i Which page's fragment should be returned
     * @return The fragment for the requested page
     */
    override fun getItem(i: Int): Fragment {
        return when (i) {
            0 -> ScanQRTutorialFragment()
            1 -> StartPlayingTutorialFragment()
            2 -> AllowPermissionsTutorialFragment()
            else -> {
                // This should never happen
                Fragment()
            }
        }
    }

    /**
     * Method providing the name of the requested page
     *
     * @param position The position number of the page the name should be returned
     * @return Returns the name of the requested page
     */
    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> c.getString(R.string.scan_qr_tutorial_title)
            1 -> c.getString(R.string.start_playing_tutorial_title)
            2 -> c.getString(R.string.allow_permissions_tutorial_title)
            else -> {
                // This should never happen
                ""
            }
        }
    }
}