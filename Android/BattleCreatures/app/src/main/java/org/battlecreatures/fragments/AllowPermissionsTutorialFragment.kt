/*
 * Copyright (c) 2020 lululujojo123
 *
 * AllowPermissionsTutorialFragment.kt
 *
 * created by: Andreas G.
 * last edit \ by: 2020/12/28 \ Andreas G.
 */

package org.battlecreatures.fragments

import android.Manifest
import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import org.battlecreatures.R

/**
 * The tutorial fragment responsible for gathering all the required permissions
 */
class AllowPermissionsTutorialFragment : Fragment() {
    companion object{
        /**
         * Static constant with the class name for logging purposes
         */
        private const val TAG: String = "AllowPermissionsTutorialFragment"
    }

    /**
     * Android related onCreateView method inflating the layout xml file.
     *
     * @param inflater The layout inflater for the current view
     * @param container The view group container the fragment should be placed
     * @param savedInstanceState Bundle with the saved instance state
     * @return The completely prepared view
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_allow_permissions_tutorial, container, false)
    }

    /**
     * Android related onViewCreated method adding the onClickListener to the image view
     *
     * @param view The view that was created during fragment initialisation
     * @param savedInstanceState Bundle with the saved instance state
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Super classes onViewCreated method
        super.onViewCreated(view, savedInstanceState)

        // Adding onClickListener to the ImageView
        view.findViewById<ImageView>(R.id.btnSwordNext3).setOnClickListener {
            ActivityCompat.requestPermissions(activity as Activity, arrayOf(Manifest.permission.CAMERA), 1)
        }
    }
}