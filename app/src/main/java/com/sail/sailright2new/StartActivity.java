package com.sail.sailright2new;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class StartActivity extends AppCompatActivity {


    TextView startCourseTextView, startNextMarkTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // Locate the UI widgets.
        startCourseTextView = (TextView) findViewById(R.id.start_course_name);
        startNextMarkTextView = (TextView) findViewById(R.id.start_next_mark_name);


    }

    public StartActivity () {
//        Bundle extras = getIntent().getExtras();
//        if (extras != null) {
//            String startCourse = extras.getString("course");
//            String startMark = extras.getString("mark");


        String startCourse = getIntent().getStringExtra("course");
        String startMark = getIntent().getStringExtra("mark");
        Log.d("course,mark", startCourse + ", " + startMark);
//    }
//        startCourseTextView.setText(startCourse);
//        startNextMarkTextView.setText(startMark);

    }
}
