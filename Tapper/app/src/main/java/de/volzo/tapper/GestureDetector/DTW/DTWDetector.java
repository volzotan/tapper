package de.volzo.tapper.GestureDetector.DTW;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.volzo.tapper.GestureDetector.DTW.lowlevelElements.Accellerometer;
import de.volzo.tapper.GestureDetector.DTW.lowlevelElements.GestureAnalyzer;
import de.volzo.tapper.GestureDetector.DTW.lowlevelElements.GestureHierarchyFilter;
import de.volzo.tapper.GestureDetector.DTW.lowlevelElements.Windower;
import de.volzo.tapper.GestureDetector.DTW.streamSystem.StreamReceiver;
import de.volzo.tapper.GestureDetector.Displayer;
import de.volzo.tapper.GestureDetector.GestureType;
import de.volzo.tapper.R;

/**
 * Created by tassilokarge on 05.12.16.
 */

public class DTWDetector implements StreamReceiver<GestureType> {

    private Accellerometer accel;

    //raw windows
    private Double[] rawWindowX;
    private Double[] rawWindowY;
    private Double[] rawWindowZ;

    //application context
    private Context context;

    //view
    private Displayer view;

    //to execute pipeline off main thread
    private ExecutorService pipelineExecutor = Executors.newSingleThreadExecutor();

    //to do execution of analysis off main thread
    private ExecutorService analysisExecutor = Executors.newSingleThreadExecutor();

    //windowing parameters
    private final int windowSizeMs = 2000;
    private final int windowShiftMs = 1000;
    private final int samplesPerSec = 100;

    public DTWDetector(Context context) {
        this.context = context;
        setupPipeline(context);
        view = (Displayer) ((Activity) context).findViewById(R.id.displayView);
    }

    /**
     * Pipeline:
     * accel -> absX -> square -> add -> sqrt -> avg -> quantile -> windower -> gestureAnalyzer -> foundGesture
     *     |--> absY -> square ----'                                             |
     *     '--> absZ --------------------------> avg -> quantile -> windower ----'
     *
     * Alternative pipeline:
     * accel -> absX -> avg -> quantile -> windower -> gestureAnalyzer -> foundGesture
     *     |--> absY -> avg -> quantile -> windower ----|
     *     '--> absZ -> avg -> quantile -> windower ----'
     */
    private void setupPipeline(Context context) {
        //init pipeline in reverse order


        //GESTURE ANALYZER

        GestureAnalyzer gestureAnalyzer = new GestureAnalyzer(this);


        //WINDOWER

        Windower<Integer[]> windower = new Windower<Integer[]>(windowSizeMs, windowShiftMs, samplesPerSec,
                (output) -> {
                    analysisExecutor.execute(() -> gestureAnalyzer.process(output));
                    view.x = new Double[output.length];
                    view.y = new Double[output.length];
                    view.z = new Double[output.length];
                    for (int i = 0; i < output.length; i++) {
                        view.x[i] = output[i][0].doubleValue();
                        view.y[i] = output[i][0].doubleValue();
                        view.z[i] = output[i][1].doubleValue();
                    }
                    ((Activity)context).runOnUiThread(view::invalidate);
                }
        );


        //FILTERING

        FilteringPipeline filter = new FilteringPipeline(windower);


        //RAW VALUE WINDOWING

        Windower<Double> rawWindowerX = new Windower<>(windowSizeMs, windowShiftMs, samplesPerSec, (output) -> rawWindowX = output);
        Windower<Double> rawWindowerY = new Windower<>(windowSizeMs, windowShiftMs, samplesPerSec, (output) -> rawWindowY = output);
        Windower<Double> rawWindowerZ = new Windower<>(windowSizeMs, windowShiftMs, samplesPerSec, (output) -> {rawWindowZ = output;
            //redraw view
            //view.x = rawWindowX;
            //view.y = rawWindowY;
            //view.z = rawWindowZ;
            //view.invalidate();
        });


        //ACCELLEROMETER DATA RECEIVING

        this.accel = new Accellerometer(context, (Double[] output) -> {
            //raw data to window for recording and displaying purposes
            rawWindowerX.process(output[0]);
            rawWindowerY.process(output[1]);
            rawWindowerZ.process(output[2]);
            //filter incoming values
            pipelineExecutor.execute(() -> filter.process(output));
        });
    }

    @Override
    public void process(GestureType input) {
        foundGesture(input);
    }

    private void foundGesture(GestureType type) {

        //output everything but the "NOTHING" gesture
        if (type == GestureType.NOTHING) { return; }

        Intent intent = new Intent("GESTURE_DETECTED");
        intent.putExtra("GESTURE_TYPE", type.name());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
