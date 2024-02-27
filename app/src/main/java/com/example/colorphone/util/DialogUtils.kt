package com.example.colorphone.util

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.forEachIndexed
import androidx.core.view.get
import androidx.core.widget.doAfterTextChanged
import com.example.colorphone.R
import com.example.colorphone.databinding.DialogChangePasswordBinding
import com.example.colorphone.databinding.DialogFeedbackBinding
import com.example.colorphone.databinding.DialogLoginSyncNowBinding
import com.example.colorphone.databinding.DialogOtpBinding
import com.example.colorphone.databinding.DialogRecommendLockBinding
import com.example.colorphone.databinding.DialogSetPasswordBinding
import com.example.colorphone.databinding.DialogSetProtectEmailBinding
import com.example.colorphone.databinding.DialogVerifyBinding
import com.example.colorphone.databinding.ItemOptionViewBinding
import com.example.colorphone.databinding.LayoutAddToWaitingScreenBinding
import com.example.colorphone.databinding.LayoutDialogRepeatDailyBinding
import com.example.colorphone.databinding.LayoutLoginSuccessBinding
import com.example.colorphone.databinding.LayoutRatingAppBinding
import com.example.colorphone.databinding.LayoutSaveYourChangeReminderBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.wecan.inote.util.bindViewMenuSort
import com.wecan.inote.util.getIndexSelectedMenu
import com.wecan.inote.util.getInitValueRepeat
import com.wecan.inote.util.gone
import com.wecan.inote.util.haveNetworkConnection
import com.wecan.inote.util.setColorSelect
import com.wecan.inote.util.setOpacitySelect
import com.wecan.inote.util.setPreventDoubleClick
import com.wecan.inote.util.setSoftInputResize
import com.wecan.inote.util.show
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


fun Context.showAlertDialogBackOnReminder(onContinue: (() -> Unit)) {
    val dialog = AlertDialog.Builder(this)
        .create()
    val binding: LayoutSaveYourChangeReminderBinding =
        LayoutSaveYourChangeReminderBinding.inflate(LayoutInflater.from(this))
    dialog.setView(binding.root)
    binding.apply {
        tvButtonBack.setOnClickListener { dialog.dismiss() }
        tvButtonContinue.setOnClickListener {
            onContinue.invoke()
            dialog.dismiss()
        }
    }
    dialog.insetMargin()
    dialog.setCanceledOnTouchOutside(false)
    dialog.show()
}

fun Context.showAlertDialog(
    tittle: String,
    content: String,
    textAction: String,
    callAction: (() -> Unit)
) {
    val dialog = AlertDialog.Builder(this)
        .create()
    val binding: LayoutSaveYourChangeReminderBinding =
        LayoutSaveYourChangeReminderBinding.inflate(LayoutInflater.from(this))
    dialog.setView(binding.root)
    binding.apply {
        tvTittle.text = tittle
        tvContent.text = content
        tvButtonContinue.text = textAction
        tvButtonBack.setOnClickListener { dialog.dismiss() }
        tvButtonContinue.setOnClickListener {
            callAction.invoke()
            dialog.dismiss()
        }
    }
    dialog.insetMargin()
    dialog.setCanceledOnTouchOutside(false)
    dialog.show()
}

fun Context.showDialogLoginSuccess() {
    val dialog = AlertDialog.Builder(this)
        .create()
    val binding: LayoutLoginSuccessBinding =
        LayoutLoginSuccessBinding.inflate(LayoutInflater.from(this))
    dialog.setView(binding.root)
    dialog.setCanceledOnTouchOutside(true)
    dialog.insetMargin()
    dialog.show()
}

