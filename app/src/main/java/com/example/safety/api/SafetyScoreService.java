package com.example.safety.api;


import com.example.safety.model.SafetyScoreRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface SafetyScoreService {
    @POST("/sentiment_score")
    Call<ResponseBody> getSafetyScore(@Body SafetyScoreRequest request);
}
