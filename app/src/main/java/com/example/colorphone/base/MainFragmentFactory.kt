package com.example.colorphone.base

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.bumptech.glide.RequestManager
import com.example.colorphone.util.PrefUtil
import javax.inject.Inject


class MainFragmentFactory @Inject constructor(
    private val glide: RequestManager,
    private val preUtil: PrefUtil
) : FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            else -> super.instantiate(classLoader, className)
        }
    }
}