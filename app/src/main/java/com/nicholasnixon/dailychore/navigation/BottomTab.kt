package com.nicholasnixon.dailychore.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.nicholasnixon.dailychore.R

/**
 * The tabs in the bottom bar, in the order they appear.
 * The bar is built by looping over BottomTab.entries, so adding a tab only means adding a line here.
 *
 * @StringRes / @DrawableRes tell the compiler these Ints must be resource ids (R.string… / R.drawable…).
 * "@param:" says the annotation goes on the constructor parameter (avoids a Kotlin 2.2 warning).
 */
enum class BottomTab(
    @param:StringRes val label: Int,
    @param:DrawableRes val icon: Int,
    val route: Any
) {
    TODAY(R.string.tab_today, R.drawable.ic_tab_today, KidRoute),
    REWARDS(R.string.tab_rewards, R.drawable.ic_tab_rewards, RewardsRoute),
    PARENT(R.string.tab_parent, R.drawable.ic_tab_parent, ParentRoute)
}
