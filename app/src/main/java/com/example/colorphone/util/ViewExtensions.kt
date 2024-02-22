package com.wecan.inote.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Rect
import android.net.ConnectivityManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.StrictMode
import android.os.SystemClock
import android.text.InputType
import android.text.SpannableString
import android.text.format.Formatter
import android.text.style.ForegroundColorSpan
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.colorphone.R
import com.example.colorphone.databinding.CustomToastPinnedBinding
import com.example.colorphone.databinding.ItemOptionViewBinding
import com.example.colorphone.databinding.ItemToastSyncSuccessBinding
import com.example.colorphone.util.RepeatType
import com.example.colorphone.util.SortType
import com.example.colorphone.util.TypeColorNote
import java.util.Locale
import java.util.TimeZone

fun Context.haveNetworkConnection(): Boolean {
    var haveConnectedWifi = false
    var haveConnectedMobile = false
    return try {
        val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.allNetworkInfo
        for (ni in netInfo) {
            if (ni.typeName
                            .equals("WIFI", ignoreCase = true)
            ) if (ni.isConnected) haveConnectedWifi = true
            if (ni.typeName
                            .equals("MOBILE", ignoreCase = true)
            ) if (ni.isConnected) haveConnectedMobile = true
        }
        haveConnectedWifi || haveConnectedMobile
    } catch (e: java.lang.Exception) {
        System.err.println(e.toString())
        false
    }
}

fun Activity.getHeightDevice(): Int {
    val displayMetrics = DisplayMetrics()
    this.windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.heightPixels
}

fun Activity.getWidthDevice(): Int {
    val displayMetrics = DisplayMetrics()
    this.windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.widthPixels
}

fun View.changeBackgroundColor(newColor: Int) {
    setBackgroundColor(
        ContextCompat.getColor(
            context,
            newColor
        )
    )
}

fun ImageView.setTintColor(@ColorRes color: Int) {
    imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, color))
}

fun TextView.changeTextColor(newColor: Int) {
    setTextColor(
        ContextCompat.getColor(
            context,
            newColor
        )
    )
}

fun View.animRotation() {
    val anim = RotateAnimation(
        0f, 360f,
        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
    )
    anim.interpolator = LinearInterpolator()
    anim.duration = 4000
    anim.isFillEnabled = true
    anim.repeatCount = Animation.INFINITE
    anim.fillAfter = true
    startAnimation(anim)
}

fun View.animRotation2() {
    val anim = RotateAnimation(
        360f, 0f,
        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
    )
    anim.interpolator = LinearInterpolator()
    anim.duration = 4000
    anim.isFillEnabled = true
    anim.repeatCount = Animation.INFINITE
    anim.fillAfter = true
    startAnimation(anim)
}

fun View.animRotation3() {
    val anim = RotateAnimation(
        0f, 360f,
        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
    )
    anim.interpolator = LinearInterpolator()
    anim.duration = 1500
    anim.isFillEnabled = true
    anim.repeatCount = Animation.INFINITE
    anim.fillAfter = true
    startAnimation(anim)
}

fun View.isShow() = visibility == View.VISIBLE

fun View.isGone() = visibility == View.GONE

fun View.isInvisible() = visibility == View.INVISIBLE

fun View.show() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.inv() {
    visibility = View.INVISIBLE
}

fun View.setPreventDoubleClick(debounceTime: Long = 500, action: () -> Unit) {
    this.setOnClickListener(object : View.OnClickListener {
        private var lastClickTime: Long = 0
        override fun onClick(v: View?) {
            if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) return
            action()
            lastClickTime = SystemClock.elapsedRealtime()
        }
    })
}

fun View.setOnClickAnim(action: () -> Unit) {
    this.setOnClickListener { v ->
        v?.startAnimation(AnimationUtils.loadAnimation(context, R.anim.ripple))
        action()
    }
}

fun View.startAnim() {
    startAnimation(AnimationUtils.loadAnimation(context, R.anim.ripple))
}


