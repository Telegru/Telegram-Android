package ru.dahl.messenger.settings

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.BuildVars
import org.telegram.messenger.LocaleController.getString
import org.telegram.messenger.MessagesController
import org.telegram.messenger.R
import org.telegram.messenger.Utilities
import org.telegram.messenger.Utilities.Callback5
import org.telegram.messenger.Utilities.Callback5Return
import org.telegram.ui.ActionBar.ActionBar
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.ActionBar.BackDrawable
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Components.LayoutHelper
import org.telegram.ui.Components.SizeNotifierFrameLayout
import org.telegram.ui.Components.UItem
import org.telegram.ui.Components.UniversalRecyclerView
import kotlin.math.absoluteValue
import kotlin.math.max

class DahlSettingsActivity : BaseFragment() {

    companion object {
        const val GENERAL = 1
        const val APPEARANCE = 2
        const val CHATS = 3
        const val SUPPORT = 4

        val EXPANDED_LOGO_SIZE = AndroidUtilities.dp(78f)
        val COLLAPSED_LOGO_SIZE = AndroidUtilities.dp(36f)
    }

    private var header: FrameLayout? = null
    private var logo: AppCompatImageView? = null
    private var title: AppCompatTextView? = null
    private var info: AppCompatTextView? = null
    private var listView: UniversalRecyclerView? = null

    private var headerViewHeight = 0

    private var savedScrollPosition = -1
    private var scrollOffset = 0

