package com.example.colorphone.retrofit

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiRetrofit {
    @FormUrlEncoded
    @POST("sendmail.php")
    suspend fun sendmail(@Field("otp") otp: String, @Field("mailto") mail: String)
}