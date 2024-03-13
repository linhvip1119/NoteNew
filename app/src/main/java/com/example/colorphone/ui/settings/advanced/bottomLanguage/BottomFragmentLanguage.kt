package com.example.colorphone.ui.settings.advanced.bottomLanguage

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.colorphone.R
import com.example.colorphone.databinding.FragmentBottomLanguageBinding
import com.example.colorphone.ui.bottomDialogColor.viewmodel.BottomSheetViewModel
import com.example.colorphone.util.Const
import com.example.colorphone.util.PrefUtil
import com.example.colorphone.util.PrefUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject


@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class BottomFragmentLanguage() : BottomSheetDialogFragment() {

    @Inject
    lateinit var prefUtil: PrefUtil

    private var _binding: FragmentBottomLanguageBinding? = null

    private lateinit var _adapter: BottomLanguageAdapter

    private val _noteTypeViewModel: BottomSheetViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBottomLanguageBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bottomSheet = view.parent as View
        bottomSheet.backgroundTintMode = PorterDuff.Mode.CLEAR
        bottomSheet.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
        setUpRv()
        initObserver()
        _noteTypeViewModel.apply {
            context?.getListLanguage()
        }
        Const.checking("Advanced_Language_Show")
    }

    private fun setUpRv() {
        _adapter = BottomLanguageAdapter {
            context?.let { it1 -> PrefUtils.languageApp(it1, it.name) }
            val text = "Advanced_Language_${it.name}_Click"
            Const.checking(text)
            dismiss()
            activity?.apply {
                finish()
                intent?.let { startActivity(it) }
            }
        }
        _adapter.currentLanguage = PrefUtils.languageApp(requireContext()) ?: getString(R.string.englishKey)
        _binding?.rvLanguage?.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = _adapter
        }
    }

    private fun initObserver() {
        _noteTypeViewModel.languageModelLiveData.observe(viewLifecycleOwner) {
            _adapter.submitList(it)
        }
    }

    companion object {
        fun newInstance(): BottomFragmentLanguage {
            return BottomFragmentLanguage()
        }
    }
}