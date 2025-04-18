package ru.dahl.messenger.settings

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.LocaleController
import org.telegram.messenger.LocaleController.getString
import org.telegram.messenger.MessagesController
import org.telegram.messenger.NotificationCenter
import org.telegram.messenger.R
import org.telegram.messenger.SharedConfig
import org.telegram.messenger.SharedConfig.ProxyInfo
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.ConnectionsManager
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Cells.TextDetailSettingsCell
import org.telegram.ui.Components.CheckBox2
import org.telegram.ui.Components.LayoutHelper
import org.telegram.ui.Components.UItem
import org.telegram.ui.Components.UniversalAdapter
import org.telegram.ui.Components.UniversalFragment
import org.telegram.ui.ProxySettingsActivity
import ru.dahl.messenger.Extra

class GeneralSettingsActivity : UniversalFragment(), NotificationCenter.NotificationCenterDelegate {

    companion object {
        const val PROXY = 1
        const val HIDE_HELP = 2
        const val NAVIGATION_DRAWER = 3
        const val HIDE_PHONE_NUMBER = 4
        const val MESSAGE_READ_STATUS = 5
        const val OFFLINE_MODE = 6
        const val PREMIUM = 7
        const val DAHL_PROXY = 8
        const val CUSTOM_PROXY = 99
    }

    private var currentConnectionState = -1
    private var useProxySettings = false

    private val proxyList = mutableListOf<ProxyInfo>()
    private var dahlProxy = ProxyInfo(Extra.PROXY_ADDRESS, Extra.PROXY_PORT, null, null, Extra.PROXY_SECRET)

    override fun onFragmentCreate(): Boolean {
        SharedConfig.loadProxyList()
        currentConnectionState = ConnectionsManager.getInstance(currentAccount).connectionState

        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.proxyChangedByRotation)
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.proxySettingsChanged)
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.proxyCheckDone)
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.didUpdateConnectionState)

        useProxySettings = MessagesController.getGlobalMainSettings()
            .getBoolean("proxy_enabled", false) && SharedConfig.proxyList.isNotEmpty() && !DahlSettings.isProxyEnabled

        checkProxyList()
        return true
    }

    override fun onFragmentDestroy() {
        super.onFragmentDestroy()
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.proxyChangedByRotation)
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.proxySettingsChanged)
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.proxyCheckDone)
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.didUpdateConnectionState)
    }

    override fun getTitle(): CharSequence = getString(R.string.General)

    override fun fillItems(items: ArrayList<UItem>?, adapter: UniversalAdapter?) {

        items?.add(UItem.asHeader(getString(R.string.ConnectionType)))
        items?.add(UItem.asCheck(PROXY, getString(R.string.ProxyDahl)).setChecked(DahlSettings.isProxyEnabled))
        items?.add(UItem.asShadow(getString(R.string.ProxyDahlInfo)))

        items?.add(UItem.asHeader(getString(R.string.ProxyConnections)))
        items?.add(UItem.asCustom(DAHL_PROXY, TextDetailProxyCell(context).also {
            it.isDahl = true
            it.setProxy(dahlProxy)
            it.setChecked(dahlProxy.link == SharedConfig.currentProxy?.link)
            it.updateStatus(useProxySettings, currentConnectionState)
        }))

        proxyList.clear()
        proxyList.addAll(SharedConfig.proxyList.filter { it.link != dahlProxy.link })
        for (i in 0 until proxyList.size) {
            items?.add(UItem.asCustom(CUSTOM_PROXY + i, TextDetailProxyCell(context).also {
                val info = proxyList[i]
                it.isDahl = false
                it.setProxy(info)
                it.updateStatus(useProxySettings, currentConnectionState)
                it.setChecked(info == SharedConfig.currentProxy)
                it.showDetails = { i -> presentFragment(ProxySettingsActivity(i)) }
            }))
        }

        items?.add(UItem.asShadow(-3, null))

        items?.add(UItem.asHeader(getString(R.string.TgSettingsMenu)))
        items?.add(UItem.asCheck(HIDE_HELP, getString(R.string.HideHelpBlock)).setChecked(DahlSettings.isHiddenHelpBlock))

        items?.add(UItem.asShadow(-3, null))

        items?.add(UItem.asHeader(getString(R.string.NavigationDrawer)))
        val isPremium = UserConfig.getInstance(UserConfig.selectedAccount)?.isPremium == true
        val settingsCell = TextDetailSettingsCell(context).apply {
            setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite))
            setMultilineDetail(true)
            setTextAndValue(getString(R.string.NavigationDrawerItems), DahlSettings.navigationDrawerItems.getInfoText(isPremium), false)
        }
        items?.add(UItem.asCustom(NAVIGATION_DRAWER, settingsCell))
