package ru.dahl.messenger.settings

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.text.Html
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.util.forEach
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.telegram.messenger.AndroidUtilities.dp
import org.telegram.messenger.LocaleController.formatString
import org.telegram.messenger.LocaleController.getString
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Components.LayoutHelper
import org.telegram.ui.Components.UItem
import org.telegram.ui.Components.UniversalAdapter
import org.telegram.ui.Components.UniversalFragment
import org.telegram.ui.LaunchActivity
import org.telegram.ui.WallpapersListActivity
import ru.dahl.messenger.icons.VKUiIconReplacement
import ru.dahl.messenger.settings.DahlSettings.ICON_REPLACEMENT_VKUI
import ru.dahl.messenger.settings.DahlSettings.NO_REPLACEMENT
import ru.dahl.messenger.ui.cells.DahlChatListCell

class AppearanceSettingsActivity : UniversalFragment() {

    companion object {
        const val BOTTOM_PANEL = 1
        const val PERSONAL_COLORS = 2
        const val WALLPAPERS = 3
        const val SWITCH_ICONS = 4
        const val SQUARE_AVATARS = 5
        const val AVATARS_FONT = 6
        const val CUSTOM_WALLPAPERS = 7
        const val CHAT_LIST_LINES = 8
    }

    private val icons: Set<Int> by lazy {
        val icons = mutableSetOf<Int>()
        val seenValues = mutableSetOf<Int>()
        VKUiIconReplacement().replacements.forEach { key, value ->
            if (key != R.drawable.attach_send && !seenValues.contains(value)) {
                icons.add(key)
                seenValues.add(value)
            }
        }
        return@lazy icons
    }


    override fun getTitle(): CharSequence = getString(R.string.Appearance)

    override fun fillItems(items: ArrayList<UItem>?, adapter: UniversalAdapter?) {

        items?.add(UItem.asHeader(getString(R.string.EnableInChatsAndChannels)))
        items?.add(
            UItem.asCheck(BOTTOM_PANEL, getString(R.string.BottomPanelInChannels)).setChecked(DahlSettings.isShowBottomPanelInChannels)
        )
//        items?.add(UItem.asCheck(PERSONAL_COLORS, getString(R.string.PersonalColors)).setChecked(DahlSettings.isEnabledPersonalColors))
        items?.add(UItem.asCheck(CUSTOM_WALLPAPERS, getString(R.string.CustomWallpapers)).setChecked(DahlSettings.isCustomWallpapersEnabled))
        items?.add(UItem.asShadow(-3, null))

        items?.add(UItem.asHeader(getString(R.string.ChatList)))
        items?.add(UItem.asCustom(
            CHAT_LIST_LINES,
            object : DahlChatListCell(context){
                override fun didSelectChatType(lines: Int) {
                    DahlSettings.chatListLines = lines
                }
            }
        ))
        items?.add(UItem.asShadow(-3, null))

        items?.add(UItem.asHeader(getString(R.string.ChatsSettings)))
        items?.add(UItem.asButton(WALLPAPERS, getString(R.string.Wallpapers)))

        items?.add(UItem.asShadow(-3, null))

//        items?.add(UItem.asHeader(getString(R.string.NavigationDrawer)))
//        items?.add(UItem.asShadow(-3, null))

        items?.add(UItem.asHeader(getString(R.string.ElementsRounding)))
        items?.add(UItem.asCheck(SQUARE_AVATARS, getString(R.string.Squared)).apply {
            setChecked(DahlSettings.rectangularAvatars)
        })

        items?.add(UItem.asShadow(-3, null))

        items?.add(UItem.asHeader(getString(R.string.AvatarsFont)))
        items?.add(UItem.asCheck(AVATARS_FONT, getString(R.string.Nizhegorodski)).apply {
            setChecked(DahlSettings.ngAvatarFont)
        })

        val hexAuthorColor = String.format("#%06X", (0xFFFFFF and Theme.getColor(Theme.key_windowBackgroundWhiteBlackText)))
        items?.add(UItem.asShadow(Html.fromHtml(formatString(R.string.FontAuthor, hexAuthorColor))))

        items?.add(UItem.asShadow(-3, null))

        items?.add(UItem.asHeader(getString(R.string.Icons)))
        items?.add(
            UItem.asCheck(SWITCH_ICONS, getString(R.string.IconsVkUI)).apply {
                hideDivider = true
                setChecked(DahlSettings.iconReplacement == ICON_REPLACEMENT_VKUI)
            })
        items?.add(UItem.asCustom(createIconsView()))
    }

