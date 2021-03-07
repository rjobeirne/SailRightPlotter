package com.sail.sailright2new;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class StartActivity extends AppCompatActivity {


    TextView startCourseTextView, startNextMarkTextView;
    boolean flagKillStart;


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

        //Locate stop button
        TextView killStart = findViewById(R.id.stopStart);
        killStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        StartDisplay(startCourse, startMark);
    }

    public void StartDisplay(String startCourse, String  startMark) {

        startCourseTextView.setText(startCourse);
        startNextMarkTextView.setText(startMark);


    }
}
