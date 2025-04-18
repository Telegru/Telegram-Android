package ru.dahl.messenger.settings

import android.view.View
import android.widget.LinearLayout
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.LocaleController.getString
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Cells.RadioColorCell
import org.telegram.ui.Components.UItem
import org.telegram.ui.Components.UniversalAdapter
import org.telegram.ui.Components.UniversalFragment
import ru.dahl.messenger.settings.model.CameraType

class ChatsSettingsActivity : UniversalFragment() {

    companion object {
        const val SWITCH_FOLDERS_AT_BOTTOM = 1
        const val SWITCH_HIDE_ALL_CHATS_FOLDER = 2
        const val SWITCH_FOLDERS_INFINITE_SCROLL = 3
        const val SWITCH_SHOW_FOLDERS_TABS = 4

        const val SWITCH_CONFIRM_CALL = 5
        const val SWITCH_CONFIRM_AUDIO_MESSAGE = 6
        const val SELECTOR_VIDEO_CAMERA = 7

        const val SWITCH_RECENT_CHATS = 8
        const val SWITCH_KEYBOARD_HIDING = 9
    }

    override fun getTitle(): CharSequence {
        return getString(R.string.ChatsSettings)
    }

    override fun fillItems(items: ArrayList<UItem>?, adapter: UniversalAdapter?) {
        items?.add(UItem.asHeader(getString(R.string.NeedConfirm)))
        items?.add(UItem.asCheck(SWITCH_CONFIRM_CALL, getString(R.string.ConfirmCalling)).setChecked(DahlSettings.isConfirmCall))
        items?.add(UItem.asCheck(SWITCH_CONFIRM_AUDIO_MESSAGE, getString(R.string.ConfirmAudioMessage)).setChecked(DahlSettings.isConfirmAudioMessage))
        items?.add(
            UItem.asButton(
                SELECTOR_VIDEO_CAMERA,
                getString(R.string.ConfirmVideoMessageCamera),
                getString(DahlSettings.videoMessageCamera.titleShort)
            )
        )

        items?.add(UItem.asShadow(-3, null))
        items?.add(UItem.asHeader(getString(R.string.RecentChats)))
        items?.add(UItem.asCheck(SWITCH_RECENT_CHATS, getString(R.string.EnablePanel)).setChecked(DahlSettings.isRecentChatsEnabled))
        items?.add(UItem.asShadow(getString(R.string.RecentChatsInfo)))

        items?.add(UItem.asShadow(-3, null))

        items?.add(UItem.asHeader(getString(R.string.ChatsFolders)))
        items?.add(UItem.asCheck(SWITCH_FOLDERS_AT_BOTTOM, getString(R.string.FoldersAtBottom)).setChecked(DahlSettings.isFoldersTabsAtBottom))
        items?.add(UItem.asCheck(SWITCH_FOLDERS_INFINITE_SCROLL, getString(R.string.FoldersInfiniteScrolling)).setChecked(DahlSettings.isFoldersTabInfiniteScroll))
        items?.add(UItem.asCheck(SWITCH_SHOW_FOLDERS_TABS, getString(R.string.ShowAllFolders)).setChecked(!DahlSettings.isHiddenFoldersTabs))
        items?.add(UItem.asCheck(SWITCH_HIDE_ALL_CHATS_FOLDER, getString(R.string.HideAllChatsFolder)).setChecked(DahlSettings.isHiddenAllChatsFolder))

        items?.add(UItem.asShadow(-3, null))

        items?.add(UItem.asHeader(getString(R.string.Keyboard)))
        items?.add(UItem.asCheck(SWITCH_KEYBOARD_HIDING, getString(R.string.HideOnScroll)).setChecked(DahlSettings.isKeyboardHidingEnabled))
        items?.add(UItem.asShadow(getString(R.string.HideOnScrollInfo)))
    }

    override fun onClick(item: UItem?, view: View?, position: Int, x: Float, y: Float) {
        when (item?.id) {
            SWITCH_CONFIRM_CALL -> DahlSettings.isConfirmCall = !DahlSettings.isConfirmCall
            SWITCH_CONFIRM_AUDIO_MESSAGE -> DahlSettings.isConfirmAudioMessage = !DahlSettings.isConfirmAudioMessage
            SELECTOR_VIDEO_CAMERA -> showCameraSelector()

            SWITCH_RECENT_CHATS -> DahlSettings.isRecentChatsEnabled = !DahlSettings.isRecentChatsEnabled

            SWITCH_FOLDERS_AT_BOTTOM -> DahlSettings.isFoldersTabsAtBottom = !DahlSettings.isFoldersTabsAtBottom
            SWITCH_HIDE_ALL_CHATS_FOLDER -> DahlSettings.isHiddenAllChatsFolder = !DahlSettings.isHiddenAllChatsFolder
            SWITCH_FOLDERS_INFINITE_SCROLL -> DahlSettings.isFoldersTabInfiniteScroll = !DahlSettings.isFoldersTabInfiniteScroll
            SWITCH_SHOW_FOLDERS_TABS -> DahlSettings.isHiddenFoldersTabs = !DahlSettings.isHiddenFoldersTabs
            SWITCH_KEYBOARD_HIDING -> DahlSettings.isKeyboardHidingEnabled = !DahlSettings.isKeyboardHidingEnabled
            else -> {}
        }
        listView.adapter.update(true)
    }

    override fun onLongClick(item: UItem?, view: View?, position: Int, x: Float, y: Float): Boolean {
        return false
    }

    private fun showCameraSelector() {
        val selected = DahlSettings.videoMessageCamera.ordinal
        val items = CameraType.entries.map { if(it == CameraType.ALWAYS_ASK) getString(it.titleLong) else getString(it.titleShort) }.toTypedArray()
        val linearLayout = LinearLayout(parentActivity)
        linearLayout.orientation = LinearLayout.VERTICAL

        val builder = AlertDialog.Builder(parentActivity)
            .setTitle(getString(R.string.ConfirmVideoMessageCamera))
            .setView(linearLayout)
            .setNegativeButton(getString(R.string.Cancel), null)

        for (a in items.indices) {
            val cell = RadioColorCell(parentActivity)
            cell.setPadding(AndroidUtilities.dp(4f), 0, AndroidUtilities.dp(4f), 0)
            cell.tag = a
            cell.setCheckColor(Theme.getColor(Theme.key_radioBackground), Theme.getColor(Theme.key_dialogRadioBackgroundChecked))
            cell.setTextAndValue(items[a], selected == a)
            linearLayout.addView(cell)
            cell.setOnClickListener { v->
                builder.dismissRunnable.run()
                val which = v.tag as Int
                DahlSettings.videoMessageCamera = CameraType.entries.toTypedArray()[which]
                listView.adapter.update(true)
            }
        }
        showDialog(builder.create())
    }

}