package de.volzo.tapper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import de.volzo.tapper.GestureDetector.FSM.DataCollector;
import de.volzo.tapper.GestureDetector.FSM.Detector;
import de.volzo.tapper.GestureDetector.GestureType;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = DataCollector.class.getName();

    public Detector gestureDetector;
    public DataCollector dataCollector;

    MainActivity activity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gestureDetector = new Detector(this);
        dataCollector   = new DataCollector(this, gestureDetector);

        registerUpdateReceiver();
    }

    private BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            GestureType gestureType = GestureType.valueOf(intent.getStringExtra("GESTURE_TYPE"));

            // TODO: do what should be done (maybe user output), trigger action, etc...

            String msg = "Gesture detected: " + gestureType;

            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
        }
    };

    public void registerUpdateReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("GESTURE_DETECTED");
        LocalBroadcastManager.getInstance(this).registerReceiver(updateReceiver, filter);
    }
}
