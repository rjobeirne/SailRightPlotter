package com.sail.sailrightplotter;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class HelpActivity extends Activity {

private StringBuilder text = new StringBuilder();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        TextView output = findViewById(R.id.help_text);
        try {
            output.setText(Html.fromHtml(getHtmlText()));
            output.setMovementMethod(new ScrollingMovementMethod());
        } catch (IOException e) {
            throw new RuntimeException(e);
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
private String getHtmlText() throws IOException {
     InputStream inputStream = getResources().openRawResource(R.raw.help);
     ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
     int i;
     try {
         i = inputStream.read();
          while (i != -1)
              {
               byteArrayOutputStream.write(i);
               i = inputStream.read();
              }
          inputStream.close();
          } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
         }
          return byteArrayOutputStream.toString();
    }
}
