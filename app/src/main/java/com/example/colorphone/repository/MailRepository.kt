package com.example.colorphone.repository

import com.example.colorphone.retrofit.ApiRetrofit

class MailRepository(private val apiRetrofit: ApiRetrofit) {
    suspend fun sendmail(otp: String, mail: String) {
        try {
            apiRetrofit.sendmail(otp, mail)
        } catch (e: Exception) {

        }
    }
}