fun Context.showDialogRating(submit: ((Int) -> Unit)? = null) {
    val dialog = AlertDialog.Builder(this)
        .create()
    val binding: LayoutRatingAppBinding =
        LayoutRatingAppBinding.inflate(LayoutInflater.from(this))
    dialog.setView(binding.root)
    binding.apply {
        ivCloseDialog.setOnClickListener { dialog.dismiss() }
        tvWeTryEveryDay.makeHighLightText("iNote", R.color.blueOption)
        tvIfYouLike.makeHighLightText("5-star?", R.color.orangeOption)

        llStar.forEachIndexed { index, view ->
            view.setOnClickListener {
                handleViewClickStar(index) { pos ->
                    submit?.invoke(pos)
                    dialog.dismiss()
                }
            }
        }
    }
    dialog.setCanceledOnTouchOutside(false)
    dialog.insetMargin()
    dialog.show()
}

fun Context.showDialogFeedback(feedback: () -> Unit) {
    val dialog = AlertDialog.Builder(this)
        .create()
    val binding: DialogFeedbackBinding =
        DialogFeedbackBinding.inflate(LayoutInflater.from(this))
    dialog.setView(binding.root)
    binding.apply {
        ivCloseDialog.setOnClickListener { dialog.dismiss() }
        tvFeedback.setOnClickListener {
            dialog.dismiss()
            feedback.invoke()
        }
    }
    dialog.setCanceledOnTouchOutside(true)
    dialog.insetMargin()
    dialog.show()
}

fun AlertDialog.insetMargin() {
    val back = ColorDrawable(Color.TRANSPARENT)
    val inset = InsetDrawable(back, 30)
    this.window?.setBackgroundDrawable(inset)
}

fun LayoutRatingAppBinding.handleViewClickStar(pos: Int, submit: ((Int) -> Unit)) {
    CoroutineScope(Dispatchers.Main).launch {
        llStar.forEachIndexed { index, view ->
            view.isSelected = index <= pos
        }
        tvSubmit.apply {
            setTextColor(ContextCompat.getColor(root.context, R.color.white))
            setBackgroundResource(R.drawable.bg_submit_selected)
            setOnClickListener {
                submit.invoke(pos)
            }
        }
        if (pos == 4) {
            delay(600)
            submit.invoke(pos)
        }
    }
}


fun Context.showDialogAddToWaitingScreen(isAddWidgetBar: Boolean, onContinue: (() -> Unit)) {
    val dialog = AlertDialog.Builder(this)
        .create()
    val binding: LayoutAddToWaitingScreenBinding =
        LayoutAddToWaitingScreenBinding.inflate(LayoutInflater.from(this))
    dialog.setView(binding.root)
    binding.apply {
        tvAdd.setOnClickListener {
            onContinue.invoke()
            dialog.dismiss()
        }
        tvCancel.setOnClickListener {
            dialog.dismiss()
        }
        if (isAddWidgetBar) {
            ivItem.setImageResource(R.drawable.ic_widgetbar)
            tvNameItem.text = this@showDialogAddToWaitingScreen.getString(R.string.noteTools)
            tvSizeItem.text = "4 x 1"
        } else {
            ivItem.setImageResource(R.drawable.image_text)
            tvNameItem.text = this@showDialogAddToWaitingScreen.getString(R.string.noteSticky)
            tvSizeItem.text = "2 x 2"
        }
    }
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.setCanceledOnTouchOutside(false)
    dialog.show()
}

fun Context.showAlertDialogTip(message: String, onContinue: (() -> Unit), onBack: (() -> Unit)) {
    val dialog = AlertDialog.Builder(this)
        .create()
    val binding: LayoutSaveYourChangeReminderBinding =
        LayoutSaveYourChangeReminderBinding.inflate(LayoutInflater.from(this))
    dialog.setView(binding.root)
    binding.apply {
        tvTittle.gone()
        tvTip.show()
        tvContent.text = message
        tvButtonBack.text = this@showAlertDialogTip.getString(R.string.cancel)
        tvButtonBack.setOnClickListener {
            onBack.invoke()
            dialog.dismiss()
        }
        tvButtonContinue.setOnClickListener {
            onContinue.invoke()
            dialog.dismiss()
        }
    }
    dialog.insetMargin()
    dialog.setCanceledOnTouchOutside(false)
    dialog.show()
}

