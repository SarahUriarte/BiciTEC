package com.example.bicitec_project;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class StolenBicycle extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stolen_bicycle);
        Toast.makeText(getApplicationContext(),"Hello",Toast.LENGTH_SHORT).show();
    }
}
