package com.example.colorphone.base

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.example.colorphone.ui.main.viewmodel.TextNoteViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseFragment<B : ViewBinding>(val inflate: Inflate<B>) : Fragment() {
    var navController: NavController? = null
    private lateinit var _binding: B
    val binding get() = _binding

    val viewModelTextNote: TextNoteViewModel by viewModels()


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

        return binding.root
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
}