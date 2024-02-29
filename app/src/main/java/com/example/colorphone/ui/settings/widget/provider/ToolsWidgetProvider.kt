package com.example.colorphone.ui.settings.widget.provider

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import com.example.colorphone.R
import com.example.colorphone.util.Const
import com.example.colorphone.util.PrefUtil
import com.example.colorphone.util.RequestPinWidget
import com.example.colorphone.util.TypeColorNote
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ToolsWidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var prefUtil: PrefUtil
    override fun onUpdate(
        context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray
    ) {
        updateBarWidget(context)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.getBooleanExtra(Const.KEY_ADD_WIDGET_SUCCESS, false) == true) {
            CoroutineScope(Dispatchers.Main).launch {
                RequestPinWidget.publishEventTools(true)
            }
        }
        super.onReceive(context, intent)
    }

    private fun updateBarWidget(context: Context) {
        val views = RemoteViews(context.packageName, R.layout.new_app_widget)
        views.setOnClickPendingIntent(R.id.ivText, setMyAction(TEXT_FM, context))
        views.setOnClickPendingIntent(R.id.icCheckList, setMyAction(CL_FM, context))
        views.setOnClickPendingIntent(R.id.ivSetting, setMyAction(SETTING_FM, context))
        views.setInt(R.id.llBarWidget, "setBackgroundResource", getBackgroundWidgetBar())
        val thisWidget = ComponentName(context, ToolsWidgetProvider::class.java)
        AppWidgetManager.getInstance(context)?.updateAppWidget(thisWidget, views)
    }

    private fun setMyAction(idFm: Int, context: Context?): PendingIntent? {
        val myPendingIntent = context?.let {
            val desFragment = R.id.mainFragment
            val nameScreen = when (idFm) {
                TEXT_FM -> Const.TEXT_SCREEN
                CL_FM -> Const.CHECK_LIST_SCREEN
                else -> Const.SETTING_SCREEN
            }
            NavDeepLinkBuilder(it).setGraph(R.navigation.nav_graph).setDestination(
                desFragment, bundleOf(
                    "ARG_CREATE_NOTE" to true,
                    "TYPE_ITEM_EDIT" to nameScreen,
                )
            ).createPendingIntent()
        }
        return myPendingIntent
    }

    private fun getBackgroundWidgetBar(): Int {
        return when (prefUtil.themeColor) {
            TypeColorNote.F_PRIMARY.name -> R.drawable.bg_bar_widget_primary
            TypeColorNote.BLUE.name -> R.drawable.bg_bar_widget_blue
            TypeColorNote.A_ORANGE.name -> R.drawable.bg_bar_widget_orange
            TypeColorNote.D_RED.name -> R.drawable.bg_bar_widget_red
            TypeColorNote.B_GREEN.name -> R.drawable.bg_bar_widget_green
            else -> 0
        }
    }

    companion object {
        const val ACTION_REFRESH = "actionRefresh"
        const val TEXT_FM = 1
        const val CL_FM = 2
        const val CALENDAR_FM = 3
        const val SETTING_FM = 4
        const val KEY_ID_FRAGMENT = "KEY_ID_FRAGMENT"
        const val KEY_NAVIGATE_TO_NOTE_DETAIL = "KEY_NAVIGATE_TO_NOTE_DETAIL"
    }
}