//        items?.add(UItem.asCheck(HIDE_PHONE_NUMBER, getString(R.string.HidePhoneNumber)).setChecked(DahlSettings.isHidePhoneNumber))

        items?.add(UItem.asShadow(-3, null))

//
//        items?.add(UItem.asHeader(getString(R.string.Privacy)))
//        items?.add(
//            UItem.asCustom(
//                MESSAGE_READ_STATUS,
//                TextCheckCell(context).apply {
//                    setTextAndValueAndCheck(
//                        getString(R.string.MessagesStatus),
//                        getString(R.string.MessagesStatusInfo),
//                        DahlSettings.hideMessageReadStatus,
//                        true,
//                        true
//                    )
//                    setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite))
//                }
//            )
//        )
//        items?.add(
//            UItem.asCustom(
//                OFFLINE_MODE,
//                TextCheckCell(context).apply {
//                    setTextAndValueAndCheck(
//                        getString(R.string.OfflineMode),
//                        getString(R.string.OfflineModeInfo),
//                        DahlSettings.isOffline,
//                        true,
//                        false
//                    )
//                    setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite))
//                }
//            )
//        )
//
//        items?.add(UItem.asShadow(-3, null))

        items?.add(UItem.asButton(PREMIUM, getString(R.string.PremiumFeatures)))
        items?.add(UItem.asShadow(getString(R.string.PremiumFeaturesInfo)))
    }

    override fun onClick(item: UItem, view: View?, position: Int, x: Float, y: Float) {
        when (item.id) {
            PROXY -> {
                DahlSettings.isProxyEnabled = !DahlSettings.isProxyEnabled
                useProxySettings = false
                checkProxyList()
            }

            HIDE_PHONE_NUMBER -> DahlSettings.isHidePhoneNumber = !DahlSettings.isHidePhoneNumber
            MESSAGE_READ_STATUS -> DahlSettings.hideMessageReadStatus = !DahlSettings.hideMessageReadStatus
            OFFLINE_MODE -> DahlSettings.isOffline = !DahlSettings.isOffline
            PREMIUM -> presentFragment(PremiumSettingsActivity())
            DAHL_PROXY -> {
                if (!DahlSettings.isProxyEnabled) {
                    DahlSettings.isProxyEnabled = true
                    useProxySettings = false
                    checkProxyList()
                } else {
                    return
                }
            }
            HIDE_HELP -> DahlSettings.isHiddenHelpBlock = !DahlSettings.isHiddenHelpBlock
            NAVIGATION_DRAWER -> presentFragment(NavigationDrawerSettingsActivity())

            else -> {}
        }
        if (item.id >= CUSTOM_PROXY) {
            val i = item.id - CUSTOM_PROXY
            val info = proxyList[i]
            useProxySettings = true
            MessagesController.getGlobalMainSettings().edit()
                .putString("proxy_ip", info.address)
                .putString("proxy_pass", info.password)
                .putString("proxy_user", info.username)
                .putInt("proxy_port", info.port)
                .putString("proxy_secret", info.secret)
                .putBoolean("proxy_enabled", useProxySettings)
                .apply()

            SharedConfig.currentProxy = info
            ConnectionsManager.setProxySettings(
                useProxySettings,
                info.address,
                info.port,
                info.username,
                info.password,
                info.secret
            )
            checkProxyList()
        }
        listView.adapter.update(true)
    }

    override fun onLongClick(item: UItem?, view: View?, position: Int, x: Float, y: Float): Boolean = false


    override fun didReceivedNotification(id: Int, account: Int, vararg args: Any?) {
        if (id == NotificationCenter.didUpdateConnectionState) {
            val state = ConnectionsManager.getInstance(account).connectionState
            if (currentConnectionState != state) {
                currentConnectionState = state
                listView.adapter.update(true)
            }
        }else if(id == NotificationCenter.proxyCheckDone){
            val proxyInfo = args[0] as ProxyInfo?
            if (proxyInfo != null && proxyInfo.link == dahlProxy.link) {
                dahlProxy = proxyInfo
            }
            listView.adapter.update(true)
        } else {
            listView.adapter.update(true)
        }
    }

    private fun checkProxyList() {
        proxyList.add(dahlProxy)
        proxyList.addAll(SharedConfig.proxyList)
        var a = 0
        val count = proxyList.size
        while (a < count) {
            val proxyInfo = proxyList[a]
            if (proxyInfo.checking || SystemClock.elapsedRealtime() - proxyInfo.availableCheckTime < 2 * 60 * 1000) {
                a++
                continue
            }
            proxyInfo.checking = true
            proxyInfo.proxyCheckPingId = ConnectionsManager.getInstance(currentAccount).checkProxy(
                proxyInfo.address, proxyInfo.port, proxyInfo.username, proxyInfo.password, proxyInfo.secret
            ) { time: Long ->
                AndroidUtilities.runOnUIThread {
                    proxyInfo.availableCheckTime = SystemClock.elapsedRealtime()
                    proxyInfo.checking = false
                    if (time == -1L) {
                        proxyInfo.available = false
                        proxyInfo.ping = 0
                    } else {
                        proxyInfo.ping = time
                        proxyInfo.available = true
                    }
                    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.proxyCheckDone, proxyInfo)
                }
            }
            a++
        }
    }

}

