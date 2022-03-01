package com.example.app
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
interface ApiService {
    //Request to save Bitcoin limit
    @POST("/btc-pref")
    fun saveBTCLimit(@Body body: RequestBody): Call<String>

    //Request to save Ethereum limit
    @POST("/eth-pref")
    fun saveETHLimit(@Body body: RequestBody): Call<String>

    //Request to get values
    @GET("/fetch-values")
    fun getValues():Call<String>
}