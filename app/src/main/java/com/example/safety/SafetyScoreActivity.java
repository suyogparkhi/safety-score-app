package com.example.safety;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.safety.api.ApiClient;
import com.example.safety.api.SafetyScoreService;
import com.example.safety.model.SafetyScoreRequest;
import com.example.safety.utils.PermissionUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SafetyScoreActivity extends AppCompatActivity {

    private static final String TAG = "SafetyScoreActivity";
    private TextView tvSafetyScore, tvAdditionalInfo;
    private ProgressBar progressBar;
    private LocationManager locationManager;
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safety_score);

        tvSafetyScore = findViewById(R.id.tvSafetyScore);
        tvAdditionalInfo = findViewById(R.id.tvAdditionalInfo);
        progressBar = findViewById(R.id.progressBar);

        // Check permissions and get location
        PermissionUtils.checkLocationPermission(this, () -> {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        });
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            fetchCurrentDateTimeAndScore();
        }
    };

    private void fetchCurrentDateTimeAndScore() {
        // Display loading animation
        progressBar.setVisibility(View.VISIBLE);
        tvAdditionalInfo.setText("Calculating safety score...");

        LocalDateTime currentDateTime = LocalDateTime.now();
        double day = currentDateTime.getDayOfMonth();
        double month = currentDateTime.getMonthValue();
        double hour = currentDateTime.getHour();
        double minute = currentDateTime.getMinute();

        Log.d(TAG, "Latitude: " + latitude);
        Log.d(TAG, "Longitude: " + longitude);
        Log.d(TAG, "Date: " + day + "/" + month);
        Log.d(TAG, "Time: " + hour + ":" + minute);

        fetchSafetyScore(hour, minute, latitude, longitude, day, month);
    }

    private void fetchSafetyScore(double hour, double minute, double lat, double lon, double day, double month) {
        String review = hour + "," + minute + "," + lat + "," + lon + "," + day + "," + month;
        SafetyScoreService service = ApiClient.getApiClient().create(SafetyScoreService.class);
        SafetyScoreRequest request = new SafetyScoreRequest(review);

        service.getSafetyScore(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        double score = jsonObject.getDouble("score");
                        tvSafetyScore.setText("Safety Score: " + Math.round(score * 100));
                        tvAdditionalInfo.setText("Data retrieved successfully!");
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                        tvSafetyScore.setText("Error parsing response.");
                        tvAdditionalInfo.setText("Please try again.");
                    }
                } else {
                    tvSafetyScore.setText("Failed to fetch safety score.");
                    tvAdditionalInfo.setText("Ensure network connectivity.");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "API call failed", t);
                tvSafetyScore.setText("Failed to fetch safety score.");
                tvAdditionalInfo.setText("Network error.");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }
}