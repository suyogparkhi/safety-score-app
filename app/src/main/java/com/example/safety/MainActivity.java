package com.example.safety;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button btnSafetyScore, btnHeatmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSafetyScore = findViewById(R.id.btnSafetyScore);
        btnHeatmap = findViewById(R.id.btnHeatmap);

        btnSafetyScore.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, SafetyScoreActivity.class));
        });

        btnHeatmap.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, HeatmapActivity.class));
        });
    }
}
