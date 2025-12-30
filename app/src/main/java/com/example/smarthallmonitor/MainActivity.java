package com.example.smarthallmonitor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String LAPTOP_IP = "10.139.192.253"; // Confirm this matches your ipconfig

    private static final int PICK_FILE_REQUEST = 101;
    private String selectedUploadType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Block Navigation
        findViewById(R.id.btnBlock1).setOnClickListener(v -> {
            Intent i = new Intent(this, BlockActivity.class);
            i.putExtra("BLOCK_ID", "b1");
            startActivity(i);
        });

        findViewById(R.id.btnBlock2).setOnClickListener(v -> {
            Intent i = new Intent(this, BlockActivity.class);
            i.putExtra("BLOCK_ID", "b2");
            startActivity(i);
        });

        // 2. Upload Listeners
        findViewById(R.id.btnUploadSem).setOnClickListener(v -> {
            selectedUploadType = "semester";
            openFilePicker();
        });

        findViewById(R.id.btnUploadMod).setOnClickListener(v -> {
            selectedUploadType = "modified";
            openFilePicker();
        });

        // 3. View Master Schedule
        findViewById(R.id.btnViewMaster).setOnClickListener(v ->
                startActivity(new Intent(this, ScheduleActivity.class))
        );
    }

    // --- FILE UPLOAD LOGIC ---
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {"application/vnd.ms-excel", "text/csv", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri fileUri = data.getData();
            uploadFileToServer(fileUri);
        }
    }

    private void uploadFileToServer(Uri fileUri) {
        String url = "http://" + LAPTOP_IP + ":5000/api/upload";
        Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show();

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(this, "Upload Successful!", Toast.LENGTH_LONG).show();
                },
                error -> {
                    String err = error.getMessage() != null ? error.getMessage() : "Check Laptop Connection";
                    Toast.makeText(this, "Upload Failed: " + err, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("type", selectedUploadType);
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                try {
                    InputStream iStream = getContentResolver().openInputStream(fileUri);
                    byte[] inputData = getBytes(iStream);
                    params.put("file", new DataPart("upload.xlsx", inputData));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return params;
            }
        };

        Volley.newRequestQueue(this).add(multipartRequest);
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}