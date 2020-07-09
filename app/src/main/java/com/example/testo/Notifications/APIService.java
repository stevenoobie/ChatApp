package com.example.testo.Notifications;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
  @Headers(
          {
                  "Content-Type:application/json",
                  "Authorization:key=AAAADtmcy18:APA91bHiufijRVuW_zUvdx133pUMjophARqjgbhbnuBuEODgmnQQcxM7r6BW2LjieArWofxX92SuG_IS_nKCyxSMgoRZ5cjOCxSV4q6gXbS1EREmvpxqOoPNFjp0BOODFceGScFvHpTh"
          }
  )
    @POST("fcm/send")
    Call<MyResponce> sendNotification(@Body Sender body);
}