    override fun onClick(item: UItem?, view: View?, position: Int, x: Float, y: Float) {
        when (item?.id) {
            BOTTOM_PANEL -> DahlSettings.isShowBottomPanelInChannels = !DahlSettings.isShowBottomPanelInChannels
            PERSONAL_COLORS -> DahlSettings.isEnabledPersonalColors = !DahlSettings.isEnabledPersonalColors
            WALLPAPERS -> presentFragment(WallpapersListActivity(WallpapersListActivity.TYPE_ALL))
            SWITCH_ICONS -> {
                if (item.checked) {
                    DahlSettings.iconReplacement = NO_REPLACEMENT
                } else {
                    DahlSettings.iconReplacement = ICON_REPLACEMENT_VKUI
                }
                Toast.makeText(context, R.string.reloadToApply, Toast.LENGTH_SHORT).show()
                (context as LaunchActivity).reloadResources()
            }
            SQUARE_AVATARS -> {
                DahlSettings.rectangularAvatars = !DahlSettings.rectangularAvatars
            }
            AVATARS_FONT -> {
                DahlSettings.ngAvatarFont = !DahlSettings.ngAvatarFont
            }
            CUSTOM_WALLPAPERS -> DahlSettings.isCustomWallpapersEnabled = !DahlSettings.isCustomWallpapersEnabled
            else -> {}
        }
        if(item?.id != CHAT_LIST_LINES) {
            listView.adapter.update(true)
        }
    }

    override fun onLongClick(item: UItem?, view: View?, position: Int, x: Float, y: Float): Boolean = false

    private fun createIconsView(): View {
        val listView = RecyclerView(context)
        listView.id = View.generateViewId()
        listView.background = Theme.createRoundRectDrawable(dp(6f), dp(6f), Theme.getColor(Theme.key_dialogBackgroundGray))
        listView.setHasFixedSize(true)
        listView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val iconsAdapter = IconsAdapter()
        listView.adapter = iconsAdapter
        iconsAdapter.updateIcons(icons.toList())

        val container = FrameLayout(context)
        container.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite))
        container.id = View.generateViewId()
        val margin = 21f
        container.addView(listView, LayoutHelper.createFrame(LayoutParams.MATCH_PARENT, 40f, Gravity.CENTER, margin, 8f, margin, 16f))
        return container
    }

}

private class IconsAdapter : RecyclerView.Adapter<IconViewHolder>() {

    private var icons = emptyList<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
        val frameLayout = FrameLayout(parent.context)
        frameLayout.id = View.generateViewId()
        return IconViewHolder(frameLayout)
    }

    override fun getItemCount(): Int = icons.size

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        holder.bind(icons[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateIcons(icons: List<Int>) {
        this.icons = icons
        notifyDataSetChanged()
    }
}

private class IconViewHolder(container: FrameLayout) : RecyclerView.ViewHolder(container) {

    private val iconView: ImageView = ImageView(container.context).apply { id = View.generateViewId() }

    init {
        iconView.imageTintList = ColorStateList.valueOf(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon))
        container.addView(iconView, LayoutHelper.createFrame(16, 16f, Gravity.CENTER, 12f, 12f, 12f, 12f))
    }

    fun bind(@DrawableRes icon: Int) {
        iconView.setImageResource(icon)
    }
}