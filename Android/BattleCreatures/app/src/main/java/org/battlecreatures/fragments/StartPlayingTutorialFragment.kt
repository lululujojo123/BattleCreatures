/*
 * Copyright (c) 2020 lululujojo123
 *
 * StartPlayingTutorialFragment.kt
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
 * The start playing fragment providing a simple tutorial slide for what to do after scanning the card's QR codes
 */
class StartPlayingTutorialFragment : Fragment() {
    companion object{
        /**
         * Static constant with the class name for logging purposes
         */
        private const val TAG: String = "StartPlayingTutorialFragment"
    }

    /**
     * Android related onCreateView method inflating the respective layout xml file
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_start_playing_tutorial, container, false)
    }

    /**
     * Android related onViewCreated method adding a onClickListener to the sword image view
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // The super classes onViewCreated method
        super.onViewCreated(view, savedInstanceState)

        // Adding a onClickListener to the ImageView
        view.findViewById<ImageView>(R.id.btnSwordNext2).setOnClickListener {
            val viewPager: ViewPager = view.parent as ViewPager

            viewPager.currentItem++
        }
    }
}