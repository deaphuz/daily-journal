package com.example.dailyjournal;

import static android.content.ContentValues.TAG;


import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.UUID;

public class EntryFragment extends Fragment implements SensorEventListener, DatePickerDialog.OnDateSetListener {

    private static final float SHAKE_THRESHOLD = 8f;
    private long lastShakeTime;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Entry entry;
    private TextView dateField;
    private EditText nameField;
    private EditText descField;
    private EditText videoField;
    private Button dateButton;
    private CheckBox doneCheckBox;
    public static final String ARG_ENTRY_ID = "entry_id";


    public static String extractVideoId(String youtubeUrl) {
        String videoId = null;
        String pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed\\.|youtu\\.be%2F|\\/v%2F|e%2F|watch\\?v=|\\?v=)([^#\\&\\?\\n]*[^\\?\\n])";

        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(youtubeUrl);

        if (matcher.find()) {
            videoId = matcher.group();
        }
        return videoId;
    }
    public EntryFragment() {
    }

    public static EntryFragment newInstance(UUID entryId) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_ENTRY_ID, entryId);
        EntryFragment entryFragment = new EntryFragment();
        entryFragment.setArguments(bundle);
        return entryFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID entryId = (UUID)getArguments().getSerializable(ARG_ENTRY_ID);
        entry = EntryStorage.getInstance().getEntry(entryId);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_entry, container, false);

        sensorManager = (SensorManager) requireActivity().getApplicationContext().getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        WebView webView = view.findViewById(R.id.webview_youtube);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        String[] videoId = {entry.getVideoID()};
        String iframe = "<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/" + videoId[0] + "\" frameborder=\"0\" allowfullscreen></iframe>";

        webView.loadData(iframe, "text/html", "utf-8");

        nameField = view.findViewById(R.id.entry_name);
        nameField.setText(entry.getName());

        descField = view.findViewById(R.id.entry_description);
        descField.setText(entry.getDescription());

        videoField = view.findViewById(R.id.entry_video);
        videoField.setText(entry.getVideoID());

        dateField = view.findViewById(R.id.entry_date_field);

        nameField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                entry.setName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        descField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                entry.setDescription(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        videoField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = extractVideoId(s.toString());
                entry.setVideoID(str);
                videoId[0] = str;

            }

            @Override
            public void afterTextChanged(Editable s) { webView.loadData(iframe, "text/html", "utf-8"); }


        });

        dateButton = view.findViewById(R.id.entry_date_button);
        Date date = entry.getDate();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
        dateField.setText(sdf.format(date));
        dateButton.setEnabled(true);

        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }

            private void showDatePickerDialog() {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), EntryFragment.this, year, month, dayOfMonth);
                datePickerDialog.show();
            }



        });



        doneCheckBox = view.findViewById(R.id.entry_done);
        doneCheckBox.setChecked(entry.isDone());
        doneCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            entry.setDone(isChecked);
        });
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastShakeTime) > 1000) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float acceleration = (float) Math.sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH;

                if (acceleration > SHAKE_THRESHOLD) {
                    lastShakeTime = currentTime;
                    this.requireActivity().finish();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, month);
        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String selectedDate = DateFormat.getDateInstance(DateFormat.FULL).format(mCalendar.getTime());
        dateField.setText(selectedDate);
        entry.setDate(mCalendar.getTime());
    }
}