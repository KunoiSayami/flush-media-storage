package com.github.kunoisayami.flushmediastorage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "FlushMediaStorage";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initButton();
        try {
            grantPermission();
        } catch (Exception e) {
            txtDefaultOutput.setText(Log.getStackTraceString(e));
            e.printStackTrace();
        }
    }

    Button btnFlush, btnSelectDirectory;
    TextView txtDefaultOutput;
    EditText etDirectoryInput;

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

    void iterFiles(String directory) {
        String real_path = Environment.getExternalStorageDirectory() + directory;
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
        for (File f: files) {
            Log.d(TAG, "iterFiles: " + f);
        }
    }

    void initButton() {
        btnFlush = findViewById(R.id.btn_flush);
        btnSelectDirectory = findViewById(R.id.btn_select_directory);
        txtDefaultOutput = findViewById(R.id.text_output);
        etDirectoryInput = findViewById(R.id.et_directory_input);
        etDirectoryInput.setText(getStoreText());

        btnFlush.setOnClickListener( l -> {
            String directory = etDirectoryInput.getText().toString();
            try {
                storeText(directory);
                iterFiles(directory);
            } catch (Exception e) {
                txtDefaultOutput.setText(Log.getStackTraceString(e));
                e.printStackTrace();
            }
        });
    }
}