fun View.setPreventDoubleClickScaleView(debounceTime: Long = 500, action: () -> Unit) {
    setOnTouchListener(object : View.OnTouchListener {
        private var lastClickTime: Long = 0
        private var rect: Rect? = null

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            fun setScale(scale: Float) {
                v.scaleX = scale
                v.scaleY = scale
            }

            if (event.action == MotionEvent.ACTION_DOWN) {
                //action down: scale view down
                rect = Rect(v.left, v.top, v.right, v.bottom)
                setScale(0.9f)
            } else if (rect != null && !rect!!.contains(
                        v.left + event.x.toInt(),
                        v.top + event.y.toInt()
                    )
            ) {
                //action moved out
                setScale(1f)
                return false
            } else if (event.action == MotionEvent.ACTION_UP) {
                //action up
                setScale(1f)
                //handle click too fast
                if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) {
                } else {
                    lastClickTime = SystemClock.elapsedRealtime()
                    action()
                }
            } else {
                //other
            }

            return true
        }
    })
}

fun Fragment.displayToast(msg: String) {
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}

fun Fragment.displayToast(@StringRes msg: Int) {
    Toast.makeText(context, getString(msg), Toast.LENGTH_SHORT).show()
}

fun Fragment.convertDpToPx(dp: Int): Int {
    val dip = dp.toFloat()
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dip,
        resources.displayMetrics
    ).toInt()
}

fun Context.convertDpToPx(dp: Int): Int {
    val dip = dp.toFloat()
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dip,
        resources.displayMetrics
    ).toInt()
}

fun getKeyEventByColor(color: String, isEdit: Boolean = false): String? {
    return when (color) {
        TypeColorNote.DEFAULT.name -> "ChangeColor_AllColor_Click"
        TypeColorNote.A_ORANGE.name -> if (isEdit) "ChangeColor_EditOrange_Click" else "ChangeColor_Orange_Click"
        TypeColorNote.B_GREEN.name -> if (isEdit) "ChangeColor_EditGreen_Click" else "ChangeColor_Green_Click"
        TypeColorNote.F_PRIMARY.name -> if (isEdit) "ChangeColor_EditPurple_Click" else "ChangeColor_Purple_Click"
        TypeColorNote.D_RED.name -> if (isEdit) "ChangeColor_EditRed_Click" else "ChangeColor_Red_Click"
        TypeColorNote.GRAY.name -> if (isEdit) "ChangeColor_EditBlack_Click" else "ChangeColor_Black_Click"
        TypeColorNote.C_BLUE.name -> if (isEdit) "ChangeColor_EditGray_Click" else "ChangeColor_Gray_Click"
        else -> null
    }
}

fun Context.sendEmailMore(
    addresses: Array<String>,
    subject: String? = null,
    body: String? = ""
) {

    disableExposure()

    val title = " iNote - Please tell us your bug or suggestions, we will hear everything!"

    val intent = Intent(Intent.ACTION_SENDTO)
    // intent.type = "message/rfc822"
    intent.data = Uri.parse("mailto:") // only email apps should handle this
    intent.putExtra(Intent.EXTRA_EMAIL, addresses)
    intent.putExtra(Intent.EXTRA_SUBJECT, subject ?: title)

//    intent.putExtra(
//        Intent.EXTRA_TEXT, body + "\n\n\n" +
//                "DEVICE INFORMATION (Device information is useful for application improvement and development)"
//                + "\n\n" + getDeviceInfo()
//    )
    //intent.selector = emailSelectorIntent

    try {
        startActivity(Intent.createChooser(intent, ""))
    } catch (e: Exception) {
        Toast.makeText(this, "you need install gmail", Toast.LENGTH_SHORT).show()
    }
}

private fun getDeviceInfo(): String {
    val densityText = when (Resources.getSystem().displayMetrics.densityDpi) {
        DisplayMetrics.DENSITY_LOW -> "LDPI"
        DisplayMetrics.DENSITY_MEDIUM -> "MDPI"
        DisplayMetrics.DENSITY_HIGH -> "HDPI"
        DisplayMetrics.DENSITY_XHIGH -> "XHDPI"
        DisplayMetrics.DENSITY_XXHIGH -> "XXHDPI"
        DisplayMetrics.DENSITY_XXXHIGH -> "XXXHDPI"
        else -> "HDPI"
    }

    //TODO: Update android Q
    var megAvailable = 0L

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        val stat = StatFs(Environment.getExternalStorageDirectory().path)
        val bytesAvailable: Long
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            bytesAvailable = stat.blockSizeLong * stat.availableBlocksLong
            megAvailable = bytesAvailable / (1024 * 1024)
        }
    }


    return "Manufacturer ${Build.MANUFACTURER}, Model ${Build.MODEL}," +
            " ${Locale.getDefault()}, " +
            "osVer ${Build.VERSION.RELEASE}, Screen ${Resources.getSystem().displayMetrics.widthPixels}x${Resources.getSystem().displayMetrics.heightPixels}, " +
            "$densityText, Free space ${megAvailable}MB, TimeZone ${
                TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT)
            }"
}

