package com.example.smarthallmonitor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Block 1
        findViewById(R.id.btnBlock1).setOnClickListener(v -> {
            Intent i = new Intent(this, BlockActivity.class);
            i.putExtra("BLOCK_ID", "b1");
            startActivity(i);
        });

        // 2. Block 2
        findViewById(R.id.btnBlock2).setOnClickListener(v -> {
            Intent i = new Intent(this, BlockActivity.class);
            i.putExtra("BLOCK_ID", "b2");
            startActivity(i);
        });

        // 3. View Master Schedule
        findViewById(R.id.btnViewMaster).setOnClickListener(v ->
                startActivity(new Intent(this, ScheduleActivity.class))
        );
    }
}