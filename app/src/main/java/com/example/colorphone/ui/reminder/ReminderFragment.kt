package com.example.colorphone.ui.reminder

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import android.widget.PopupMenu
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.addCallback
import androidx.lifecycle.lifecycleScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.colorphone.R
import com.example.colorphone.base.BaseFragment
import com.example.colorphone.databinding.FragmentReminderBinding
import com.example.colorphone.model.NoteModel
import com.example.colorphone.ui.reminder.workHelper.ReminderWorkerNotification
import com.example.colorphone.ui.select.SelectScreen.Companion.ITEM_FROM_SELECTED_SCREEN
import com.example.colorphone.util.Const.CHANNEL_ID_ONE_TIME_WORK
import com.example.colorphone.util.Const.CHANNEL_ID_PERIOD_WORK
import com.example.colorphone.util.Const.FORMAT_DATE_REMINDER
import com.example.colorphone.util.Const.FORMAT_DATE_REMINDER_TIME
import com.example.colorphone.util.Const.FORMAT_DATE_UPDATE
import com.example.colorphone.util.Const.ID
import com.example.colorphone.util.Const.MESSAGE
import com.example.colorphone.util.Const.TITTLE
import com.example.colorphone.util.Const.TYPE_ITEM
import com.example.colorphone.util.Const.TYPE_WORKER
import com.example.colorphone.util.RepeatType
import com.example.colorphone.util.TypeItem
import com.example.colorphone.util.ext.convertDateStringToLong
import com.example.colorphone.util.ext.convertLongToDate
import com.example.colorphone.util.ext.formatTime
import com.example.colorphone.util.ext.getCurrentTimeToLong
import com.example.colorphone.util.ext.showAlertDialogBackOnReminder
import com.example.colorphone.util.ext.showAlertDialogRepeatDaily
import com.wecan.inote.util.setColorSelect
import com.wecan.inote.util.showCustomToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ReminderFragment : BaseFragment<FragmentReminderBinding>(FragmentReminderBinding::inflate),
                         DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener,
                         PopupMenu.OnMenuItemClickListener {
    private var note: NoteModel? = null

    override fun init(view: View) {
        onListener()
        onBackPressHandle()
    }

    private var chosenYear = 0
    private var chosenMonth = 0
    private var chosenDay = 0
    private var chosenHour = 0
    private var chosenMin = 0
    private var mTypeRepeat: String? = RepeatType.DOES_NOT_REPEAT.name
    private var mValueRepeat = 1

    private val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val idNote = arguments?.getInt(ITEM_FROM_SELECTED_SCREEN)
        if (idNote != null) {
            viewModelTextNote.getNoteWithIds(idNote)
        }
    }

    private fun initTimeType(data: NoteModel) {
        note = data
        val timeItem = Calendar.getInstance(Locale.getDefault())
        timeItem.timeInMillis = note?.dateReminder ?: getCurrentTimeToLong()
        mTypeRepeat = note?.typeRepeat
        chosenYear = timeItem[Calendar.YEAR]
        chosenMonth = timeItem[Calendar.MONTH]
        chosenDay = timeItem[Calendar.DAY_OF_MONTH]
        chosenHour = timeItem[Calendar.HOUR_OF_DAY]
        chosenMin = timeItem[Calendar.MINUTE]
    }

    override fun onSubscribeObserver(view: View) {
        viewModelTextNote.itemWithIdsLD.observe(viewLifecycleOwner) {
            initTimeType(it)
            initView()
        }
    }

    private fun initView() {
        binding.apply {
            tvDateValue.text = if (note?.dateReminder != null) {
                convertLongToDate(
                    note?.dateReminder!!,
                    FORMAT_DATE_REMINDER
                )
            } else getString(R.string.none)
            tvReminderTimeValue.text = if (note?.dateReminder != null) {
                convertLongToDate(
                    note?.dateReminder!!,
                    FORMAT_DATE_REMINDER_TIME
                )
            } else getString(R.string.none)
            tvRepeatValue.text = getRepeatValue()
            switchAlarmValue.isChecked = note?.isAlarm == true
        }
    }

    private fun getRepeatValue(repeatValue: Int? = note?.repeatValue): String {
        when (mTypeRepeat) {
            RepeatType.DOES_NOT_REPEAT.name -> return getString(R.string.doesNotRepeat)
            RepeatType.DAILY.name -> return getString(R.string.daily)
            RepeatType.WEEKLY.name -> return getString(R.string.weekly)
            RepeatType.MONTHLY.name -> return getString(R.string.monthly)
            RepeatType.MORE_DAYS.name -> return getString(R.string.every).plus(" ")
                    .plus(repeatValue.toString()).plus(" ")
                    .plus(getString(R.string.days))

            RepeatType.MORE_WEEKS.name -> return getString(R.string.every).plus(" ")
                    .plus(repeatValue.toString()).plus(" ")
                    .plus(getString(R.string.weeks))

            RepeatType.MORE_MONTH.name -> return getString(R.string.every).plus(" ")
                    .plus(repeatValue.toString()).plus(" ")
                    .plus(getString(R.string.months))
        }
        return getString(R.string.none)
    }

    private fun getIndexSelectedMenu(): Int {
        when (mTypeRepeat) {
            RepeatType.DOES_NOT_REPEAT.name -> return 0
            RepeatType.DAILY.name -> return 1
            RepeatType.WEEKLY.name -> return 2
            RepeatType.MONTHLY.name -> return 3
            RepeatType.MORE_DAYS.name -> return 4
            RepeatType.MORE_WEEKS.name -> return 4
            RepeatType.MORE_MONTH.name -> return 4
        }
        return 0
    }

    private fun hasChangeValue(): Boolean {
        binding.apply {
            val hasChangeDate = (if (note?.dateReminder != null) convertLongToDate(
                note?.dateReminder!!,
                FORMAT_DATE_REMINDER
            ) else getString(R.string.none)) != tvDateValue.text
            val hasChangeTime = (if (note?.dateReminder != null) convertLongToDate(
                note?.dateReminder!!,
                FORMAT_DATE_REMINDER_TIME
            ) else getString(R.string.none)) != tvReminderTimeValue.text
            val hasChangeRepeat =
                (note?.typeRepeat ?: getString(R.string.none)) != tvRepeatValue.text
            val hasChangeAlarm = note?.isAlarm != switchAlarmValue.isChecked
            return hasChangeDate || hasChangeTime || hasChangeRepeat || hasChangeAlarm
        }
    }

    private fun openCalendar() {
        context?.let { ct ->
            DatePickerDialog(ct, R.style.MyDatePickerStyle2, this, chosenYear, chosenMonth, chosenDay).show()
        }
    }

    private fun openTimePicker() {
        context?.let { ct ->
            TimePickerDialog(
                ct,
                this,
                chosenHour, chosenMin,
                true
            ).show()
        }
    }


    private fun onListener() {
        binding.apply {
            tvBack.setOnClickListener {
                showDialog()
            }

            tvDateValue.setOnClickListener {
                openCalendar()
            }

            tvReminderTimeValue.setOnClickListener {
                openTimePicker()
            }

            tvRepeatValue.setOnClickListener {
                showPopupMenu()
            }

            switchAlarmValue.setOnCheckedChangeListener { buttonView, isChecked ->
            }

            tvButtonSave.setOnClickListener {
                saveTimeUpdate()
            }

            tvButtonCancel.setOnClickListener {
                showDialog()
            }
        }
    }

    private fun showPopupMenu() {
        lifecycleScope.launch {
            val mContext: Context = ContextThemeWrapper(activity, R.style.BasePopupMenu)
            val popupMenu = PopupMenu(mContext, binding.tvRepeatValue, Gravity.CENTER)
            popupMenu.inflate(R.menu.menu_repeat)
            popupMenu.setOnMenuItemClickListener(this@ReminderFragment)
            val item: MenuItem = popupMenu.menu.getItem(getIndexSelectedMenu())
            context?.let { item.setColorSelect(it) }
            popupMenu.show()
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.doesNotRepeat -> {
                binding.tvRepeatValue.text = getString(R.string.doesNotRepeat)
                mTypeRepeat = RepeatType.DOES_NOT_REPEAT.name
                mValueRepeat = 1
                return true
            }

            R.id.daily -> {
                binding.tvRepeatValue.text = getString(R.string.daily)
                mTypeRepeat = RepeatType.DAILY.name
                mValueRepeat = 1
                return true
            }

            R.id.weekly -> {
                binding.tvRepeatValue.text = getString(R.string.weekly)
                mTypeRepeat = RepeatType.WEEKLY.name
                mValueRepeat = 1
                return true
            }

            R.id.monthly -> {
                binding.tvRepeatValue.text = getString(R.string.monthly)
                mTypeRepeat = RepeatType.MONTHLY.name
                mValueRepeat = 1
                return true
            }

            R.id.custom -> {
                val initType = when (mTypeRepeat) {
                    RepeatType.MORE_WEEKS.name -> RepeatType.MORE_WEEKS.name
                    RepeatType.MORE_MONTH.name -> RepeatType.MORE_MONTH.name
                    RepeatType.MORE_DAYS.name -> RepeatType.MORE_DAYS.name
                    else -> null
                }
                context?.showAlertDialogRepeatDaily(initType, mValueRepeat, onClickView = {}) { value, type ->
                    mTypeRepeat = type
                    mValueRepeat = value
                    binding.tvRepeatValue.text = getRepeatValue(value)
                }
                return true
            }

            else -> return false
        }
    }

    private fun showDialog() {
        if (hasChangeValue()) {
            context?.showAlertDialogBackOnReminder {
                navController?.popBackStack()
            }
        } else {
            navController?.popBackStack()
        }
    }

    private fun onBackPressHandle() {
        activity?.onBackPressedDispatcher?.addCallback(this, true) {
            showDialog()
        }
    }

    private fun saveTimeUpdate() {
        val dateUpdate = getDateUpdate()
        val isRepeat = mTypeRepeat != RepeatType.DOES_NOT_REPEAT.name
        val mIsAlarm = binding.switchAlarmValue.isChecked

        val getPeriodicTime: Long = when (mTypeRepeat) {
            RepeatType.DAILY.name -> RepeatType.DAILY.value
            RepeatType.WEEKLY.name -> RepeatType.WEEKLY.value
            RepeatType.MONTHLY.name -> RepeatType.MONTHLY.value
            RepeatType.MORE_DAYS.name -> RepeatType.MORE_DAYS.value * mValueRepeat
            RepeatType.MORE_WEEKS.name -> RepeatType.MORE_WEEKS.value * mValueRepeat
            RepeatType.MORE_MONTH.name -> RepeatType.MORE_MONTH.value * mValueRepeat
            else -> 0
        }

        if (mIsAlarm) {
            setReminderTime(isRepeat, getPeriodicTime)
        } else {
            closeReminder(note?.uniqueWorkName() ?: "")
            activity?.let { act ->
                Toast(context).showCustomToast(
                    act,
                    context?.getString(R.string.noteHasNotBeenReminded).toString()
                )
            }
        }

        note?.apply {
            dateReminder = dateUpdate
            typeRepeat = mTypeRepeat
            repeatValue = mValueRepeat
            isAlarm = mIsAlarm
        }?.let { viewModelTextNote.updateNote(it) {} }
        navController?.popBackStack()
    }

    private fun closeReminder(uniqueWorkName: String) {
        context?.let { WorkManager.getInstance(it).cancelUniqueWork(uniqueWorkName) }
    }

    private fun setReminderTime(isRepeat: Boolean? = false, periodicTime: Long = 0) {

        val dateUpdate = getDateUpdate()

        val currentTime = getCurrentTimeToLong()

        val delayInSeconds = dateUpdate?.minus(currentTime)?.div(1000)

        if (isRepeat == true) {
            createPeriodicWorkRequest(
                delayInSeconds ?: 0,
                periodicTime = periodicTime
            )
        } else {
            createOneTimeWorkRequest(delayInSeconds ?: 0)
        }
    }

    private fun getDateUpdate(): Long? {
        var date = ""
        var time = ""
        binding.tvDateValue.text.toString().let {
            date = if (it != getString(R.string.none)) it else convertLongToDate(
                getCurrentTimeToLong(),
                FORMAT_DATE_REMINDER
            )!!
        }
        binding.tvReminderTimeValue.text.toString().let {
            time = if (it != getString(R.string.none)) it else convertLongToDate(
                getCurrentTimeToLong(),
                FORMAT_DATE_REMINDER_TIME
            )!!
        }
        return convertDateStringToLong("$date $time", FORMAT_DATE_UPDATE)
    }

    private fun createPeriodicWorkRequest(
        timeDelayInSeconds: Long,
        periodicTime: Long
    ) {
        val periodWork = PeriodicWorkRequest.Builder(
            ReminderWorkerNotification::class.java,
            periodicTime,
            TimeUnit.MINUTES
        )
                .setInitialDelay(timeDelayInSeconds, TimeUnit.SECONDS)
                .addTag(note?.uniqueWorkName() ?: "")
                .setConstraints(constraints)
                .setInputData(
                    workDataOf(
                        ID to note?.ids,
                        TITTLE to note?.title,
                        MESSAGE to getContentNotification(note),
                        TYPE_ITEM to note?.typeItem,
                        TYPE_WORKER to CHANNEL_ID_PERIOD_WORK
                    )
                )
                .build()
        context?.let {
            WorkManager.getInstance(it).enqueueUniquePeriodicWork(
                note?.uniqueWorkName() ?: "",
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                periodWork
            )
        }
        activity?.let { act ->
            Toast(context).showCustomToast(
                act,
                context?.getString(R.string.noteHasBeenReminded).toString()
            )
        }
    }

    private fun getContentNotification(model: NoteModel?): String {
        return if (model?.typeItem == TypeItem.TEXT.name) model.content
                ?: "" else context?.getString(R.string.checklistLabel).plus("[")
                .plus(model?.listCheckList?.firstOrNull()?.body ?: "").plus(",...]")
    }

    private fun createOneTimeWorkRequest(timeDelayInSeconds: Long) {
        val onetimeWork = OneTimeWorkRequest.Builder(ReminderWorkerNotification::class.java)
                .setInitialDelay(timeDelayInSeconds, TimeUnit.SECONDS)
                .setConstraints(constraints)
                .addTag(note?.uniqueWorkName() ?: "")
                .setInputData(
                    workDataOf(
                        ID to note?.ids,
                        TITTLE to note?.title,
                        MESSAGE to getContentNotification(note),
                        TYPE_ITEM to note?.typeItem,
                        TYPE_WORKER to CHANNEL_ID_ONE_TIME_WORK
                    )
                )
                .build()

        context?.let {
            WorkManager.getInstance(it).enqueueUniqueWork(
                note?.uniqueWorkName() ?: "",
                ExistingWorkPolicy.REPLACE, onetimeWork
            )
        }
        activity?.let { act ->
            Toast(context).showCustomToast(
                act,
                context?.getString(R.string.noteHasBeenReminded).toString()
            )
        }
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        val hourFormat = hourOfDay.formatTime()
        val minuteFormat = minute.formatTime()
        binding.tvReminderTimeValue.text = "$hourFormat:$minuteFormat"
        chosenHour = hourFormat.toInt()
        chosenMin = hourFormat.toInt()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val monthFormat = (month + 1).formatTime()
        val dayFormat = dayOfMonth.formatTime()
        binding.tvDateValue.text = "$year-${monthFormat}-$dayOfMonth"
        chosenYear = year
        chosenMonth = monthFormat.toInt()
        chosenDay = dayFormat.toInt()
    }

    companion object {
    }

}