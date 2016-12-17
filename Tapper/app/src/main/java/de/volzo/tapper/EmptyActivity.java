package de.volzo.tapper;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.chan.fastdtw.dtw.DTW;

import de.volzo.tapper.GestureDetector.DTW.DTWDetector;

public class EmptyActivity extends AppCompatActivity {

    private DTWDetector dTWDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_empty);

        try {
            dTWDetector = new DTWDetector(this);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    protected void onDestroy() {
        dTWDetector.stop();
    }
}
