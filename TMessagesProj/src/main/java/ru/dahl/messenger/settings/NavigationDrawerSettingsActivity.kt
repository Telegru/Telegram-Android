package ru.dahl.messenger.settings

import android.view.View
import org.telegram.messenger.LocaleController.getString
import org.telegram.messenger.R
import org.telegram.messenger.UserConfig
import org.telegram.ui.Adapters.DrawerLayoutAdapter.ID_CALLS
import org.telegram.ui.Adapters.DrawerLayoutAdapter.ID_CHANGE_STATUS
import org.telegram.ui.Adapters.DrawerLayoutAdapter.ID_CONTACTS
import org.telegram.ui.Adapters.DrawerLayoutAdapter.ID_INVITE_FRIENDS
import org.telegram.ui.Adapters.DrawerLayoutAdapter.ID_NEW_GROUP
import org.telegram.ui.Adapters.DrawerLayoutAdapter.ID_PROFILE
import org.telegram.ui.Adapters.DrawerLayoutAdapter.ID_PROXY
import org.telegram.ui.Adapters.DrawerLayoutAdapter.ID_SAVED_MESSAGES
import org.telegram.ui.Adapters.DrawerLayoutAdapter.ID_TELEGRAM_FEATURES
import org.telegram.ui.Components.UItem
import org.telegram.ui.Components.UniversalAdapter
import org.telegram.ui.Components.UniversalFragment

class NavigationDrawerSettingsActivity : UniversalFragment() {

    companion object {
        const val WALLET = -1
    }

    override fun getTitle(): CharSequence = getString(R.string.NavigationDrawer)

    override fun fillItems(items: ArrayList<UItem>?, adapter: UniversalAdapter?) {
        val isPremium = UserConfig.getInstance(UserConfig.selectedAccount)?.isPremium == true
        DahlSettings.navigationDrawerItems.apply {
            items?.add(UItem.asHeader(getString(R.string.NavigationDrawerItems)))
            items?.add(UItem.asIconCheck(ID_PROXY, R.drawable.shield_keyhole_outline_28, getString(R.string.ProxyDahl)).setChecked(proxy))
            items?.add(UItem.asIconCheck(ID_PROFILE, R.drawable.left_status_profile, getString(R.string.MyProfile)).setChecked(profile))
            if(isPremium) {
                items?.add(UItem.asIconCheck(ID_CHANGE_STATUS, R.drawable.msg_status_edit, getString(R.string.ChangeEmojiStatus)).setChecked(changeStatus))
            }
            items?.add(UItem.asIconCheck(WALLET, R.drawable.wallet_outline_28, getString(R.string.Wallet)).setChecked(wallet))
            items?.add(UItem.asIconCheck(ID_NEW_GROUP, R.drawable.msg_groups, getString(R.string.NewGroup)).setChecked(newGroup))
            items?.add(UItem.asIconCheck(ID_CONTACTS, R.drawable.msg_contacts, getString(R.string.Contacts)).setChecked(contacts))
            items?.add(UItem.asIconCheck(ID_CALLS, R.drawable.msg_calls, getString(R.string.Calls)).setChecked(calls))
            items?.add(UItem.asIconCheck(ID_SAVED_MESSAGES, R.drawable.msg_saved, getString(R.string.SavedMessages)).setChecked(savedMessages))
            items?.add(UItem.asIconCheck(ID_INVITE_FRIENDS, R.drawable.msg_invite, getString(R.string.InviteFriends)).setChecked(inviteFriends))
            items?.add(UItem.asIconCheck(ID_TELEGRAM_FEATURES, R.drawable.msg_help, getString(R.string.TelegramFeatures)).setChecked(telegramFeatures))
            items?.add(UItem.asShadow(getString(R.string.NavigationDrawerSettingsInfo)))
        }
    }

    override fun onClick(item: UItem?, view: View?, position: Int, x: Float, y: Float) {
        DahlSettings.navigationDrawerItems.let {
            when (item?.id) {
                ID_PROXY -> DahlSettings.navigationDrawerItems = it.copy(proxy = !it.proxy)
                ID_PROFILE -> DahlSettings.navigationDrawerItems = it.copy(profile = !it.profile)
                ID_CHANGE_STATUS -> DahlSettings.navigationDrawerItems = it.copy(changeStatus = !it.changeStatus)
                ID_NEW_GROUP -> DahlSettings.navigationDrawerItems = it.copy(newGroup = !it.newGroup)
                ID_CONTACTS -> DahlSettings.navigationDrawerItems = it.copy(contacts = !it.contacts)
                ID_CALLS -> DahlSettings.navigationDrawerItems = it.copy(calls = !it.calls)
                ID_SAVED_MESSAGES -> DahlSettings.navigationDrawerItems = it.copy(savedMessages = !it.savedMessages)
                ID_INVITE_FRIENDS -> DahlSettings.navigationDrawerItems = it.copy(inviteFriends = !it.inviteFriends)
                ID_TELEGRAM_FEATURES -> DahlSettings.navigationDrawerItems = it.copy(telegramFeatures = !it.telegramFeatures)
                WALLET -> DahlSettings.navigationDrawerItems = it.copy(wallet = !it.wallet)
                else -> {}
            }
        }
        listView.adapter.update(true)
    }

    override fun onLongClick(item: UItem?, view: View?, position: Int, x: Float, y: Float): Boolean = false
}