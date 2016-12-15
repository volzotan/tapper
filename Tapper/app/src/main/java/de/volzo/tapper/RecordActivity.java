package de.volzo.tapper;

import android.content.Intent;
import android.gesture.Gesture;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import de.volzo.tapper.GestureDetector.Displayer;
import de.volzo.tapper.GestureDetector.GestureType;

public class RecordActivity extends AppCompatActivity {

    private static final String TAG = RecordActivity.class.getName();

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
    }

    public void retry(View v) {

    }

    public void save(View v) {
        Displayer disp = (Displayer) findViewById(R.id.displayView);

        Support support = new Support(this);
        support.add(support.convert(disp.x, disp.y, disp.z)); // no need to use the displayer, arrays can be derived from the windowing class directly

        support.saveToFile(gesture.toString());

        Log.d(TAG, "samples saved for gesture " + gesture.toString());
    }
}
