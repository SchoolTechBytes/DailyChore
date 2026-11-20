package com.nicholasnixon.dailychore.navigation

import kotlinx.serialization.Serializable

/*
 * One object per screen. These are "type-safe routes": Navigation uses the object itself
 * (not a text name) to know where to go, so a typo is caught by the compiler.
 * @Serializable lets Navigation turn the object into its internal route string.
 * (Pattern from developer.android.com: "Type safety in Kotlin DSL and Navigation Compose".)
 */

@Serializable
object KidRoute

@Serializable
object RewardsRoute

@Serializable
object ParentRoute
