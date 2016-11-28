package de.volzo.tapper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import de.volzo.tapper.GestureDetector.DataCollector;
import de.volzo.tapper.GestureDetector.Detector;
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
            //int gestureIntensity    = intent.getIntExtra("GESTURE_INTENSITY", -1);

            // TODO: do what should be done (maybe user output), trigger action, etc...

            String msg = "Gesture detected: " + gestureType;

            //if (gestureIntensity > 0) {
            //    msg += " [" + Integer.toString(gestureIntensity) + "]";
            //}

            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();

            //TODO: comment in as a signal for which gesture was recognized

            Ringtone r1 = RingtoneManager.getRingtone(
                    getApplicationContext(),
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            Ringtone r2 = RingtoneManager.getRingtone(
                    getApplicationContext(),
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            Ringtone r3 = RingtoneManager.getRingtone(
                    getApplicationContext(),
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            try {
                switch (gestureType) {
                    case PICKUPDROP:
                        r1.play();
                        Thread.sleep(300);
                    case SIDETAP:
                        r2.play();
                        Thread.sleep(300);
                    case DOUBLETAP:
                        r3.play();
                    default:
                        break;
                }
            } catch (Exception E) {}

        }
    };

    public void registerUpdateReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("GESTURE_DETECTED");
        LocalBroadcastManager.getInstance(this).registerReceiver(updateReceiver, filter);
    }
}