internal class TextDetailProxyCell(context: Context) : FrameLayout(context) {
    private val textView = TextView(context)
    private val valueTextView: TextView
    private val checkImageView: ImageView
    private var currentInfo: ProxyInfo? = null
    private var checkDrawable: Drawable? = null

    private val checkBox: CheckBox2
    private var isSelected = false
    private var isSelectionEnabled = false

    private var color = 0

    var isDahl = false
    var showDetails: ((ProxyInfo?) -> Unit)? = null

    init {
        setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite))
        textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText))
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
        textView.setLines(1)
        textView.maxLines = 1
        textView.isSingleLine = true
        textView.ellipsize = TextUtils.TruncateAt.END
        textView.gravity = (if (LocaleController.isRTL) Gravity.RIGHT else Gravity.LEFT) or Gravity.CENTER_VERTICAL
        addView(
            textView,
            LayoutHelper.createFrame(
                LayoutHelper.WRAP_CONTENT,
                LayoutHelper.WRAP_CONTENT.toFloat(),
                (if (LocaleController.isRTL) Gravity.RIGHT else Gravity.LEFT) or Gravity.TOP,
                (if (LocaleController.isRTL) 56 else 21).toFloat(),
                10f,
                (if (LocaleController.isRTL) 21 else 56).toFloat(),
                0f
            )
        )

        valueTextView = TextView(context)
        valueTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13f)
        valueTextView.gravity = if (LocaleController.isRTL) Gravity.RIGHT else Gravity.LEFT
        valueTextView.setLines(1)
        valueTextView.maxLines = 1
        valueTextView.isSingleLine = true
        valueTextView.compoundDrawablePadding = AndroidUtilities.dp(6f)
        valueTextView.ellipsize = TextUtils.TruncateAt.END
        valueTextView.setPadding(0, 0, 0, 0)
        addView(
            valueTextView,
            LayoutHelper.createFrame(
                LayoutHelper.WRAP_CONTENT,
                LayoutHelper.WRAP_CONTENT.toFloat(),
                (if (LocaleController.isRTL) Gravity.RIGHT else Gravity.LEFT) or Gravity.TOP,
                (if (LocaleController.isRTL) 56 else 21).toFloat(),
                35f,
                (if (LocaleController.isRTL) 21 else 56).toFloat(),
                0f
            )
        )

        checkImageView = ImageView(context)
        checkImageView.setImageResource(R.drawable.msg_info)
        checkImageView.colorFilter =
            PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3), PorterDuff.Mode.MULTIPLY)
        checkImageView.scaleType = ImageView.ScaleType.CENTER
        checkImageView.contentDescription = getString(R.string.Edit)
        addView(
            checkImageView,
            LayoutHelper.createFrame(48, 48f, (if (LocaleController.isRTL) Gravity.LEFT else Gravity.RIGHT) or Gravity.TOP, 8f, 8f, 8f, 0f)
        )
        checkImageView.setOnClickListener { _ -> showDetails?.invoke(currentInfo) }

        checkBox = CheckBox2(context, 21)
        checkBox.setColor(Theme.key_checkbox, Theme.key_radioBackground, Theme.key_checkboxCheck)
        checkBox.setDrawBackgroundAsArc(14)
        checkBox.visibility = GONE
        addView(
            checkBox,
            LayoutHelper.createFrame(
                24,
                24f,
                (if (LocaleController.isRTL) Gravity.RIGHT else Gravity.LEFT) or Gravity.CENTER_VERTICAL,
                16f,
                0f,
                8f,
                0f
            )
        )

        setWillNotDraw(false)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(64f) + 1, MeasureSpec.EXACTLY)
        )
    }

    @SuppressLint("SetTextI18n")
    fun setProxy(proxyInfo: ProxyInfo) {
        if (isDahl) {
            textView.text = getString(R.string.ProxyDahl)
        } else {
            textView.text = proxyInfo.address + ":" + proxyInfo.port
        }
        checkImageView.visibility = if (isDahl) GONE else VISIBLE
        currentInfo = proxyInfo
    }

    fun updateStatus(useProxySettings: Boolean, currentConnectionState: Int) {
        val colorKey: Int
        val current = SharedConfig.currentProxy
        if (current != null && (useProxySettings || DahlSettings.isProxyEnabled) && current.link == currentInfo!!.link) {
            if (currentConnectionState == ConnectionsManager.ConnectionStateConnected || currentConnectionState == ConnectionsManager.ConnectionStateUpdating) {
                colorKey = Theme.key_windowBackgroundWhiteBlueText6
                if (currentInfo!!.ping != 0L) {
                    valueTextView.text =
                        getString(R.string.Connected) + ", " + LocaleController.formatString("Ping", R.string.Ping, currentInfo!!.ping)
                } else {
                    valueTextView.text = getString(R.string.Connected)
                }
                if (!currentInfo!!.checking && !currentInfo!!.available) {
                    currentInfo!!.availableCheckTime = 0
                }
            } else {
                colorKey = Theme.key_windowBackgroundWhiteGrayText2
                valueTextView.text = getString(R.string.Connecting)
            }
        } else {
            if (currentInfo!!.checking) {
                valueTextView.text = getString(R.string.Checking)
                colorKey = Theme.key_windowBackgroundWhiteGrayText2
            } else if (currentInfo!!.available) {
                if (currentInfo!!.ping != 0L) {
                    valueTextView.text =
                        getString(R.string.Available) + ", " + LocaleController.formatString("Ping", R.string.Ping, currentInfo!!.ping)
                } else {
                    valueTextView.text = getString(R.string.Available)
                }
                colorKey = Theme.key_windowBackgroundWhiteGreenText
            } else {
                valueTextView.text = getString(R.string.Unavailable)
                colorKey = Theme.key_text_RedRegular
            }
        }
        color = Theme.getColor(colorKey)
        valueTextView.tag = colorKey
        valueTextView.setTextColor(color)
        if (checkDrawable != null) {
            checkDrawable!!.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY)
        }
    }

    fun setChecked(checked: Boolean) {
        if (checked) {
            if (checkDrawable == null) {
                checkDrawable = resources.getDrawable(R.drawable.proxy_check).mutate()
            }

            checkDrawable!!.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY)

            if (LocaleController.isRTL) {
                valueTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, checkDrawable, null)
            } else {
                valueTextView.setCompoundDrawablesWithIntrinsicBounds(checkDrawable, null, null, null)
            }
        } else {
            valueTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        }
    }

    fun setValue(value: CharSequence?) {
        valueTextView.text = value
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawLine(
            (if (LocaleController.isRTL) 0 else AndroidUtilities.dp(20f)).toFloat(),
            (measuredHeight - 1).toFloat(),
            (measuredWidth - (if (LocaleController.isRTL) AndroidUtilities.dp(20f) else 0)).toFloat(),
            (measuredHeight - 1).toFloat(),
            Theme.dividerPaint
        )
    }
}