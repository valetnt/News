package com.example.android.news;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    public static final String QUERY = "https://content.guardianapis.com/search?section=science|technology&show-fields=thumbnail&show-tags=contributor&api-key=test";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