fun Context.showAlertDialogRepeatDaily(
    typeRepeat: String?,
    valueRepeat: Int? = 1,
    onClickView: ((String) -> Unit)? = null,
    onContinue: ((value: Int, type: String) -> Unit)
) {
    val dialog = AlertDialog.Builder(this)
        .create()
    val binding: LayoutDialogRepeatDailyBinding =
        LayoutDialogRepeatDailyBinding.inflate(LayoutInflater.from(this))
    dialog.setView(binding.root)
    var mTypeRepeat: String =
        if (typeRepeat.isNullOrEmpty()) RepeatType.MORE_DAYS.name else typeRepeat
    binding.apply {
        tvButtonContinue.text = getString(R.string.save)
        tvButtonBack.text = getString(R.string.cancel)
        getInitValueRepeat(mTypeRepeat) { valueTitle, value ->
            tvTittle.text = valueTitle
            tvDaily.text = value
            etNumberDaily.setText(if (typeRepeat.isNullOrEmpty()) "15" else valueRepeat.toString())
        }

        etNumberDaily.setOnClickListener { onClickView?.invoke("CustomRep_Filing_Click") }
        etNumberDaily.doAfterTextChanged {
            tvButtonContinue.isEnabled = etNumberDaily.text.toString().isNotEmpty()
            tvButtonContinue.alpha = if (etNumberDaily.text.toString().isNotEmpty()) 1f else 0.6f
        }

        tvTittle.setOnClickListener {
            onClickView?.invoke("CustomRep_Frequency_Click")
            val mContext: Context =
                ContextThemeWrapper(this@showAlertDialogRepeatDaily, R.style.BasePopupMenu)
            val popupMenu =
                PopupMenu(mContext, tvTittle, Gravity.CENTER)
            popupMenu.inflate(R.menu.menu_option_repeat)
            popupMenu.menu[getIndexSelectedMenu(mTypeRepeat)].setColorSelect(root.context)
            popupMenu.setOnMenuItemClickListener {
                when (it?.itemId) {
                    R.id.repeatDaily -> {
                        onClickView?.invoke("CustomRep_Daily_Click")
                        tvDaily.text = getString(R.string.days)
                        tvTittle.text = getString(R.string.repeatDaily)
                        mTypeRepeat = RepeatType.MORE_DAYS.name
                        return@setOnMenuItemClickListener true
                    }

                    R.id.repeatWeekly -> {
                        onClickView?.invoke("CustomRep_Weekly_Click")
                        tvDaily.text = getString(R.string.weeks)
                        tvTittle.text = getString(R.string.repeatWeekly)
                        mTypeRepeat = RepeatType.MORE_WEEKS.name
                        return@setOnMenuItemClickListener true
                    }

                    R.id.repeatMonth -> {
                        onClickView?.invoke("CustomRep_Monthly_Click")
                        tvDaily.text = getString(R.string.months)
                        tvTittle.text = getString(R.string.repeatMonthly)
                        mTypeRepeat = RepeatType.MORE_MONTH.name
                        return@setOnMenuItemClickListener true
                    }

                    else -> {
                        return@setOnMenuItemClickListener false
                    }
                }
            }
            popupMenu.show()
        }
        tvButtonBack.setOnClickListener {
            onClickView?.invoke("CustomRep_Cancel_Click")
            dialog.dismiss()
        }
        tvButtonContinue.setOnClickListener {
            onClickView?.invoke("CustomRep_Save_Click")
            val value =
                if (etNumberDaily.text.toString().isEmpty()) 1 else etNumberDaily.text.toString()
                    .toInt()
            onContinue.invoke(value, mTypeRepeat)
            dialog.dismiss()
        }
    }
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.setCanceledOnTouchOutside(false)
    dialog.show()
}

