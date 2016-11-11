package de.volzo.tapper;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import de.volzo.tapper.GestureDetector.DataCollector;

public class MainActivity extends AppCompatActivity {

    DataCollector dataCollector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataCollector = new DataCollector(this);
    }
}
