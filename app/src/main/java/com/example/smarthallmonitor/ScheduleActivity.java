package com.example.smarthallmonitor;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;

public class ScheduleActivity extends AppCompatActivity {

    // *** UPDATED IP ADDRESS FROM YOUR IPCONFIG ***
    private static final String LAPTOP_IP = "10.139.192.253";

    private TableLayout tableData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        tableData = findViewById(R.id.tableData);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        fetchMasterSchedule();
    }

    private void fetchMasterSchedule() {
        String url = "http://" + LAPTOP_IP + ":5000/api/master_schedule";

        Toast.makeText(this, "Fetching Schedule...", Toast.LENGTH_SHORT).show();

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        tableData.removeAllViews();

                        if (response.length() == 0) {
                            Toast.makeText(this, "Schedule is Empty!", Toast.LENGTH_LONG).show();
                            return;
                        }

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject row = response.getJSONObject(i);
                            addTableRow(
                                    row.optString("Date"),
                                    row.optString("Time"),
                                    row.optString("Room"),
                                    row.optString("Subject"),
                                    i
                            );
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Parse Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    String err = error.getMessage() != null ? error.getMessage() : "Connection Timeout";
                    Toast.makeText(this, "Failed: " + err + ". Check Firewall!", Toast.LENGTH_LONG).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void addTableRow(String date, String time, String room, String subject, int index) {
        TableRow tr = new TableRow(this);
        tr.setPadding(10, 20, 10, 20);

        if (index % 2 == 0) {
            tr.setBackgroundColor(Color.parseColor("#1AFFFFFF"));
        }

        tr.addView(createTextView(date, "#FFFFFF"));
        tr.addView(createTextView(time, "#CCCCCC"));
        tr.addView(createTextView(room, "#00d2ff"));
        tr.addView(createTextView(subject, "#FFFFFF"));

        tableData.addView(tr);
    }

    private TextView createTextView(String text, String colorHex) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(Color.parseColor(colorHex));
        tv.setTextSize(14);
        tv.setPadding(0, 0, 10, 0);
        return tv;
    }
}