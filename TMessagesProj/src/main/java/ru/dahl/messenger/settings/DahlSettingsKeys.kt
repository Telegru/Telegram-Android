package ru.dahl.messenger.settings

object DahlSettingsKeys {
    const val iconPackKey = "Icon_Replacements"
    const val avatarsRedesignKey = "Avatars_Redesign"

    const val RECENT_CHATS_ENABLED = "recent_chats_enabled_v2"
    private const val RECENT_CHATS_LIST = "recent_chats_list"

    fun recentChatsKey(currentUser: Int) = "${RECENT_CHATS_LIST}_$currentUser"
}
