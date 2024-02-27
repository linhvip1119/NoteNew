//package com.example.colorphone.ui.main.listNote
//
//import androidx.core.os.bundleOf
//import com.example.colorphone.R
//import com.example.colorphone.util.Const
//import com.example.colorphone.util.TypeItem
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//
//@OptIn(ExperimentalCoroutinesApi::class)
//class ListCheckList : BaseListNote() {
//
//    companion object {
//        fun newInstance(): ListCheckList {
//            return ListCheckList()
//        }
//    }
//
//    override fun initAdapter() {
//        super.initAdapter()
//        mAdapterText.typeItem = TypeItem.CHECK_LIST.name
//
//    }
//
//    override fun loadData() {
//        super.loadData()
//        viewModelTextNote.getListTextNote(TypeItem.CHECK_LIST.name, prefUtil.sortType)
//
//    }
//
//    override fun navigateToEdit(idNote: Int?) {
//        navigationWithAnim(
//            R.id.editFragment, bundleOf(
//                Const.KEY_ID_DATA_NOTE to idNote, Const.TYPE_ITEM_EDIT to Const.TYPE_CHECKLIST
//            )
//        )
//    }
//}