private fun disableExposure() {
    if (Build.VERSION.SDK_INT >= 24) {
        try {
            val m = StrictMode::class.java.getMethod("disableDeathOnFileUriExposure")
            m.invoke(null)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}


fun Context.getStringResourceByName(aString: String): String {
    val packageName: String = packageName
    val resId: Int = resources.getIdentifier(aString, "string", packageName)
    return getString(resId)
}

fun Context.openBrowser(url: String) {
    var url = url
    if (!url.startsWith("http://") && !url.startsWith("https://")) {
        url = "http://$url"
    }
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    try {
        startActivity(browserIntent)
    } catch (ex: java.lang.Exception) {
        ex.printStackTrace()
    }
}

fun LinearLayout.setWidth(width: Int) {
    val params = LinearLayout.LayoutParams(
        width,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )
    this.apply {
        layoutParams = params
        requestFocus()
    }
}

fun ConstraintLayout.setWidth(width: Int) {
    val params = LinearLayout.LayoutParams(
        width,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )
    this.apply {
        layoutParams = params
        requestFocus()
    }
}

fun Context.getIp(): String {
    val wifiMgr = this.getSystemService(Context.WIFI_SERVICE) as WifiManager?
    val wifiInfo = wifiMgr!!.connectionInfo
    val ip = wifiInfo.ipAddress
    return Formatter.formatIpAddress(ip)
}

fun EditText.setOnNextAction(onNext: () -> Unit) {
    setRawInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)

    setOnKeyListener { v, keyCode, event ->
        if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
            onNext()
            return@setOnKeyListener true
        } else return@setOnKeyListener false
    }

    setOnEditorActionListener { v, actionId, event ->
        if (actionId == EditorInfo.IME_ACTION_NEXT) {
            onNext()
            return@setOnEditorActionListener true
        } else return@setOnEditorActionListener false
    }
}


fun ItemOptionViewBinding.bindViewMenuSort(sortType: String) {
    tvView.text = root.context.getString(R.string.softByLabel)
    tvList.apply {
        setDrawableLeft(R.drawable.ic_modified_time)
        text = root.context.getString(R.string.modifiedTimeLabel)
        isSelected = sortType == SortType.MODIFIED_TIME.name
        setOpacitySelect()
    }
    tvGrid.apply {
        setDrawableLeft(R.drawable.ic_create_time)
        text = root.context.getString(R.string.createTime)
        isSelected = sortType == SortType.CREATE_TIME.name
        setOpacitySelect()
    }
    tvDetails.apply {
        setDrawableLeft(R.drawable.ic_reminder_time_option)
        text = root.context.getString(R.string.reminderTime)
        isSelected = sortType == SortType.REMINDER_TIME.name
        setOpacitySelect()
    }
    tvItemFour.apply {
        show()
        setDrawableLeft(R.drawable.ic_color_sort)
        text = root.context.getString(R.string.color)
        isSelected = sortType == SortType.COLOR.name
        setOpacitySelect()
    }
}

fun TextView.setDrawableLeft(id: Int) {
    this.setCompoundDrawablesWithIntrinsicBounds(id, 0, 0, 0);
}

fun TextView.setOpacitySelect() {
    this.setTextColor(
        ContextCompat.getColor(
            context,
            if (isSelected) R.color.neutral500 else R.color.neutral50038
        )
    )
}

