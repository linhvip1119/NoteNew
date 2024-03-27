package com.example.colorphone.adsConfig

import android.os.Bundle
import androidx.fragment.app.Fragment


interface FragmentLifecycleListener {
    fun onFragmentPause(fragment: Fragment)
    fun onFragmentCreated(fragment: Fragment, savedInstanceState: Bundle?)
    fun onFragmentResumed(fragment: Fragment)
    // Thêm các phương thức khác tùy theo nhu cầu của bạn
}