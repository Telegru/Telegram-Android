package ru.dahl.messenger.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.telegram.messenger.AccountInstance
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.DialogObject
import org.telegram.messenger.ImageReceiver
import org.telegram.messenger.LocaleController
import org.telegram.messenger.MessagesController
import org.telegram.messenger.NotificationCenter
import org.telegram.messenger.R
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.TLRPC
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Cells.BaseCell
import org.telegram.ui.Components.AvatarDrawable
import org.telegram.ui.Components.ButtonBounce
import org.telegram.ui.Components.LayoutHelper
import org.telegram.ui.Components.RecyclerListView
import org.telegram.ui.Components.VectorAvatarThumbDrawable
import ru.dahl.messenger.settings.DahlSettings
import ru.dahl.messenger.settings.DahlSettingsKeys
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max

@SuppressLint("ViewConstructor")
class RecentChatsPanel(
    context: Context,
    delegate: RecentChatCell.Delegate
) : FrameLayout(context), OnSharedPreferenceChangeListener, NotificationCenter.NotificationCenterDelegate {

    companion object {
        const val HEIGHT_IN_DP = 56f
    }

    private val topDivider = View(context)
    private val bottomDivider = View(context)
    private val emptyView: AppCompatTextView = AppCompatTextView(context)
    val listView: RecyclerListView
    private val adapter: RecentChatAdapter

    init {
        setBackgroundColor(Theme.getColor(if(Theme.isCurrentThemeDark()) Theme.key_actionBarDefault else Theme.key_windowBackgroundGray))

        emptyView.text = LocaleController.getString(R.string.EmptyRecentChats)
        emptyView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText))
        emptyView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        val margin = AndroidUtilities.dp(16f)
        emptyView.setPadding(margin, 0, margin, 0)
        addView(emptyView, LayoutHelper.createFrame(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER))

        adapter = RecentChatAdapter(UserConfig.selectedAccount, delegate)
        adapter.setHasStableIds(true)
        listView = RecyclerListView(context)
        listView.setHasFixedSize(true)
        listView.itemAnimator = null
        listView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, LocaleController.isRTL)
        listView.adapter = adapter

        addView(listView, LayoutHelper.createFrame(ViewGroup.LayoutParams.MATCH_PARENT, 48f, Gravity.START, 4f, 4f, 4f, 4f))

        topDivider.setBackgroundColor(Theme.getColor(Theme.key_divider))
        addView(topDivider, LayoutHelper.createFrame(ViewGroup.LayoutParams.MATCH_PARENT.toFloat(), 0.5f, Gravity.TOP))
        bottomDivider.setBackgroundColor(Theme.getColor(Theme.key_divider))
        addView(bottomDivider, LayoutHelper.createFrame(ViewGroup.LayoutParams.MATCH_PARENT.toFloat(), 0.5f, Gravity.BOTTOM))
    }

    fun onResume() {
        DahlSettings.addListener(this)
        AccountInstance.getInstance(UserConfig.selectedAccount).notificationCenter.apply {
            addObserver(this@RecentChatsPanel, NotificationCenter.dialogsNeedReload)
            addObserver(this@RecentChatsPanel, NotificationCenter.dialogsUnreadCounterChanged)
        }
        reloadData(false)
    }

    fun onPause() {
        DahlSettings.removeListener(this)
        AccountInstance.getInstance(UserConfig.selectedAccount).notificationCenter.apply {
            removeObserver(this@RecentChatsPanel, NotificationCenter.dialogsNeedReload)
            removeObserver(this@RecentChatsPanel, NotificationCenter.dialogsUnreadCounterChanged)
        }
    }

    private fun reloadData(scrollToFirst: Boolean) {
        val ids = DahlSettings
            .getRecentChats(UserConfig.selectedAccount)
            .reversed()

        adapter.update(ids)
        if (scrollToFirst && adapter.itemCount > 0) {
            listView.scrollToPosition(0)
        }
        emptyView.visibility = if (adapter.itemCount == 0) VISIBLE else GONE
    }

    private fun reload(){
        adapter.reload()
        emptyView.visibility = if (adapter.itemCount == 0) VISIBLE else GONE
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateColors(){
        setBackgroundColor(Theme.getColor(if(Theme.isCurrentThemeDark()) Theme.key_actionBarDefault else Theme.key_windowBackgroundGray))
        topDivider.setBackgroundColor(Theme.getColor(Theme.key_divider))
        bottomDivider.setBackgroundColor(Theme.getColor(Theme.key_divider))
        adapter.notifyDataSetChanged()
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String?) {
        if(key == DahlSettingsKeys.recentChatsKey(UserConfig.selectedAccount)){
            reloadData(scrollToFirst = true)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun didReceivedNotification(id: Int, account: Int, vararg args: Any?) {
        when(id){
            NotificationCenter.dialogsNeedReload -> {
                reload()
            }
            NotificationCenter.dialogsUnreadCounterChanged -> {
                reload()
            }
        }
    }
}

internal class RecentChatAdapter(private val currentAccount: Int, private val delegate: RecentChatCell.Delegate) :
    RecyclerListView.SelectionAdapter() {

    private val dialogsIds = mutableListOf<Long>()
    private val dialogs = mutableListOf<TLRPC.Dialog>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val cell = RecentChatCell(parent.context, currentAccount, delegate)
        return RecyclerListView.Holder(cell)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val dialog = dialogs[position]
        val cell = holder.itemView as RecentChatCell
        cell.bind(dialog)
    }

    override fun getItemCount(): Int = dialogs.size

    override fun isEnabled(holder: RecyclerView.ViewHolder?): Boolean = true

    override fun getItemId(position: Int): Long = dialogs[position].id

    @SuppressLint("NotifyDataSetChanged")
    fun update(dialogIds: Collection<Long>) {
        val messagesController = MessagesController.getInstance(currentAccount)
        this.dialogsIds.clear()
        this.dialogsIds.addAll(dialogIds)
        val dialogs = dialogIds.mapNotNull { messagesController.getDialog(it) }
        this.dialogs.clear()
        this.dialogs.addAll(dialogs)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun reload() {
        val messagesController = MessagesController.getInstance(currentAccount)
        val dialogs = dialogsIds.mapNotNull { messagesController.getDialog(it) }
        this.dialogs.clear()
        this.dialogs.addAll(dialogs)
        notifyDataSetChanged()
    }
}

@SuppressLint("ViewConstructor")
class RecentChatCell(context: Context, private val currentAccount: Int, private val delegate: Delegate) : BaseCell(context) {

    companion object {

        private val counterTextPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = AndroidUtilities.dpf2(12f)
            typeface = AndroidUtilities.bold()
        }

        private val counterPaintOutline = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = AndroidUtilities.dp(1.5f).toFloat()
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
    }

    private var dialog: TLRPC.Dialog? = null
    private val avatarImage = ImageReceiver(this)
    private val avatarDrawable = AvatarDrawable()
    private var countLayout: StaticLayout? = null

    private var longPressRunnable: Runnable? = null
    private var buttonBounce: ButtonBounce? = null
    private var pressed = false
    private var startX = 0f
    private var startY = 0f


    private val counterRect = RectF()
    private var countLeft = 0f
    private var countTop = 0f

    private var isMuted = true

    init {
        avatarImage.setAllowLoadingOnAttachedOnly(true)
        val avatarSize = AndroidUtilities.dp(32f)
        val avatarLeft = AndroidUtilities.dp(12f)
        val avatarTop = AndroidUtilities.dp(8f)
        avatarImage.setImageCoords(
            RectF(
                avatarLeft.toFloat(),
                avatarTop.toFloat(),
                (avatarLeft + avatarSize).toFloat(),
                (avatarTop + avatarSize).toFloat()
            )
        )
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        avatarImage.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        avatarImage.onDetachedFromWindow()
    }

    override fun onLayout(p0: Boolean, p1: Int, p2: Int, p3: Int, p4: Int) {

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(AndroidUtilities.dp(56f), AndroidUtilities.dp(48f))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        avatarImage.setRoundRadius(AndroidUtilities.dp(if (DahlSettings.rectangularAvatars) 3f else 16f))
        avatarImage.draw(canvas)

        countLayout?.let { layout ->
            val paint = if(isMuted) Theme.dialogs_countGrayPaint else Theme.dialogs_countPaint
            val corner = if (DahlSettings.rectangularAvatars) AndroidUtilities.dpf2(4f) else AndroidUtilities.dpf2(10f)
            canvas.drawRoundRect(counterRect, corner, corner, paint)

            val outlineColor = if (Theme.isCurrentThemeDark()) Theme.getColor(Theme.key_actionBarDefault) else Theme.getColor(Theme.key_windowBackgroundGray)
            counterPaintOutline.color = outlineColor
            canvas.drawRoundRect(counterRect, corner, corner, counterPaintOutline)

            val textColor = Theme.getColor(Theme.key_chats_unreadCounterText)
            counterTextPaint.color = textColor
            canvas.save()
            canvas.translate(countLeft, countTop)
            layout.draw(canvas)
            canvas.restore()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val dialogId = dialog?.id ?: return false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (buttonBounce == null) {
                    buttonBounce = ButtonBounce(this, 1.5f, 5f)
                }
                buttonBounce!!.isPressed = true
                pressed = true
                startX = event.x
                startY = event.y
                if (longPressRunnable != null) {
                    AndroidUtilities.cancelRunOnUIThread(longPressRunnable)
                }
                AndroidUtilities.runOnUIThread(Runnable {
                    try {
                        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    } catch (ignored: Exception) {
                    }

                    buttonBounce?.isPressed = false
                    pressed = false
                    delegate.showContextMenu(dialogId)
                }.also { longPressRunnable = it }, ViewConfiguration.getLongPressTimeout().toLong())
            }

            MotionEvent.ACTION_MOVE -> {
                if (abs((startX - event.x).toDouble()) > AndroidUtilities.touchSlop || abs((startY - event.y).toDouble()) > AndroidUtilities.touchSlop) {
                    buttonBounce?.isPressed = false

                    if (longPressRunnable != null) {
                        AndroidUtilities.cancelRunOnUIThread(longPressRunnable)
                    }
                    pressed = false
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                buttonBounce?.isPressed = false

                if (pressed && event.action == MotionEvent.ACTION_UP) {
                    delegate.openChat(dialogId)
                }
                pressed = false
                if (longPressRunnable != null) {
                    AndroidUtilities.cancelRunOnUIThread(longPressRunnable)
                }
            }
        }
        return pressed
    }

    fun bind(dialog: TLRPC.Dialog) {
        val messagesController = MessagesController.getInstance(currentAccount)
        this.dialog = dialog
        var user: TLRPC.User? = null
        var chat: TLRPC.Chat? = null
        if (dialog.id != 0L) {
            if (DialogObject.isEncryptedDialog(dialog.id)) {
                messagesController.getEncryptedChat(DialogObject.getEncryptedChatId(dialog.id))?.let { encryptedChat ->
                    user = messagesController.getUser(encryptedChat.user_id)
                }
            } else if (DialogObject.isUserDialog(dialog.id)) {
                user = messagesController.getUser(dialog.id)
            } else {
                chat = messagesController.getChat(-dialog.id)
            }
        }
        if (user != null) {
            avatarDrawable.setInfo(currentAccount, user)
            avatarImage.setForUserOrChat(user, avatarDrawable, null, true, VectorAvatarThumbDrawable.TYPE_SMALL, false)
        } else if (chat != null) {
            avatarDrawable.setInfo(currentAccount, chat)
            avatarImage.setForUserOrChat(chat, avatarDrawable)
        }

        val count = messagesController.getDialogUnreadCount(dialog)
        val countString = if (count > 0) {
           "$count"
        } else {
            null
        }
        if (countString != null) {
            val countWidth = max(
                AndroidUtilities.dpf2(12f),
                (ceil(counterTextPaint.measureText(countString)))
            )
            @Suppress("DEPRECATION")
            countLayout =
                StaticLayout(countString, counterTextPaint, countWidth.toInt(), Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false)

            countLeft = if (!LocaleController.isRTL) {
                measuredWidth - countWidth - AndroidUtilities.dp(9f)
            } else {
                AndroidUtilities.dpf2(9f)
            }
            val x = countLeft - AndroidUtilities.dpf2(4f)
            val y = AndroidUtilities.dpf2(26f)
            counterRect.set(x, y, x + countWidth + AndroidUtilities.dp(8f), y + AndroidUtilities.dp(20f))
            countTop = y + AndroidUtilities.dp(3f)
        } else {
            countLayout = null
        }
        isMuted = messagesController.isDialogMuted(dialog.id, 0)
        invalidate()
    }

    interface Delegate {
        fun openChat(dialogId: Long)
        fun showContextMenu(dialogId: Long)
    }
}
