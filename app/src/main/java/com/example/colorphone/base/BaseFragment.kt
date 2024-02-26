package com.example.colorphone.base

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.example.colorphone.ui.bottomDialogColor.ui.NoteBottomSheetDialog
import com.example.colorphone.ui.main.viewmodel.ListShareViewModel
import com.example.colorphone.ui.main.viewmodel.TextNoteViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseFragment<B : ViewBinding>(val inflate: Inflate<B>) : Fragment() {
    var navController: NavController? = null
    private lateinit var _binding: B
    val binding get() = _binding

    val viewModelTextNote: TextNoteViewModel by viewModels()
    val shareViewModel: ListShareViewModel by activityViewModels()
    private val navBuilder = NavOptions.Builder()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        onSubscribeObserver(view)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        navController = findNavController()
        _binding = inflate.invoke(inflater, container, false)
//        if (navController?.currentDestination?.id == R.id.themeFragment) {
//            if (viewPreview == null) {
//                viewPreview = binding.root
//                return viewPreview
//            } else {
//                return viewPreview
//            }
//        }
        navBuilder.setEnterAnim(android.R.anim.fade_in).setExitAnim(android.R.anim.fade_out)
                .setPopEnterAnim(android.R.anim.fade_in)
                .setPopExitAnim(android.R.anim.fade_out)
        navController = findNavController()
        return binding.root
    }

    fun navigationWithAnim(des: Int, bundle: Bundle? = null) {
        navController?.navigate(des, bundle, navBuilder.build())
    }

    fun putFragmentListener(key: String, bundle: Bundle = bundleOf()) {
        activity?.supportFragmentManager?.setFragmentResult(key, bundle)
    }

    fun getFragmentListener(key: String, callback: (Bundle) -> Unit) {
        activity?.supportFragmentManager?.setFragmentResultListener(key, viewLifecycleOwner) { _, result ->
            callback(result)
        }
    }

    abstract fun init(view: View)
    abstract fun onSubscribeObserver(view: View)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun isConnectedViaWifi(): Boolean {
        val connectivityManager =
            context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        val mMobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        return mWifi!!.isConnected || mMobile!!.isConnected
    }

    fun showBottomSheet(
        showDefaultColor: Boolean = true,
        currentColor: String? = null,
        fromScreen: String,
        colorClick: (String) -> Unit
    ) {
        val addPhotoBottomDialogFragment: NoteBottomSheetDialog =
            NoteBottomSheetDialog.newInstance(showDefaultColor, currentColor, fromScreen, colorClick)
        activity?.supportFragmentManager?.let {
            addPhotoBottomDialogFragment.show(
                it, "TAG"
            )
        }
    }
}