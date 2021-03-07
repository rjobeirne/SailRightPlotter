package com.sail.sailright2new;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class StartActivity extends AppCompatActivity {


    TextView startCourseTextView, startNextMarkTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Intent intent = getIntent();
        String startCourse = intent.getStringExtra("course");
        String startMark = intent.getStringExtra("mark");

        // Locate the UI widgets.
        startCourseTextView = (TextView) findViewById(R.id.start_course_name);
        startNextMarkTextView = (TextView) findViewById(R.id.start_next_mark_name);

        StartDisplay(startCourse, startMark);
    }

    public void StartDisplay(String startCourse, String  startMark) {

        startCourseTextView.setText(startCourse);
        startNextMarkTextView.setText(startMark);

    }
}
