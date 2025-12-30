package com.example.smarthallmonitor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class BlockActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block);

        String blockId = getIntent().getStringExtra("BLOCK_ID");
        if(blockId == null) blockId = "b1"; // Fallback

        TextView title = findViewById(R.id.txtBlockTitle);
        TextView txtRoomA = findViewById(R.id.txtRoomA);
        TextView txtRoomB = findViewById(R.id.txtRoomB);
        CardView btnRoomA = findViewById(R.id.btnRoomA);
        CardView btnRoomB = findViewById(R.id.btnRoomB);

        final String roomA_ID, roomB_ID;

        // DYNAMIC LOGIC
        if ("b1".equals(blockId)) {
            title.setText("Block 1");
            txtRoomA.setText("Room 101");
            txtRoomB.setText("Room 102");
            roomA_ID = "101";
            roomB_ID = "102";
        } else {
            title.setText("Block 2");
            txtRoomA.setText("Room 201");
            txtRoomB.setText("Room 202");
            roomA_ID = "201";
            roomB_ID = "202";
        }

        // CLICK LISTENERS
        btnRoomA.setOnClickListener(v -> openRoom(roomA_ID));
        btnRoomB.setOnClickListener(v -> openRoom(roomB_ID));
    }

    private void openRoom(String roomId) {
        Intent intent = new Intent(BlockActivity.this, RoomActivity.class);
        intent.putExtra("ROOM_ID", roomId);
        startActivity(intent);
    }
}