fun Context.showDialogOptionView(typeView: String, call: ((TypeView) -> Unit)? = null) {
    val dialog = BottomSheetDialog(this)
    val binding: ItemOptionViewBinding =
        ItemOptionViewBinding.inflate(LayoutInflater.from(this))
    dialog.setContentView(binding.root)
    val bottomSheet = binding.root.parent as View
    bottomSheet.backgroundTintMode = PorterDuff.Mode.CLEAR
    bottomSheet.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)

    binding.apply {
        tvList.isSelected = typeView == binding.tvList.text
        tvGrid.isSelected = typeView == binding.tvGrid.text
        tvDetails.isSelected = typeView == binding.tvDetails.text

        tvList.setOpacitySelect()
        tvGrid.setOpacitySelect()
        tvDetails.setOpacitySelect()

        tvList.setOnClickListener {
            call?.invoke(TypeView.List)
            dialog.dismiss()
        }
        tvGrid.setOnClickListener {
            call?.invoke(TypeView.Grid)
            dialog.dismiss()
        }
        tvDetails.setOnClickListener {
            call?.invoke(TypeView.Details)
            dialog.dismiss()
        }
    }
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.setCanceledOnTouchOutside(true)
    dialog.show()
}

fun Context.showDialogOptionSoft(sortType: String, call: ((String) -> Unit)? = null) {
    val dialog = BottomSheetDialog(this)
    val binding: ItemOptionViewBinding =
        ItemOptionViewBinding.inflate(LayoutInflater.from(this))
    dialog.setContentView(binding.root)
    binding.bindViewMenuSort(sortType)

    binding.llRoot.forEachIndexed { index, view ->
        view.setOnClickListener {
            when (index) {
                1 -> {
                    call?.invoke(SortType.MODIFIED_TIME.name)
                    dialog.dismiss()
                }

                2 -> {
                    call?.invoke(SortType.CREATE_TIME.name)
                    dialog.dismiss()
                }

                3 -> {
                    call?.invoke(SortType.REMINDER_TIME.name)
                    dialog.dismiss()
                }

                4 -> {
                    call?.invoke(SortType.COLOR.name)
                    dialog.dismiss()
                }
            }
        }
    }

    val bottomSheet = binding.root.parent as View
    bottomSheet.backgroundTintMode = PorterDuff.Mode.CLEAR
    bottomSheet.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
    dialog.setCanceledOnTouchOutside(true)
    dialog.show()
}

fun Context.showDialogLoginSyncNow(width: Int?, clickLogin: () -> Unit) {
    val dialog = AlertDialog.Builder(this)
        .create()
    val binding: DialogLoginSyncNowBinding =
        DialogLoginSyncNowBinding.inflate(LayoutInflater.from(this))
    dialog.setView(binding.root)

    binding.ivBg.apply {
        val params: ViewGroup.LayoutParams? = this.layoutParams
        try {
            if (width != null) {
                params?.width = width
                params?.height = width.plus(1)
            }
            this.layoutParams = params
        } catch (e: Exception) {
            Log.i("ERROR", "showDialogLoginSyncNow: fail")
        }
    }

    binding.tvLoginSync.setOnClickListener {
        clickLogin.invoke()
        dialog.dismiss()
    }
    binding.ivCloseDialog.setOnClickListener { dialog.dismiss() }
    binding.clLoginSync.setOnClickListener { dialog.dismiss() }
    dialog.setCanceledOnTouchOutside(true)
    dialog.insetMargin()
    dialog.show()
}

