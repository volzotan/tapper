package de.volzo.tapper.GestureDetector.DTW;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
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
import de.volzo.tapper.Support;

/**
 * Created by tassilokarge on 05.12.16.
 */

public class DTWDetector implements StreamReceiver<GestureType> {


    private static final String TAG = DTWDetector.class.getName();

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
    private final int windowSizeMs = 1000;
    private final int windowShiftMs = 500;
    private final int samplesPerSec = 100;

    public DTWDetector(Context context) throws Exception {
        this.context = context;

        // check if gesture templates are present
        Support support = new Support(context);
        String emptytemplate = support.loadFromFile("NOTHING");
        if (emptytemplate == null) {
            // empty template is missing
            support.add(new double[100][3]);
            support.saveToFile("NOTHING");
            Log.d(TAG, "empty template was missing. created it.");
        }

        List<GestureType> missingGestureTemplates = new ArrayList<GestureType>();
        for (GestureType gesture : GestureType.getAllPublicGestureTypes()) {
            String csv = support.loadFromFile(gesture.name());
            if (csv == null) {
                missingGestureTemplates.add(gesture);
            }
        }

        if (missingGestureTemplates.size() > 0) {
            String errmsg = "The following gestures have not been recorded yet: ";
            for (GestureType gesture : missingGestureTemplates) {
                errmsg += GestureType.getDisplayName(gesture);
                errmsg += " ";
            }

            throw new Exception(errmsg);
        }

        setupPipeline(context);
        view = (Displayer) ((Activity) context).findViewById(R.id.displayView);
    }

    public void stop() {
        // TODO
        throw new NotImplementedException("implement me!");
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

        GestureAnalyzer gestureAnalyzer = new GestureAnalyzer(new GestureHierarchyFilter(this));


        //WINDOWER

        Windower<Number[]> windower = new Windower<Number[]>(windowSizeMs, windowShiftMs, samplesPerSec,
                (output) -> {
                    analysisExecutor.execute(() -> gestureAnalyzer.process(output));
                    if (view != null){
                        view.x = new Double[output.length];
                        view.y = new Double[output.length];
                        view.z = new Double[output.length];
                        for (int i = 0; i < output.length; i++) {
                            view.x[i] = output[i][0].doubleValue();
                            view.y[i] = output[i][0].doubleValue();
                            view.z[i] = output[i][1].doubleValue();
                        }
                        ((Activity) context).runOnUiThread(view::invalidate);
                    }
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
