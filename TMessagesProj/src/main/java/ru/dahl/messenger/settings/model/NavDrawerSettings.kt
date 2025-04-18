package ru.dahl.messenger.settings.model

import android.content.SharedPreferences
import org.telegram.messenger.LocaleController

data class NavDrawerSettings(
    val proxy: Boolean = true,
    val profile: Boolean = true,
    val changeStatus: Boolean = false,
    val wallet: Boolean = false,
    val newGroup: Boolean = false,
    val contacts: Boolean = true,
    val calls: Boolean = true,
    val savedMessages: Boolean = false,
    val inviteFriends: Boolean = false,
    val telegramFeatures: Boolean = false,
) {

    constructor(sharedPreferences: SharedPreferences) :
            this(
                proxy = sharedPreferences.getBoolean("nav_drawer_proxy", true),
                profile = sharedPreferences.getBoolean("nav_drawer_profile", true),
                changeStatus = sharedPreferences.getBoolean("nav_drawer_change_status", false),
                wallet = sharedPreferences.getBoolean("nav_drawer_wallet", false),
                newGroup = sharedPreferences.getBoolean("nav_drawer_new_group", false),
                contacts = sharedPreferences.getBoolean("nav_drawer_contacts", true),
                calls = sharedPreferences.getBoolean("nav_drawer_calls", true),
                savedMessages = sharedPreferences.getBoolean("nav_drawer_saved_messages", false),
                inviteFriends = sharedPreferences.getBoolean("nav_drawer_invite_friends", false),
                telegramFeatures = sharedPreferences.getBoolean("nav_drawer_telegram_features", false),
            )

    fun save(sharedPreferences: SharedPreferences) {
        sharedPreferences.edit()
            .putBoolean("nav_drawer_proxy", proxy)
            .putBoolean("nav_drawer_profile", profile)
            .putBoolean("nav_drawer_change_status", changeStatus)
            .putBoolean("nav_drawer_wallet", wallet)
            .putBoolean("nav_drawer_new_group", newGroup)
            .putBoolean("nav_drawer_contacts", contacts)
            .putBoolean("nav_drawer_calls", calls)
            .putBoolean("nav_drawer_saved_messages", savedMessages)
            .putBoolean("nav_drawer_invite_friends", inviteFriends)
            .putBoolean("nav_drawer_telegram_features", telegramFeatures)
            .apply()
    }

    fun getInfoText(isPremium: Boolean): String {
        var count = 0
        if (proxy) count++
        if (profile) count++
        if (isPremium && changeStatus) count++
        if (wallet) count++
        if (newGroup) count++
        if (contacts) count++
        if (calls) count++
        if (savedMessages) count++
        if (inviteFriends) count++
        if (telegramFeatures) count++

        return LocaleController.formatPluralString("NavigationDrawerItems", count, count)

    }

}