fun Context.showDialogSetPassword(onConfirm: (String) -> Unit) {
    val dialog = BottomSheetDialog(this)
    val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_set_password, null)
    dialog.setContentView(view)
    dialog.window?.setSoftInputResize()
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    val bottomSheet = view.parent as View
    bottomSheet.backgroundTintMode = PorterDuff.Mode.CLEAR
    bottomSheet.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
    if (!dialog.isShowing) {
        dialog.show()
    }
    val binding = DialogSetPasswordBinding.bind(view)
    var pin = ""
    binding.apply {
        pinView.setOnCodeInputCompleteListener {
            Log.d("CUSTOMPINVIEW", "Complete $it")
            if ((pinView.codeText?.length ?: 0) < 4) {
                tvWrongPass.show()
                tvWrongPass.text = getString(R.string.enter_password)
            } else {
                if (pin.isEmpty()) {
                    pin = pinView.codeText.toString()
                    pinView.clear()
                    tvContent.text = getString(R.string.please_enter_password_again)
                } else {
                    if (pin == pinView.codeText) {
                        onConfirm(pin)
                        dialog.dismiss()
                    } else {
                        pinView.clear()
                        tvWrongPass.show()
                        tvContent.text = getString(R.string.wrong_password)
                    }
                }

            }
        }

        tvConfirm.setPreventDoubleClick {
            if ((pinView.codeText?.length ?: 0) < 4) {
                tvWrongPass.show()
                tvWrongPass.text = getString(R.string.enter_password)
            } else {
                if (pin.isEmpty()) {
                    pin = pinView.codeText.toString()
                    pinView.clear()
                    tvContent.text = getString(R.string.please_enter_password_again)
                } else {
                    if (pin == pinView.codeText) {
                        onConfirm(pin)
                        dialog.dismiss()
                    } else {
                        pinView.clear()
                        tvWrongPass.show()
                        tvContent.text = getString(R.string.wrong_password)
                    }
                }

            }
        }

        ivCloseDialog.setOnClickListener {
            dialog.dismiss()
        }
    }
}

fun Context.showDialogSetEmail(onConfirm: (String) -> Unit) {
    val dialog = BottomSheetDialog(this)
    val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_set_protect_email, null)
    dialog.setContentView(view)
    dialog.window?.setSoftInputResize()
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    val bottomSheet = view.parent as View
    bottomSheet.backgroundTintMode = PorterDuff.Mode.CLEAR
    bottomSheet.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
    dialog.setCancelable(false)
    if (!dialog.isShowing) {
        dialog.show()
    }
    val binding = DialogSetProtectEmailBinding.bind(view)
    binding.apply {
        tvDone.setPreventDoubleClick {
            if (edEmail.text.isEmpty() || edConfirmEmail.text.isEmpty()) {
                tvNotSame.show()
                tvNotSame.text = getString(R.string.enter_email)
            } else {
                if (edEmail.text.toString() == edConfirmEmail.text.toString()) {
                    dialog.dismiss()
                    onConfirm(edEmail.text.toString())
                } else {
                    tvNotSame.show()
                }
            }
        }
        ivCloseDialog.setOnClickListener {
            dialog.dismiss()
        }
    }
}

fun Context.showDialogChangePass(prefUtil: PrefUtil, onConfirm: () -> Unit, onForgot: () -> Unit) {
    val dialog = BottomSheetDialog(this)
    val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null)
    dialog.setContentView(view)
    dialog.window?.setSoftInputResize()
    val bottomSheet = view.parent as View
    bottomSheet.backgroundTintMode = PorterDuff.Mode.CLEAR
    bottomSheet.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
    if (!dialog.isShowing) {
        dialog.show()
    }
    val binding = DialogChangePasswordBinding.bind(view)
    binding.apply {
        pinView.setOnCodeInputCompleteListener {
            Log.d("CUSTOMPINVIEW", "Complete $it")
            if (pinView.codeText == prefUtil.pin) {
                dialog.dismiss()
                onConfirm()
            } else {
                tvWrongPass.show()
                pinView.clear()
            }
        }

        tvConfirm.setPreventDoubleClick {
            if (pinView.codeText == prefUtil.pin) {
                dialog.dismiss()
                onConfirm()
            } else {
                tvWrongPass.show()
                pinView.clear()
            }
        }

        tvForgotPass.setPreventDoubleClick {
            dialog.dismiss()
            onForgot()
        }

        ivCloseDialog.setOnClickListener {
            dialog.dismiss()
        }
    }
}

