package com.cbit.disha;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TipsForWomenActivity extends AppCompatActivity {

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tips_for_women);
        getSupportActionBar().setTitle(getResources().getString(R.string.tips_for_women));
        textView = findViewById(R.id.tips_women);
        try {
            loadText();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadText() throws IOException {
        String string = "";
        StringBuilder stringBuilder = new StringBuilder();
        InputStream is = this.getResources().openRawResource(R.raw.tips_womem);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        while (true) {
            try {
                if ((string = reader.readLine()) == null) break;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            stringBuilder.append(string).append("\n");
            textView.setText(stringBuilder);
        }
        is.close();
    }
}