fun mapIdColor(
    nameColor: String?,
    isGetIcon: Boolean = false,
    bgColor: ((Int) -> Unit)? = null,
    idColor: ((idIcon: Int, idBg: Int, idColor: Int, idColorBody: Int, idColorTop: Int) -> Unit)? = null
) {
    when (nameColor) {
        TypeColorNote.DEFAULT.name -> {
            bgColor?.invoke(R.drawable.bg_note_blue)
            idColor?.invoke(
                if (isGetIcon) R.drawable.ic_allbox else R.drawable.ic_none_notes,
                R.drawable.bg_item_all_notes,
                R.color.blueOption,
                R.color.blueBody,
                R.color.blueOption50
            )
        }

        TypeColorNote.A_ORANGE.name -> {
            bgColor?.invoke(R.drawable.bg_note_orange)
            idColor?.invoke(
                if (isGetIcon) R.drawable.ic_new_orange else R.drawable.ic_option_orange,
                R.drawable.bg_item_orange,
                R.color.orangeOption,
                R.color.orangeBody,
                R.color.orangeOption50
            )
        }

        TypeColorNote.B_GREEN.name -> {
            bgColor?.invoke(R.drawable.bg_note_green)
            idColor?.invoke(
                if (isGetIcon) R.drawable.ic_new_green else R.drawable.ic_option_green,
                R.drawable.bg_item_green,
                R.color.greenOption,
                R.color.greenBody,
                R.color.greenOption50
            )
        }

        TypeColorNote.F_PRIMARY.name -> {
            bgColor?.invoke(R.drawable.bg_note_primary)
            idColor?.invoke(
                if (isGetIcon) R.drawable.ic_new_primary else R.drawable.ic_option_primary,
                R.drawable.bg_item_primary, R.color.primaryOption, R.color.primaryBody, R.color.primary50
            )
        }

        TypeColorNote.D_RED.name -> {
            bgColor?.invoke(R.drawable.bg_note_red)
            idColor?.invoke(
                if (isGetIcon) R.drawable.ic_new_red else R.drawable.ic_option_red,
                R.drawable.bg_item_red, R.color.redOption, R.color.redBody, R.color.redOption50
            )
        }

        TypeColorNote.GRAY.name -> {
            bgColor?.invoke(R.drawable.bg_note_gray)
            idColor?.invoke(
                if (isGetIcon) R.drawable.ic_new_gray else R.drawable.ic_option_gray,
                R.drawable.bg_item_gray, R.color.grayOption, R.color.grayBody, R.color.grayOption50
            )
        }

        TypeColorNote.BLINK.name -> {
            bgColor?.invoke(R.drawable.bg_note_blink)
            idColor?.invoke(
                if (isGetIcon) R.drawable.ic_new_blink else R.drawable.ic_option_blink,
                R.drawable.bg_item_blink, R.color.blinkOption, R.color.blinkBody, R.color.blinkOption50
            )
        }

        else -> {
            bgColor?.invoke(R.drawable.bg_note_blue)
            idColor?.invoke(
                if (isGetIcon) R.drawable.ic_blue_new else R.drawable.ic_blue_option,
                R.drawable.bg_item_blue, R.color.blueOption, R.color.blueBody, R.color.blueOption50
            )
        }
    }
}

fun View.setBgItemNote(isDarkMode: Boolean, nameColor: String) {
    if (!isDarkMode) {
        setBackgroundResource(R.drawable.bg_item_selected)
    } else {
        when (nameColor) {

            TypeColorNote.A_ORANGE.name -> setBackgroundResource(R.drawable.bg_item_selected_orange)

            TypeColorNote.B_GREEN.name -> setBackgroundResource(R.drawable.bg_item_selected_green)

            TypeColorNote.F_PRIMARY.name -> setBackgroundResource(R.drawable.bg_item_selected_primary)

            TypeColorNote.D_RED.name -> setBackgroundResource(R.drawable.bg_item_selected_red)

            TypeColorNote.C_BLUE.name -> setBackgroundResource(R.drawable.bg_item_selected_blue)

            TypeColorNote.GRAY.name -> setBackgroundResource(R.drawable.bg_item_selected_gray)

            TypeColorNote.BLINK.name -> setBackgroundResource(R.drawable.bg_item_selected_blink)

            else -> setBackgroundResource(R.drawable.bg_item_selected)
        }
    }
}