fun Context.showDialogVerify(prefUtil: PrefUtil, onConfirm: () -> Unit) {
    val dialog = BottomSheetDialog(this)
    val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_verify, null)
    dialog.setContentView(view)
    dialog.window?.setSoftInputResize()
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.setCancelable(false)
    val bottomSheet = view.parent as View
    bottomSheet.backgroundTintMode = PorterDuff.Mode.CLEAR
    bottomSheet.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
    if (!dialog.isShowing) {
        dialog.show()
    }
    val binding = DialogVerifyBinding.bind(view)
    binding.apply {
        tvDone.setPreventDoubleClick {
            if (edEmail.text.isEmpty()) {
                tvWrongEmail.show()
                tvWrongEmail.text = getString(R.string.enter_email)
            } else {
                Log.d("TAGVBNHHH", prefUtil.email.toString())
                if (edEmail.text.toString() == prefUtil.email) {
                    if (haveNetworkConnection()) {
                        dialog.dismiss()
                        onConfirm()
                    } else {
                        Toast.makeText(
                            this@showDialogVerify,
                            getString(R.string.no_network_connection),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } else {
                    tvWrongEmail.show()
                    tvWrongEmail.text = getString(R.string.wrong_email)
                }
            }
        }
        ivCloseDialog.setOnClickListener {
            dialog.dismiss()
        }
    }
}

fun Context.showDialogOtp(otp: String, onConfirm: () -> Unit) {
    val dialog = BottomSheetDialog(this)
    val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_otp, null)
    dialog.setContentView(view)
    dialog.window?.setSoftInputResize()
    dialog.setCancelable(false)
    val bottomSheet = view.parent as View
    bottomSheet.backgroundTintMode = PorterDuff.Mode.CLEAR
    bottomSheet.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
    if (!dialog.isShowing) {
        dialog.show()
    }
    val binding = DialogOtpBinding.bind(view)
    binding.apply {

        pinView.setOnCodeInputCompleteListener {
            Log.d("CUSTOMPINVIEW", "Complete $it")
            if (pinView.codeText?.isEmpty() == true) {
                tvWrongOtp.show()
                tvWrongOtp.text = getString(R.string.enter_otp)
            } else {
                if (pinView.codeText == otp) {
                    dialog.dismiss()
                    onConfirm()
                } else {
                    tvWrongOtp.show()
                    tvWrongOtp.text = getString(R.string.wrong_otp)
                }
            }
        }
        tvConfirm.setPreventDoubleClick {
            if (pinView.codeText?.isEmpty() == true) {
                tvWrongOtp.show()
                tvWrongOtp.text = getString(R.string.enter_otp)
            } else {
                if (pinView.codeText == otp) {
                    dialog.dismiss()
                    onConfirm()
                } else {
                    tvWrongOtp.show()
                    tvWrongOtp.text = getString(R.string.wrong_otp)
                }
            }
        }

        ivCloseDialog.setOnClickListener {
            dialog.dismiss()
        }
    }
}

fun Context.showDialogComplete(text: String) {
    val dialog: Dialog
    val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_set_pass_complete, null)
    val builder = android.app.AlertDialog.Builder(this)
        .setView(view)
        .setCancelable(false)

    dialog = builder.create()
    dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    if (!dialog.isShowing) {
        dialog.show()
    }
    Handler(Looper.getMainLooper()).postDelayed({
        dialog.dismiss()
    }, 2000)

}

fun Context.showDialogRecommendLock(onSetNow: () -> Unit) {
    val dialog: Dialog
    val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_recommend_lock, null)
    val builder = android.app.AlertDialog.Builder(this)
        .setView(view)
        .setCancelable(false)

    dialog = builder.create()
    dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.setCancelable(false)
    val binding = DialogRecommendLockBinding.bind(view)
    binding.icClose.setPreventDoubleClick { dialog.dismiss() }
    binding.ivSetNow.setPreventDoubleClick {
        onSetNow.invoke()
        dialog.dismiss()
    }

    if (!dialog.isShowing) {
        dialog.show()
    }

}