/*
 * Copyright (c) 2020 lululujojo123
 *
 * ScanQRTutorialFragment.kt
 *
 * created by: Andreas G.
 * last edit \ by: 2020/12/27 \ Andreas G.
 */

package org.battlecreatures.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.ViewPager
import org.battlecreatures.R

/**
 * The fragment class for the scan QR code tutorial slide
 */
class ScanQRTutorialFragment : Fragment() {
    companion object{
        /**
         * Static constant with the class name for logging purposes
         */
        private const val TAG: String = "ScanQRTutorialFragment"
    }

    /**
     * The android related onCreateView method inflating the fragment's layout
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scan_qr_tutorial, container, false)
    }

    /**
     * The android related onViewCreated method attaching a onClickListener to the next button
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Super classes onViewCreated method
        super.onViewCreated(view, savedInstanceState)

        // Adding a onClickListener to the ImageView
        view.findViewById<ImageView>(R.id.btnSwordNext).setOnClickListener{
            val viewPager: ViewPager = view.parent as ViewPager

            viewPager.currentItem++
        }
    }
}