    override fun createActionBar(context: Context?): ActionBar {
        val actionBar = super.createActionBar(context)
        actionBar.setAddToContainer(false)
        actionBar.castShadows = false
        actionBar.backButtonDrawable = BackDrawable(false)
        actionBar.setTitleColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        actionBar.setItemsColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText), false)
        actionBar.backgroundColor = Color.TRANSPARENT

        actionBar.setActionBarMenuOnItemClick(object : ActionBarMenuOnItemClick() {
            override fun onItemClick(id: Int) {
                if (id == -1) {
                    finishFragment()
                }
            }
        })
        return actionBar
    }

    override fun createView(context: Context): View {
        val contentView: FrameLayout = object : SizeNotifierFrameLayout(context) {
            override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                super.onMeasure(
                    MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.EXACTLY)
                )
            }
        }
        contentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray))

        listView = object : UniversalRecyclerView(
            this,
            Utilities.Callback2 { items, _ -> fillItems(items) },
            Callback5 { item, _, _, _, _ -> onClick(item) },
            Callback5Return { _, _, _, _, _ -> false }) {
            override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
                super.onLayout(changed, l, t, r, b)
                savedScrollPosition = -1
                scrollOffset = 0
            }
        }

        contentView.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT.toFloat()))
        contentView.addView(createHeader(), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT.toFloat()))
        contentView.addView(actionBar)

        header?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                header?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                headerViewHeight = header?.height ?: 0
                updateViews(0)
                listView?.adapter?.update(false)

                listView?.addOnScrollListener(object : OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        scrollOffset = max(0, scrollOffset + dy)
                        updateViews(scrollOffset)
                    }

                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        if(newState == RecyclerView.SCROLL_STATE_IDLE){
                            scrollOffset = recyclerView.computeVerticalScrollOffset()
                            updateViews(scrollOffset)
                        }
                    }
                })
            }
        })

        return contentView.also { fragmentView = it }
    }

    private fun fillItems(items: ArrayList<UItem>) {
        items.add(UItem.asSpace(headerViewHeight))
        items.add(UItem.asShadow(-3, null))

        items.add(UItem.asHeader(getString(R.string.Categories)))
        items.add(UItem.asButton(GENERAL, R.drawable.msg_media, getString(R.string.General)))
        items.add(UItem.asButton(APPEARANCE, R.drawable.msg_theme, getString(R.string.Appearance)))
        items.add(UItem.asButton(CHATS, R.drawable.msg2_discussion, getString(R.string.ChatsSettings)))

        items.add(UItem.asShadow(-3, null))

        items.add(UItem.asHeader(getString(R.string.Links)))
        items.add(UItem.asButton(SUPPORT, R.drawable.message_heart_outline_28, getString(R.string.Support), "@dahl_help"))
    }

    private fun onClick(item: UItem) {
        when (item.id) {
            GENERAL -> presentFragment(GeneralSettingsActivity())
            APPEARANCE -> presentFragment(AppearanceSettingsActivity())
            CHATS -> presentFragment(ChatsSettingsActivity())
            SUPPORT -> showSupportAlert()
            else -> {}
        }
    }

    private fun showSupportAlert() {
        val dialog = AlertDialog.Builder(parentActivity)
            .setTitle(getString(R.string.ContactSupport))
            .setMessage(getString(R.string.DahlSupportInfo))
            .setNegativeButton(getString(R.string.Cancel), null)
            .setPositiveButton(getString(R.string.SendMessage)) { _, _ ->
                MessagesController.getInstance(currentAccount).openByUserName(("dahl_help"), this, 1)
            }
            .create()

        showDialog(dialog)
    }

    private fun createHeader(): View {
        val header = FrameLayout(context).also { this.header = it }
        val logo = AppCompatImageView(context).also { this.logo = it }
        val title = AppCompatTextView(context).also { this.title = it }
        val info = AppCompatTextView(context).also { this.info = it }

        val actionBarHeight = (AndroidUtilities.statusBarHeight + ActionBar.getCurrentActionBarHeight()).toFloat()
        val logoSize = EXPANDED_LOGO_SIZE

        logo.scaleType = ImageView.ScaleType.FIT_XY
        val logoDrawable: Drawable? = if (DahlSettings.rectangularAvatars)
            ContextCompat.getDrawable(context, R.drawable.logo_dahl_78)?.mutate() else ContextCompat.getDrawable(context, R.drawable.dahl_rounded_logo)?.mutate()
        logo.setImageDrawable(logoDrawable)
        val logoLayoutParams = FrameLayout.LayoutParams(logoSize, logoSize)
        logoLayoutParams.topMargin = actionBarHeight.toInt()
        logoLayoutParams.gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
        header.addView(logo, logoLayoutParams)

        val textLayout = LinearLayout(context)
        textLayout.orientation = LinearLayout.VERTICAL
        val textLayoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        textLayoutParams.topMargin = actionBarHeight.toInt() + (logoSize + AndroidUtilities.dp(10f))
        textLayoutParams.gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
        header.addView(textLayout, textLayoutParams)

        title.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText))
        title.typeface = AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM)
        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20f)
        title.text = String.format("%s %s", getString(R.string.DahlAppName), BuildVars.BUILD_VERSION_STRING)
        title.setLines(1)
        title.maxLines = 1
        title.isSingleLine = true
        title.setPadding(0, 0, 0, 0)
        title.gravity = Gravity.CENTER
        val titleLayoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        titleLayoutParams.gravity = Gravity.CENTER_HORIZONTAL
        titleLayoutParams.marginStart = AndroidUtilities.dp(21f)
        titleLayoutParams.marginEnd = AndroidUtilities.dp(21f)
        textLayout.addView(title, titleLayoutParams)

        info.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText))
        info.typeface = AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM)
        info.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
        info.text = AndroidUtilities.replaceTags(getString(R.string.Slide1Message))
        info.setLines(2)
        info.maxLines = 2
        info.setPadding(0, 0, 0, AndroidUtilities.dp(21f))
        info.gravity = Gravity.CENTER
        val infoLayoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        infoLayoutParams.marginStart = AndroidUtilities.dp(21f)
        infoLayoutParams.marginEnd = AndroidUtilities.dp(21f)
        textLayout.addView(info, infoLayoutParams)

        header.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite))
        return header
    }


    private fun updateViews(offset: Int) {
        val logo = this.logo ?: return
        val header = this.header ?: return
        val title = this.title ?: return
        val maxOffset = ActionBar.getCurrentActionBarHeight() + AndroidUtilities.statusBarHeight - headerViewHeight
        val translationY = max(-offset, maxOffset)
        header.translationY = translationY.toFloat()
        val progress = (translationY / maxOffset.toFloat()).absoluteValue
        val logoSize = EXPANDED_LOGO_SIZE - (EXPANDED_LOGO_SIZE - COLLAPSED_LOGO_SIZE) * progress
        logo.layoutParams.also {
            it.height = logoSize.toInt()
            it.width = logoSize.toInt()
        }
        logo.requestLayout()
        logo.translationX = (-(fragmentView.width - logoSize) / 2 + AndroidUtilities.dp(72f)) * progress
        logo.translationY = -translationY.toFloat() - (ActionBar.getCurrentActionBarHeight() + logoSize) / 2 * progress
        title.translationX = (-(fragmentView.width - title.width) / 2 + AndroidUtilities.dp(82f) + logoSize) * progress
        title.translationY = -translationY.toFloat() - (ActionBar.getCurrentActionBarHeight() + EXPANDED_LOGO_SIZE / 2f + title.height + AndroidUtilities.dp(10f)) * progress
        info?.scaleX = 1f - progress
        info?.scaleY = 1f - progress
        info?.alpha = 1f - progress
        info?.visibility = if(progress < 0.5f) View.VISIBLE else View.INVISIBLE
    }

    override fun onResume() {
        super.onResume()
        if (Theme.isCurrentThemeDay()) WindowCompat.getInsetsController(parentActivity.window, this.fragmentView).isAppearanceLightStatusBars = true
    }

    override fun onPause() {
        super.onPause()
        if (Theme.isCurrentThemeDay()) WindowCompat.getInsetsController(parentActivity.window, this.fragmentView).isAppearanceLightStatusBars = false
    }
}
