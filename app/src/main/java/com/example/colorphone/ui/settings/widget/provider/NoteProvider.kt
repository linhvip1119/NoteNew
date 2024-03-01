package com.example.colorphone.ui.settings.widget.provider

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import com.example.colorphone.R
import com.example.colorphone.model.NoteModel
import com.example.colorphone.repository.NoteRepository
import com.example.colorphone.ui.settings.widget.remoteService.WidgetService
import com.example.colorphone.util.Const
import com.example.colorphone.util.Const.DELETE_NOTE_WIDGET
import com.example.colorphone.util.Const.KEY_ID_NOTE_ADD_WIDGET
import com.example.colorphone.util.DataState
import com.example.colorphone.util.PrefUtil
import com.example.colorphone.util.RequestPinWidget
import com.example.colorphone.util.TypeItem
import com.example.colorphone.util.ext.convertLongToDateYYMMDD
import com.wecan.inote.util.mapIdColorWidget
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class NoteProvider : AppWidgetProvider() {

    @Inject
    lateinit var prefUtil: PrefUtil

    @Inject
    lateinit var noteRepository: NoteRepository

    private var idNote: Int = -1

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val listIds = appWidgetManager.getAppWidgetIds(ComponentName(context, this::class.java))
        prefUtil.newIdNoteWidget = listIds.lastOrNull() ?: -1
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        idNote = intent?.getIntExtra(KEY_ID_NOTE_ADD_WIDGET, -1) ?: -1
        if (idNote != -1) {
            CoroutineScope(Dispatchers.Main).launch {
                RequestPinWidget.publishEventNote(true)
            }
            context?.let { updateAppWidget(it, idNote) }
        }
    }

    private fun updateAppWidget(context: Context, idNote: Int, actionUpdate: String? = Const.UPDATE_NOTE_WIDGET) {
        val views = RemoteViews(context.packageName, R.layout.item_note_widget)
        getDataCustomizeWithId(idNote) { noteModel ->
            views.setTextViewText(R.id.tvTittleWidget, noteModel.title)
            views.setTextViewText(R.id.tvTittleWidget31, noteModel.title)
            views.setViewVisibility(
                R.id.tvTittleWidget,
                if (Build.VERSION.SDK_INT >= 31) View.GONE else View.VISIBLE
            )
            views.setViewVisibility(
                R.id.tvTittleWidget31,
                if (Build.VERSION.SDK_INT >= 31) View.VISIBLE else View.GONE
            )

            views.setTextViewText(
                R.id.tvDateWidget,
                convertLongToDateYYMMDD(noteModel.modifiedTime ?: 0)
            )

            mapIdColorWidget(noteModel.typeColor) { idColorBody, idIcon ->
                views.setInt(R.id.ivColorWidget, "setBackgroundResource", idIcon)
                views.setInt(R.id.llBodyWidget, "setBackgroundResource", idColorBody)
            }

            if (noteModel.typeItem == TypeItem.TEXT.name) {
                views.setTextViewText(R.id.tvContentWidget, noteModel.content)
                views.setViewVisibility(R.id.tvContentWidget, View.VISIBLE)
                views.setViewVisibility(R.id.llCheckListWidget, View.GONE)
            } else {
                views.setViewVisibility(R.id.tvContentWidget, View.GONE)
                views.setViewVisibility(R.id.llCheckListWidget, View.VISIBLE)

                val serviceIntent = Intent(context, WidgetService::class.java)
                serviceIntent.putExtra(Const.ID_NOTE_CHECKLIST_WIDGET, noteModel.ids ?: 999)
                serviceIntent.data = Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME))
                views.setRemoteAdapter(
                    R.id.llCheckListWidget, serviceIntent
                )
                context.startService(serviceIntent)
            }
            if (actionUpdate == DELETE_NOTE_WIDGET) {
                views.setOnClickPendingIntent(
                    R.id.llBodyWidget,
                    setMyActionUpdate(context, noteModel)
                )
            } else {
                views.setOnClickPendingIntent(
                    R.id.llBodyWidget,
                    setMyAction(context, noteModel)
                )
            }

            AppWidgetManager.getInstance(context).updateAppWidget(prefUtil.newIdNoteWidget, views)
            prefUtil.newIdNoteWidget = -1
        }
    }

    private fun getDataCustomizeWithId(ids: Int, data: (NoteModel) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            noteRepository.getNoteWithIds(ids).collect {
                withContext(Dispatchers.Main) {
                    when (it) {
                        is DataState.Success -> {
                            it.data.getOrNull(0)?.let { it1 -> data.invoke(it1) }
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun setMyAction(context: Context?, noteModel: NoteModel): PendingIntent? {
        val myPendingIntent = context?.let {
            val desFragment = R.id.mainFragment
            NavDeepLinkBuilder(it)
                    .setGraph(R.navigation.nav_graph)
                    .setDestination(
                        desFragment,
                        bundleOf(
                            "ARG_CREATE_NOTE" to false,
                            "ARG_FROM_WIDGET" to true,
                            "KEY_IDS_NOTE_FROM_WIDGET" to noteModel.ids,
                            "KEY_TYPE_ITEM_FROM_WIDGET" to noteModel.typeItem
                        )
                    )
                    .createPendingIntent()
        }
        return myPendingIntent
    }

    private fun setMyActionUpdate(context: Context?, noteModel: NoteModel): PendingIntent? {
        val myPendingIntent = context?.let {
            val desFragment = R.id.editFragment
            NavDeepLinkBuilder(it)
                    .setGraph(R.navigation.nav_graph)
                    .setDestination(
                        desFragment,
                        bundleOf(
                            "ARG_CREATE_NOTE" to true,
                            "UPDATE_WIDGET_IDS" to noteModel.ids,
//                            "TYPE_ITEM_EDIT" to if (noteModel.typeItem == TypeItem.TEXT.name) TextFragment::class.java.name else CheckListFragment::class.java.name
                        )
                    )
                    .createPendingIntent()
        }
        return myPendingIntent
    }
}