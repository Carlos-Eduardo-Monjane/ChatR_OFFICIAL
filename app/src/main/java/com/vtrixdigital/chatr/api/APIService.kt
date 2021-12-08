package com.vtrixdigital.chatr.api

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface APIService {
    @POST
    suspend fun testApiWithHeaders(@Url url : String, @HeaderMap headers: Map<String, String> , @Body requestBody: RequestBody): Response<ResponseBody>

    @POST
    suspend fun testApiWithOutHeaders(@Url url : String,  @Body requestBody: RequestBody): Response<ResponseBody>
}