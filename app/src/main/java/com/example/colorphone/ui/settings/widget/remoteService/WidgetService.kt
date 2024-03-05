package com.example.colorphone.ui.settings.widget.remoteService

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.colorphone.R
import com.example.colorphone.model.CheckList
import com.example.colorphone.model.NoteModel
import com.example.colorphone.repository.NoteRepository
import com.example.colorphone.util.Const
import com.example.colorphone.util.Const.ID_NOTE_CHECKLIST_WIDGET
import com.example.colorphone.util.DataState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class WidgetService : RemoteViewsService() {

    @Inject
    lateinit var noteRepository: NoteRepository

    private var listData = arrayListOf<CheckList>()

    private var mIntentListener: BroadcastReceiver? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        mIntentListener = listenerChangeWindowManager
        registerReceiver(
            listenerChangeWindowManager, IntentFilter(Const.UPDATE_REMOTE_CHECK_LIST)
        )

        val i = intent?.getIntExtra(ID_NOTE_CHECKLIST_WIDGET, -1)
        i?.let {
            getDataCustomizeWithId(it) { note ->
                listData.clear()
                listData.addAll(note.listCheckList ?: listOf())
            }
        }
        return START_STICKY
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

    private val listenerChangeWindowManager: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val idExtra = intent.getIntExtra(ID_NOTE_CHECKLIST_WIDGET, -1)
            if (intent.action == Const.UPDATE_REMOTE_CHECK_LIST && idExtra != -1) {
                getDataCustomizeWithId(idExtra) {
                    listData.clear()
                    listData.addAll(it.listCheckList ?: listOf())
                }
            }
        }
    }

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return DataRemoteFactory(applicationContext, intent)
    }

    inner class DataRemoteFactory(val mContext: Context?, intent: Intent?) : RemoteViewsFactory {
        private var myListView: ArrayList<CheckList> = ArrayList()

        override fun onCreate() {
            try {
                initData(listData)
            } catch (_: Exception) {
            }
        }

        override fun onDataSetChanged() {
            initData(listData)
        }

        override fun onDestroy() {}
        override fun getCount(): Int {
            return myListView.size
        }

        override fun getViewAt(position: Int): RemoteViews {
            val view = RemoteViews(
                mContext?.packageName, R.layout.item_check_list
            )
            view.setInt(
                R.id.ivCheckBox, "setImageResource", if (myListView.getOrNull(position)?.checked == true) R.drawable.ic_checkbox_true_15dp else R.drawable.ic_checkbox_false_15dp
            )
            view.setTextViewText(R.id.editText, myListView.getOrNull(position)?.body)
            val fillInIntent = Intent()
            view.setOnClickFillInIntent(R.id.editText, fillInIntent)
            return view
        }

        override fun getLoadingView(): RemoteViews? {
            return null
        }

        override fun getViewTypeCount(): Int {
            return 1
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun hasStableIds(): Boolean {
            return true
        }

        private fun initData(list: ArrayList<CheckList>) {
            try {
                myListView.clear()
                list.forEach {
                    myListView.add(it)
                }
            } catch (_: Exception) {
            }
        }
    }
}