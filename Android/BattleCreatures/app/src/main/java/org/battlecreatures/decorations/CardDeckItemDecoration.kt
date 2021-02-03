/*
 * Copyright (c) 2021 lululujojo123
 *
 * CardDeckItemDecoration.kt
 *
 * created by: Andreas G.
 * last edit \ by: 2021/01/28 \ Andreas G.
 */

package org.battlecreatures.decorations

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * CardDeckItemDecoration class for proper spacing between the Recycler grid elements. Making the recycler view look
 * a little more polish.
 *
 * @param space The space between the single recycler items
 * @param spanCount Providing the correct spanCount for the space calculation
 * @param includeEdge Provide the information whether the items should start directly at the top or after a short Edge space
 */
class CardDeckItemDecoration (private val space: Int, private val spanCount: Int, private val includeEdge: Boolean): RecyclerView.ItemDecoration() {
    /**
     * Overriding the RecyclerView.ItemDecoration related getItemOffsets method for individualization of the recycler view item
     *
     * @param outRect The rectangle outer border of the item
     * @param view The current view for the item
     * @param parent The parent of the view means the current recycler view object
     * @param state The current state of the recycler view
     */
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        // Gather and calculate the important information by using the provided parameters
        val position: Int = parent.getChildAdapterPosition(view)
        val column: Int = position % spanCount

        // Modify the outRect provided by calling class according to the fact if edges are requested or not
        if (includeEdge) {
            // Calculate the proper space between the elements horizontally
            outRect.left = space - column * space / spanCount
            outRect.right = (column + 1) * space / spanCount

            // Set the top space if the provided item is in the first row
            if (position < spanCount) {
                outRect.top = space * 2
            }

            // Set the bottom space for vertical spacing
            outRect.bottom = space * 2
        } else {
            // Calculate the proper space between the elements horizontally
            outRect.right = column * space / spanCount
            outRect.left = space - (column + 1) * space / spanCount

            // Set the top space for every item after the first row
            if (position >= spanCount) {
                outRect.top = space
            }
        }
    }
}