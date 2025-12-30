package com.example.smarthallmonitor;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class RoomActivity extends AppCompatActivity {

    // *** UPDATED IP ADDRESS FROM YOUR IPCONFIG ***
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

        ((TextView)findViewById(R.id.txtRoomTitle)).setText("Room " + roomId + " Control");

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

        // Fetch Data
        fetchThingSpeakHistory();
        fetchSchedule();
    }

    private void configureRoom(String id) {
        if(id.equals("101")) { channelId="3209747"; readKey="TR9XFHVVTNWQTCPH"; }
        else if(id.equals("102")) { channelId="3213892"; readKey="IDFQECXSIM1ZMCHM"; }
        else if(id.equals("201")) { channelId="3213894"; readKey="18TYV1WH06TXAAWN"; }
        else if(id.equals("202")) { channelId="3213896"; readKey="V11EWNH7AYHS3BKM"; }
    }

    private void fetchThingSpeakHistory() {
        String url = "https://api.thingspeak.com/channels/" + channelId + "/feeds.json?api_key=" + readKey + "&results=20";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray feeds = response.getJSONArray("feeds");

                        if(feeds.length() > 0) {
                            JSONObject latest = feeds.getJSONObject(feeds.length()-1);
                            updateStatus(latest);
                        }

                        ArrayList<Entry> entries = new ArrayList<>();
                        for(int i=0; i<feeds.length(); i++) {
                            JSONObject f = feeds.getJSONObject(i);
                            float val = (float) f.optDouble("field1", 0);
                            entries.add(new Entry(i, val));
                        }

                        LineDataSet set = new LineDataSet(entries, "Sensor 1 Temp");
                        set.setColor(Color.CYAN);
                        set.setLineWidth(2f);
                        set.setDrawCircles(false);
                        set.setDrawValues(false);

                        chart.setData(new LineData(set));
                        chart.invalidate();

                    } catch (Exception e) { e.printStackTrace(); }
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

    private void fetchSchedule() {
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Using the new IP here
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
                            TextView tv = new TextView(this);
                            tv.setText(row.getString("Time") + " : " + row.getString("Subject"));
                            tv.setTextColor(Color.WHITE);
                            tv.setTextSize(16);
                            tv.setPadding(0, 10, 0, 10);
                            scheduleContainer.addView(tv);
                        }
                    } catch(Exception e) {}
                }, error -> {}
        );
        Volley.newRequestQueue(this).add(req);
    }
}