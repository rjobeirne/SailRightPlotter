package com.sail.sailrightplotter;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class HelpActivity extends Activity {

private StringBuilder text = new StringBuilder();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        BufferedReader reader = null;

    try {
        reader = new BufferedReader(
            new InputStreamReader(getAssets().open("help.txt")));

        // do reading, usually loop until end of file reading
        String mLine;
        while ((mLine = reader.readLine()) != null) {
            text.append(mLine);
            text.append('\n');
        }
    } catch (
    IOException e) {
        Toast.makeText(getApplicationContext(),"Error reading file!",Toast.LENGTH_LONG).show();
        e.printStackTrace();
    } finally {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                //log the exception
            }
        }

        TextView output = (TextView) findViewById(R.id.help_text);
        output.setText((CharSequence) text);
        output.setMovementMethod(new ScrollingMovementMethod());
    }
                //Locate go back button
        TextView goBack = findViewById(R.id.back_button);
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               finish();
            }
        });

 }
}