fun mapIdColorWidget(
    nameColor: String?,
    idColor: ((idBgBody: Int, idIcon: Int) -> Unit)? = null
) {
    when (nameColor) {

        TypeColorNote.A_ORANGE.name -> idColor?.invoke(
            R.drawable.bg_note_orange, R.drawable.ic_bookmark_orange
        )

        TypeColorNote.B_GREEN.name -> idColor?.invoke(
            R.drawable.bg_note_green, R.drawable.ic_bookmark_green
        )

        TypeColorNote.F_PRIMARY.name -> idColor?.invoke(
            R.drawable.bg_note_primary, R.drawable.ic_bookmark_primary
        )

        TypeColorNote.D_RED.name -> idColor?.invoke(
            R.drawable.bg_note_red, R.drawable.ic_bookmark_red
        )

        TypeColorNote.C_BLUE.name -> idColor?.invoke(
            R.drawable.bg_note_blue, R.drawable.ic_bookmark_blue
        )

        TypeColorNote.BLINK.name -> idColor?.invoke(
            R.drawable.bg_note_blink, R.drawable.ic_bookmark_blink
        )

        TypeColorNote.GRAY.name -> idColor?.invoke(
            R.drawable.bg_note_gray, R.drawable.ic_bookmark_gray
        )

        else -> idColor?.invoke(
            R.drawable.bg_note_blue,
            R.drawable.ic_bookmark_blue
        )
    }
}

fun Toast.showToastSyncSuccess(ct: Context) {
    val binding: ItemToastSyncSuccessBinding =
        ItemToastSyncSuccessBinding.inflate(LayoutInflater.from(ct))

    binding.tvToastSync.text = ct.getString(R.string.notesSyncedSuccessfully)

    this.apply {
        setGravity(Gravity.BOTTOM, 0, 0)
        duration = Toast.LENGTH_SHORT
        view = binding.root
        show()
    }
}

fun Toast.showCustomToastPinned(activity: Context, isPinned: Boolean) {
    val binding: CustomToastPinnedBinding =
        CustomToastPinnedBinding.inflate(LayoutInflater.from(activity))

    binding.tvContent.text =
        if (isPinned) activity.getString(R.string.pinnedToTheTopLabel) else activity.getString(
            R.string.unPinnedFromTheTop
        )

    this.apply {
        setGravity(Gravity.BOTTOM, 0, 280)
        duration = Toast.LENGTH_SHORT
        view = binding.root
        show()
    }
}

fun Toast.showCustomToastLocked(context: Context, isLock: Boolean) {
    val binding: CustomToastPinnedBinding =
        CustomToastPinnedBinding.inflate(LayoutInflater.from(context))

    binding.tvContent.text =
        if (isLock) context.getString(R.string.lock) else context.getString(
            R.string.un_lock
        )

    this.apply {
        setGravity(Gravity.BOTTOM, 0, 280)
        duration = Toast.LENGTH_SHORT
        view = binding.root
        show()
    }
}

fun Toast.showCustomToast(activity: Context, message: String) {
    val binding: CustomToastPinnedBinding =
        CustomToastPinnedBinding.inflate(LayoutInflater.from(activity))

    binding.tvContent.text = message

    this.apply {
        setGravity(Gravity.BOTTOM, 0, 280)
        duration = Toast.LENGTH_SHORT
        view = binding.root
        show()
    }
}

fun Context.getInitValueRepeat(
    mTypeRepeat: String,
    onContinue: ((valueTitle: String, value: String) -> Unit)
) {
    when (mTypeRepeat) {
        RepeatType.MORE_DAYS.name -> onContinue.invoke(
            getString(R.string.repeatDaily), getString(R.string.days)
        )

        RepeatType.MORE_WEEKS.name -> onContinue.invoke(
            getString(R.string.repeatWeekly), getString(R.string.weeks)
        )

        RepeatType.MORE_MONTH.name -> onContinue.invoke(
            getString(R.string.repeatMonthly), getString(R.string.months)
        )
    }
}

fun getIndexSelectedMenu(mTypeRepeat: String): Int {
    when (mTypeRepeat) {
        RepeatType.MORE_DAYS.name -> return 0
        RepeatType.MORE_WEEKS.name -> return 1
        RepeatType.MORE_MONTH.name -> return 2
    }
    return 0
}

fun MenuItem.setColorSelect(context: Context) {
    val s = SpannableString(this.title)
    s.setSpan(
        ForegroundColorSpan(
            ContextCompat.getColor(
                context,
                R.color.neutral500
            )
        ), 0, s.length, 0
    )
    this.title = s
}

fun Window.setSoftInputResize() {
    setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
}

