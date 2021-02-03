/*
 * Copyright (c) 2021 lululujojo123
 *
 * RecyclerViewExtensions.kt
 *
 * created by: Andreas G.
 * last edit \ by: 2021/1/30 \ Andreas G.
 */

package org.battlecreatures.extensions

import androidx.recyclerview.widget.RecyclerView

inline fun  <reified T: RecyclerView.ViewHolder> RecyclerView.forEachVisibleHolder(
    action: (T) -> Unit
) {
    for (i in 0 until childCount) {
        action(getChildViewHolder(getChildAt(i)) as T)
    }
}