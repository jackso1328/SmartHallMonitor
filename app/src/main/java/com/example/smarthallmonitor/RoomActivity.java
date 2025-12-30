package com.example.smarthallmonitor;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class RoomActivity extends AppCompatActivity {

    // *** ENSURE THIS IS YOUR CURRENT IP ***
    private static final String LAPTOP_IP = "10.139.192.253";

    private String roomId, channelId, readKey;
    private TextView[] tempViews = new TextView[4];
    private TextView[] acViews = new TextView[4];
    private LineChart chart;
    private LinearLayout scheduleContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        roomId = getIntent().getStringExtra("ROOM_ID");
        configureRoom(roomId);

        ((TextView)findViewById(R.id.txtRoomTitle)).setText("Room " + roomId);

        // UI Binding
        tempViews[0] = findViewById(R.id.temp1);
        tempViews[1] = findViewById(R.id.temp2);
        tempViews[2] = findViewById(R.id.temp3);
        tempViews[3] = findViewById(R.id.temp4);

        acViews[0] = findViewById(R.id.ac1);
        acViews[1] = findViewById(R.id.ac2);
        acViews[2] = findViewById(R.id.ac3);
        acViews[3] = findViewById(R.id.ac4);

        chart = findViewById(R.id.trendChart);
        setupChart();

        scheduleContainer = findViewById(R.id.scheduleContainer);

        // BUTTONS
        findViewById(R.id.btnAddManual).setOnClickListener(v -> showAddClassDialog());

        // REFRESH BUTTON LISTENER
        findViewById(R.id.btnRefresh).setOnClickListener(v -> {
            Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show();
            fetchThingSpeakHistory();
            fetchSchedule();
        });

        // Initial Data Fetch
        fetchThingSpeakHistory();
        fetchSchedule();
    }

    // --- 1. CONFIGURATION ---
    private void configureRoom(String id) {
        if(id.equals("101")) { channelId="3209747"; readKey="TR9XFHVVTNWQTCPH"; }
        else if(id.equals("102")) { channelId="3213892"; readKey="IDFQECXSIM1ZMCHM"; }
        else if(id.equals("201")) { channelId="3213894"; readKey="18TYV1WH06TXAAWN"; }
        else if(id.equals("202")) { channelId="3213896"; readKey="V11EWNH7AYHS3BKM"; }
    }

    // --- 2. SENSORS & CHART ---
    private void fetchThingSpeakHistory() {
        String url = "https://api.thingspeak.com/channels/" + channelId + "/feeds.json?api_key=" + readKey + "&results=20";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray feeds = response.getJSONArray("feeds");
                        if(feeds.length() > 0) updateStatus(feeds.getJSONObject(feeds.length()-1));

                        ArrayList<Entry> entries = new ArrayList<>();
                        for(int i=0; i<feeds.length(); i++) {
                            JSONObject f = feeds.getJSONObject(i);
                            float val = (float) f.optDouble("field1", 0);
                            entries.add(new Entry(i, val));
                        }
                        LineDataSet set = new LineDataSet(entries, "Sensor 1");
                        set.setColor(Color.CYAN);
                        set.setLineWidth(2f);
                        set.setDrawCircles(false);
                        set.setDrawValues(false);
                        chart.setData(new LineData(set));
                        chart.invalidate();
                    } catch (Exception e) {}
                }, error -> {}
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void updateStatus(JSONObject feed) {
        for(int i=0; i<4; i++) {
            String t = feed.optString("field"+(i+1), "--");
            if(!t.equals("null")) tempViews[i].setText(t + "°C");
            String s = feed.optString("field"+(i+5), "0");
            acViews[i].setText(s.equals("1") ? "AC ON" : "AC OFF");
            acViews[i].setTextColor(s.equals("1") ? Color.GREEN : Color.RED);
        }
    }

    private void setupChart() {
        chart.getDescription().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setTextColor(Color.WHITE);
        chart.getAxisLeft().setTextColor(Color.WHITE);
        chart.getLegend().setTextColor(Color.WHITE);
    }

    // --- 3. SCHEDULE FETCH & DELETE LOGIC ---
    private void fetchSchedule() {
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String url = "http://" + LAPTOP_IP + ":5000/api/schedule?room=" + roomId + "&date=" + date;

        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    scheduleContainer.removeAllViews();
                    try {
                        if (response.length() == 0) {
                            TextView tv = new TextView(this);
                            tv.setText("No classes scheduled.");
                            tv.setTextColor(Color.LTGRAY);
                            scheduleContainer.addView(tv);
                        }
                        for(int i=0; i<response.length(); i++) {
                            JSONObject row = response.getJSONObject(i);
                            addScheduleRow(row.getString("Time"), row.getString("Subject"), date);
                        }
                    } catch(Exception e) {}
                }, error -> {}
        );
        Volley.newRequestQueue(this).add(req);
    }

    private void addScheduleRow(String time, String subject, String date) {
        // Create Row Layout
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 15, 0, 15);
        row.setBackgroundColor(Color.parseColor("#00000000")); // Transparent

        // Info Text
        TextView info = new TextView(this);
        info.setText(time + " : " + subject);
        info.setTextColor(Color.WHITE);
        info.setTextSize(16);
        info.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        // Delete Button (Icon)
        ImageView deleteBtn = new ImageView(this);
        deleteBtn.setImageResource(android.R.drawable.ic_menu_delete);
        deleteBtn.setColorFilter(Color.parseColor("#ff4b4b")); // Red color
        deleteBtn.setPadding(15, 5, 5, 5);

        // Delete Action
        deleteBtn.setOnClickListener(v -> confirmDelete(date, time));

        row.addView(info);
        row.addView(deleteBtn);

        // Add Divider
        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));
        divider.setBackgroundColor(Color.parseColor("#33FFFFFF"));

        scheduleContainer.addView(row);
        scheduleContainer.addView(divider);
    }

    private void confirmDelete(String date, String time) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Class?")
                .setMessage("Are you sure you want to remove the class at " + time + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteClassAPI(date, time))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteClassAPI(String date, String time) {
        String url = "http://" + LAPTOP_IP + ":5000/api/delete_class";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("date", date);
            jsonBody.put("time", time);
            jsonBody.put("room", roomId);
        } catch (JSONException e) { e.printStackTrace(); }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    Toast.makeText(this, "Class Deleted!", Toast.LENGTH_SHORT).show();
                    fetchSchedule(); // Refresh list immediately
                },
                error -> Toast.makeText(this, "Delete Failed", Toast.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(this).add(request);
    }

    // --- 4. MANUAL CLASS ADDITION ---
    private void showAddClassDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_class, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        EditText inputDate = dialogView.findViewById(R.id.inputDate);
        EditText inputTime = dialogView.findViewById(R.id.inputTime);
        EditText inputSubject = dialogView.findViewById(R.id.inputSubject);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        inputDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

        btnSave.setOnClickListener(v -> {
            String date = inputDate.getText().toString();
            String time = inputTime.getText().toString();
            String subject = inputSubject.getText().toString();
            uploadManualClass(date, time, subject);
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void uploadManualClass(String date, String time, String subject) {
        String url = "http://" + LAPTOP_IP + ":5000/api/add_class";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("date", date);
            jsonBody.put("time", time);
            jsonBody.put("room", roomId);
            jsonBody.put("subject", subject);
        } catch (JSONException e) { e.printStackTrace(); }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    Toast.makeText(this, "Class Added!", Toast.LENGTH_SHORT).show();
                    fetchSchedule();
                },
                error -> Toast.makeText(this, "Failed to Add", Toast.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(this).add(request);
    }
}