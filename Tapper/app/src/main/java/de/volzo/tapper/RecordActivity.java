package de.volzo.tapper;

import android.content.Intent;
import android.gesture.Gesture;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import de.volzo.tapper.GestureDetector.Displayer;
import de.volzo.tapper.GestureDetector.FSM.DataCollector;
import de.volzo.tapper.GestureDetector.GestureType;

public class RecordActivity extends AppCompatActivity {

    private static final String TAG = RecordActivity.class.getName();

    private DataCollector dataCollector;
    GestureType gesture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        Intent intent = getIntent();

        if (!intent.hasExtra("GESTURE")) {
            Log.e(TAG, "no gesture name provided");
            finish();
        }

        String gesturename = intent.getStringExtra("GESTURE");
        gesture = GestureType.valueOf(gesturename);

        retry(null);
    }

    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "record Activity destroyed");
        dataCollector.stop();
    }

    public void retry(View v) {
        dataCollector = new DataCollector(this, null);

        Displayer displayer = (Displayer) findViewById(R.id.displayView);
        displayer.clear();
        displayer.invalidate();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "stop recording");
                dataCollector.stop();
            }
        }, 3000);
    }

    public void save(View v) {
        Support support = new Support(this);
        support.add(support.convert(dataCollector.rawax, dataCollector.raway, dataCollector.rawaz, 280));

        support.saveToFile(gesture.toString());

        Log.d(TAG, "samples saved for gesture " + gesture.toString());

        Toast.makeText(this, "Gesture Data for " + GestureType.getDisplayName(gesture) + " saved",
                Toast.LENGTH_SHORT).show();

        finish();
    }
}
