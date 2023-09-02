package com.bachhoangxuan.android.politicalpreparedness.network

import okhttp3.OkHttpClient

class CivicsHttpClient : OkHttpClient() {

    companion object {

        private const val API_KEY = "AIzaSyDpmDJEq7zapBgEzvs4v2XLW1lA3oGEDxo"

        fun getClient(): OkHttpClient {
            return Builder()
                .addInterceptor { chain ->
                    val original = chain.request()
                    val url = original
                        .url()
                        .newBuilder()
                        .addQueryParameter("key", API_KEY)
                        .build()
                    val request = original
                        .newBuilder()
                        .url(url)
                        .build()
                    chain.proceed(request)
                }
                .build()
        }
    }
}
