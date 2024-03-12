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
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import com.bumptech.glide.request.target.AppWidgetTarget
import com.example.colorphone.R
import com.example.colorphone.model.NoteModel
import com.example.colorphone.repository.NoteRepository
import com.example.colorphone.ui.settings.widget.remoteService.WidgetService
import com.example.colorphone.ui.settings.widget.utils.RoundedCornersTransformation
import com.example.colorphone.util.Const
import com.example.colorphone.util.Const.ACTION_UPDATE_WIDGET_EDIT
import com.example.colorphone.util.Const.DELETE_NOTE_WIDGET
import com.example.colorphone.util.Const.POST_ID_NOTE_UPDATE_WIDGET
import com.example.colorphone.util.DataState
import com.example.colorphone.util.PrefUtil
import com.example.colorphone.util.RequestPinWidget
import com.example.colorphone.util.TypeItem
import com.example.colorphone.util.ext.convertLongToDateYYMMDD
import com.wecan.inote.util.mapIdColorWidget
import com.wecan.inote.util.px
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

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
//        val widgetManager = AppWidgetManager.getInstance(context?.applicationContext)
//        widgetManager.notifyAppWidgetViewDataChanged(
//            widgetManager.getAppWidgetIds(ComponentName(context?.applicationContext!!.packageName, NoteProvider::class.java.name)), R.id.llCheckListWidget
//        )
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        idNote = intent?.getIntExtra(POST_ID_NOTE_UPDATE_WIDGET, -1) ?: -1
        val action = intent?.getStringExtra(ACTION_UPDATE_WIDGET_EDIT)
        if (idNote != -1) {
            context?.let { updateAppWidget(it, idNote, action) }
        }
    }

    private fun updateAppWidget(context: Context, idNote: Int, actionUpdate: String? = Const.ADD_NOTE_WIDGET) {

        val views = RemoteViews(context.packageName, R.layout.item_note_widget)
        getDataCustomizeWithId(idNote) { noteModel ->

            views.initRemoteView(context, noteModel)

            val serviceIntent = Intent(context, WidgetService::class.java)
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, prefUtil.newIdNoteWidget)
            serviceIntent.data = Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME))
            views.setRemoteAdapter(R.id.llCheckListWidget, serviceIntent)

            if (actionUpdate == DELETE_NOTE_WIDGET) {
                views.setOnClickPendingIntent(
                    R.id.llBodyWidget, setMyActionUpdate(context, noteModel)
                )
            } else {
                views.setOnClickPendingIntent(
                    R.id.llBodyWidget, setMyAction(context, noteModel)
                )
            }

            if (actionUpdate == Const.ADD_NOTE_WIDGET) {
                CoroutineScope(Dispatchers.Main).launch {
                    if (noteModel.typeItem == TypeItem.CHECK_LIST.name) {
                        AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(prefUtil.getIdWidgetNote(idNote), R.id.llCheckListWidget)
                    }
                    AppWidgetManager.getInstance(context).updateAppWidget(prefUtil.newIdNoteWidget, views)
                    prefUtil.setIdWidgetNote(idNote, prefUtil.newIdNoteWidget)
                    prefUtil.newIdNoteWidget = -1
                    RequestPinWidget.publishEventNote(true)

                }
            } else {
                AppWidgetManager.getInstance(context).updateAppWidget(prefUtil.getIdWidgetNote(idNote), views)
                if (noteModel.typeItem == TypeItem.CHECK_LIST.name) {
                    AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(prefUtil.getIdWidgetNote(idNote), R.id.llCheckListWidget)
                }
                CoroutineScope(Dispatchers.Main).launch {
                    RequestPinWidget.publishUpdateNote(true)
                }
            }
        }
    }

    private fun RemoteViews.initRemoteView(context: Context, noteModel: NoteModel) {

        this.setTextViewText(R.id.tvTittleWidget, noteModel.title)

        this.setTextViewText(R.id.tvTittleWidget31, noteModel.title)

        this.setViewVisibility(
            R.id.tvTittleWidget, if (Build.VERSION.SDK_INT < 31) View.VISIBLE else View.GONE
        )

        this.setViewVisibility(
            R.id.tvTittleWidget31, if (Build.VERSION.SDK_INT >= 31) View.VISIBLE else View.GONE
        )

        this.setTextViewText(
            R.id.tvDateWidget, convertLongToDateYYMMDD(noteModel.modifiedTime ?: 0)
        )

        mapIdColorWidget(noteModel.typeColor) { idColorBody, idIcon ->
            this.setInt(R.id.ivColorWidget, "setBackgroundResource", idIcon)

            if (noteModel.background != null) {
                val appWidgetTarget =
                    AppWidgetTarget(context, R.id.ivBgWidget, this, prefUtil.getIdWidgetNote(noteModel.ids!!))

                Glide.with(context)
                        .asBitmap()
                        .load(noteModel.background)
                        .override(120.px, 160.px)
                        .apply(
                            bitmapTransform(
                                RoundedCornersTransformation(
                                    16.px, 4,
                                    RoundedCornersTransformation.CornerType.ALL
                                )
                            )
                        )
                        .into(appWidgetTarget)
            } else {
                this.setViewVisibility(R.id.ivBgWidget, View.GONE)
                this.setInt(R.id.llBodyWidget, "setBackgroundResource", idColorBody)
            }
        }

        if (noteModel.typeItem == TypeItem.TEXT.name) {
            this.setTextViewText(R.id.tvContentWidget, noteModel.content)
            this.setViewVisibility(R.id.tvContentWidget, View.VISIBLE)
            this.setViewVisibility(R.id.llCheckListWidget, View.GONE)
        } else {
            this.setViewVisibility(R.id.tvContentWidget, View.GONE)
            this.setViewVisibility(R.id.llCheckListWidget, View.VISIBLE)
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
            NavDeepLinkBuilder(it).setGraph(R.navigation.nav_graph).setDestination(
                desFragment, bundleOf(
                    Const.ID_NAVIGATE_EDIT_FROM_ITEM_WIDGET to noteModel.ids,
                    Const.TYPE_ITEM_EDIT to noteModel.typeItem
                )
            ).createPendingIntent()
        }

        return myPendingIntent
    }

    private fun setMyActionUpdate(context: Context?, noteModel: NoteModel): PendingIntent? {
        val myPendingIntent = context?.let {
            val desFragment = R.id.editFragment
            NavDeepLinkBuilder(it).setGraph(R.navigation.nav_graph).setDestination(
                desFragment, bundleOf(
                    "ARG_CREATE_NOTE" to true,
                    "UPDATE_WIDGET_IDS" to noteModel.ids,
//                            "TYPE_ITEM_EDIT" to if (noteModel.typeItem == TypeItem.TEXT.name) TextFragment::class.java.name else CheckListFragment::class.java.name
                )
            ).createPendingIntent()
        }
        return myPendingIntent
    }
}