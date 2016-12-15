package de.volzo.tapper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import de.volzo.tapper.GestureDetector.DTW.DTWDetector;
import de.volzo.tapper.GestureDetector.FSM.DataCollector;
import de.volzo.tapper.GestureDetector.Displayer;
import de.volzo.tapper.GestureDetector.FSM.FSMDetector;
import de.volzo.tapper.GestureDetector.GestureType;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = DataCollector.class.getName();

    public FSMDetector fSMDetector;
    public DataCollector dataCollector;

    public DTWDetector dTWDetector;

    ActionTriggers actionTriggers;

    MainActivity activity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //fSMDetector = new FSMDetector(this);
        //dataCollector   = new DataCollector(this, fSMDetector);

        dTWDetector = new DTWDetector(this);

        registerUpdateReceiver();
    }

    // ---------------------       DEBUG       --------------------- //

    public void uploadSamplesToGithub(View v) {
//
//        Log.wtf(TAG, "upload");
//        Support support = new Support(this);
//
//        Displayer disp = (Displayer) findViewById(R.id.displayView);
//        support.add(support.convert(disp.x, disp.y, disp.z));
//        support.send("test_" + System.currentTimeMillis() / (1000));


        Support support = new Support(this);
        Displayer disp = (Displayer) findViewById(R.id.displayView);
        support.add(support.convert(disp.x, disp.y, disp.z));

        System.out.println(support.stringbuilder.toString());
    }

    // --------------------- Gesture Detection --------------------- //

    private BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            GestureType gestureType = GestureType.valueOf(intent.getStringExtra("GESTURE_TYPE"));
            String msg = "Gesture detected: " + gestureType;
            Log.d(TAG, msg);
            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();

            // TODO: match the gesture to the action

            actionTriggers.triggerAction("TODO");
        }
    };

    public void registerUpdateReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("GESTURE_DETECTED");
        LocalBroadcastManager.getInstance(this).registerReceiver(updateReceiver, filter);
    }
}
