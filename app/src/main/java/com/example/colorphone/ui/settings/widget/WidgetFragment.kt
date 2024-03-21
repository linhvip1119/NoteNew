package com.example.colorphone.ui.settings.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.colorphone.R
import com.example.colorphone.base.BaseFragment
import com.example.colorphone.databinding.FragmentWidgetBinding
import com.example.colorphone.databinding.ItemCheckListBinding
import com.example.colorphone.model.NoteModel
import com.example.colorphone.ui.settings.widget.provider.ToolsWidgetProvider
import com.example.colorphone.util.Const
import com.example.colorphone.util.RequestPinWidget
import com.example.colorphone.util.TypeItem
import com.example.colorphone.util.ext.convertLongToDateYYMMDD
import com.example.colorphone.util.ext.loadUrl
import com.wecan.inote.util.changeBackgroundColor
import com.wecan.inote.util.gone
import com.wecan.inote.util.mapIdColor
import com.wecan.inote.util.mapIdColorWidget
import com.wecan.inote.util.show
import com.wecan.inote.util.showCustomToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
@AndroidEntryPoint
class WidgetFragment : BaseFragment<FragmentWidgetBinding>(FragmentWidgetBinding::inflate) {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun init(view: View) {
        onListener()
        viewModelTextNote.getFirstData()
        check("Widget_Show")
    }

    override fun onSubscribeObserver(view: View) {
        viewModelTextNote.firstItemLD.observe(viewLifecycleOwner) {
            bindViewItemNote(it)
        }
    }

    private fun bindViewItemNote(item: NoteModel?) {
        binding.iclItem.apply {
            item?.typeColor?.let {
                rootLayout.changeBackgroundColor(R.color.neutral100)
                mapIdColor(it) { _, _, idColor, idColorBody, _ ->
                    if (item.background != null) {
                        llBody.setBackgroundColor(Color.TRANSPARENT)
                        ivBg.loadUrl(item.background!!)
                    } else {
                        llBody.changeBackgroundColor(idColorBody)
                    }
                    tvTittle.compoundDrawableTintList =
                        ContextCompat.getColorStateList(root.context, idColor)
                }
                mapIdColorWidget(it) { _, idIcon ->
                    ivColorWidget.setBackgroundResource(idIcon)
                }
            }
            ivPinned.isVisible = item?.isPinned == true
            tvTittle.text = item?.title
            tvDate.text = convertLongToDateYYMMDD(item?.dateCreateNote ?: 0)
            if (item?.typeItem == TypeItem.TEXT.name) {
                tvContent.text = item.content
                tvContent.show()
                llCheckList.gone()
            } else {
                tvContent.gone()
                llCheckList.show()
                item?.listCheckList?.take(4)?.forEach {
                    val binding = ItemCheckListBinding.inflate(LayoutInflater.from(root.context))
                    binding.apply {
                        ivCheckBox.setImageResource(if (it.checked) R.drawable.ic_checkbox_true_15dp else R.drawable.ic_checkbox_false_15dp)
                        editText.text = it.body
                    }
                    llCheckList.addView(binding.root)
                }
            }
        }
    }

    private fun onListener() {
        binding.apply {
            llItem.setOnClickListener {
                check("Widget_StickyNote_Click")
                handleClickItem(true, !llItem.isSelected)
            }
            ivWidgetBarr.setOnClickListener {
                check("Widget_NoteTool_Click")
                handleClickItem(false, !ivWidgetBarr.isSelected)
            }
            tvWidgetBack.setOnClickListener {
                check("Widget_Back_Click")
                navController?.popBackStack()
            }

            tvAddToHome.setOnClickListener {
                if (llItem.isSelected) {
                    navigationWithAnim(R.id.action_widgetFragment_to_ltNoteAddWidget)
                } else {
                    check("Widget_AddToHome_Click")
                    addWidgetTools()
                }
            }
        }
    }

    private fun addWidgetTools() {
        addWidget()
        initJobAddPackage()
    }

    private fun addWidget(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            context?.let { ct ->
                val appWidgetManager = AppWidgetManager.getInstance(ct)
                val myProvider = ComponentName(ct, ToolsWidgetProvider::class.java)
                if (appWidgetManager != null && appWidgetManager.isRequestPinAppWidgetSupported) {
                    val intent = Intent(activity, ToolsWidgetProvider::class.java)
                    intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    intent.putExtra(Const.KEY_ADD_WIDGET_SUCCESS, true)
                    val successCallback = PendingIntent.getBroadcast(
                        context,
                        1,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                    appWidgetManager.requestPinAppWidget(myProvider, null, successCallback)
                    return true
                }
            }
        }
        return false
    }

    private var jobAddPackage: Job? = null

    private fun initJobAddPackage() {
        if (jobAddPackage?.isActive == true) {
            jobAddPackage?.cancel()
        }
        jobAddPackage = lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                RequestPinWidget.toolWidgetSuccess
                        .filter { state -> state }
                        .take(1)
                        .asLiveData(viewLifecycleOwner.lifecycleScope.coroutineContext)
                        .observe(viewLifecycleOwner) {
                            context?.let { it1 -> Toast(context).showCustomToast(it1, getString(R.string.addToolSuccess)) }
                            check("Widget_AddToHome_Success_Click")
                        }
            }
        }
    }

    private fun handleClickItem(isSelectedWidgetBar: Boolean, statusSelected: Boolean) {
        binding.apply {
            lifecycleScope.launch {
                llItem.isSelected = if (isSelectedWidgetBar) statusSelected else false
                ivWidgetBarr.isSelected = if (!isSelectedWidgetBar) statusSelected else false
                tvAddToHome.isSelected = statusSelected
                tvAddToHome.isEnabled = statusSelected
                setBackGround()
            }
        }

    }

    private fun setBackGround() {
        binding.apply {
            context?.let { ct ->
                llItem.background = if (llItem.isSelected) ContextCompat.getDrawable(
                    ct, R.drawable.bg_item_widget_selected
                ) else ContextCompat.getDrawable(ct, R.drawable.bg_item_widget)

                tvNoteTool.setTextColor(ContextCompat.getColor(requireContext(), if (llItem.isSelected) R.color.neutral200 else R.color.primary_current))

                ivWidgetBarr.background = if (ivWidgetBarr.isSelected) ContextCompat.getDrawable(
                    ct, R.drawable.bg_item_widget_selected
                ) else ContextCompat.getDrawable(ct, R.drawable.bg_item_widget)


                tvStickyNote.setTextColor(ContextCompat.getColor(requireContext(), if (!llItem.isSelected) R.color.neutral200 else R.color.primary_current))

                tvAddToHome.background = if (tvAddToHome.isSelected) ContextCompat.getDrawable(
                    ct, R.drawable.bg_button_add_to_home_selected
                ) else ContextCompat.getDrawable(ct, R.drawable.bg_button_add_to_home)
            }
        }
    }

}