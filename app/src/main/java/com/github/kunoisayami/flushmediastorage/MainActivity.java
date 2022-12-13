package com.github.kunoisayami.flushmediastorage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "FlushMediaStorage";
    private static final Long SPECIAL_NUMBER = 1145141919810L;
    private static final Map<Long, Long> SELECT_2_TIME;
    static {
        Map<Long, Long> m = new HashMap<>();
        long[] items = {1, 3, 12, 24, 24*3, 24*7, 24*30, 24*180};
        m.put(0L, 600L);
        long pre_insert_size = m.size();
        for (int i = 0; i < items.length; i++) {
            m.put(i + pre_insert_size, items[i] * 3600L);
        }
        m.put(items.length + pre_insert_size, SPECIAL_NUMBER);
        SELECT_2_TIME = Collections.unmodifiableMap(m);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initElements();
        try {
            grantPermission();
        } catch (Exception e) {
            txtDefaultOutput.setText(Log.getStackTraceString(e));
            e.printStackTrace();
        }
    }

    Button btnFlush, btnVerifyDirectory;
    TextView txtDefaultOutput, txtStatus;
    EditText etDirectoryInput;
    Spinner spMode;
    SwitchCompat swDebug;
    boolean debugMode;

    void grantPermission() {
        String[] permissionsStorage = {Manifest.permission.READ_EXTERNAL_STORAGE};
        int requestExternalStorage = 1;
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissionsStorage, requestExternalStorage);
        }
    }

    String getStoreText() {
        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.key_pref_file), Context.MODE_PRIVATE);
        return sharedPref.getString(getString(R.string.key_input_data), "");
    }

    void storeText(String text) {
        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.key_pref_file), Context.MODE_PRIVATE);
        sharedPref.edit().putString(getString(R.string.key_input_data), text).apply();
    }

    void iterFiles(String directory, long time_delta) {
        Log.d(TAG, "iterFiles: " + time_delta);
        long current = System.currentTimeMillis();
        StringBuilder stringBuilder = new StringBuilder();
        String real_path = Environment.getExternalStorageDirectory() + directory;
        stringBuilder.append("Path: ").append(real_path).append("\n");
        txtDefaultOutput.setText(stringBuilder);
        Log.d(TAG, "iterFiles: " + real_path);
        File real_directory = new File(real_path);
        File[] files = real_directory.listFiles();
        if (files == null) {
            Toast toast = new Toast(this);
            toast.setText(R.string.str_empty_directory);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        ArrayList<String> paths;

        if (time_delta == SPECIAL_NUMBER) {
            paths = (ArrayList<String>) Arrays.stream(files).map(File::getAbsolutePath).collect(Collectors.toList());
        } else {
            paths = new ArrayList<>();
            for (File f: files) {
                if ((current - f.lastModified()) / 1000 < time_delta) {
                    paths.add(f.getAbsolutePath());
                }
            }
        }
        stringBuilder.append("Total: ").append(paths.size()).append("\n");
        txtDefaultOutput.setText(stringBuilder);

        MediaScannerConnection.scanFile(this, paths.toArray(new String[0]), null, (path, uri) -> {
            if (this.debugMode)
                Log.d(TAG, String.format("Scanned path %s -> URI = %s", path, uri.toString()));
        });
        txtDefaultOutput.setText(stringBuilder.append(getString(R.string.str_completed)));
    }


    void initElements() {
        btnFlush = findViewById(R.id.btn_flush);
        btnVerifyDirectory = findViewById(R.id.btn_verify_directory);
        txtDefaultOutput = findViewById(R.id.text_output);
        txtStatus = findViewById(R.id.text_status);
        etDirectoryInput = findViewById(R.id.et_directory_input);
        spMode = findViewById(R.id.spinner_mode);
        swDebug = findViewById(R.id.sw_debug);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.sp_drop_down_menu, android.R.layout.simple_spinner_item);
        spMode.setAdapter(adapter);
        etDirectoryInput.setText(getStoreText());

        btnFlush.setOnClickListener( l -> {
            String directory = etDirectoryInput.getText().toString();
            try {
                storeText(directory);
                iterFiles(directory, SELECT_2_TIME.get(spMode.getSelectedItemId()));
            } catch (Exception e) {
                txtDefaultOutput.setText(Log.getStackTraceString(e));
                e.printStackTrace();
            }
        });


        btnVerifyDirectory.setOnClickListener( l -> {
            String directory = etDirectoryInput.getText().toString();
            txtStatus.setText(Environment.getExternalStorageDirectory() + directory);
        });

        swDebug.setOnCheckedChangeListener((buttonView, isChecked) -> debugMode = isChecked);
    }
}