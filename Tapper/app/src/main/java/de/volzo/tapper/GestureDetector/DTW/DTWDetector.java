package de.volzo.tapper.GestureDetector.DTW;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import de.volzo.tapper.GestureDetector.Displayer;
import de.volzo.tapper.GestureDetector.GestureType;
import de.volzo.tapper.R;

/**
 * Created by tassilokarge on 05.12.16.
 */

public class DTWDetector {

    private Accellerometer accel;

    //processed windows
    //private Double[] windowX;
    //private Double[] windowY;
    private Double[] windowXY;
    private Double[] windowZ;

    //raw windows
    private Double[] rawWindowX;
    private Double[] rawWindowY;
    private Double[] rawWindowZ;

    //application context
    private Context context;

    //view
    Displayer view;


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

        GestureAnalyzer gestureAnalyzer = new GestureAnalyzer(this::foundGesture);


        //WINDOWER

        //Windower windowerX = new Windower((Double[] values) -> this.windowX = values);
        //Windower windowerY = new Windower((Double[] values) -> this.windowY = values);
        Windower windowerXY = new Windower((Double[] output) -> {
            this.windowXY = output;
        });
        Windower windowerZ = new Windower((Double[] output) -> {this.windowZ = output;
            //redraw view
            //view.x = windowXY;
            //view.y = windowXY;
            //view.z = windowZ;
            //view.invalidate();
            //analyze gesture
            //gestureAnalyzer.analyze(windowX, windowY, windowZ);
            gestureAnalyzer.analyze(windowXY, windowZ);
        });


        //FILTERING

        FilteringPipeline filter = new FilteringPipeline(windowerXY::addDataPoint, windowerZ::addDataPoint);


        //RAW VALUE WINDOWING

        Windower rawWindowerX = new Windower((output) -> rawWindowX = output);
        Windower rawWindowerY = new Windower((output) -> rawWindowY = output);
        Windower rawWindowerZ = new Windower((output) -> {rawWindowZ = output;
            //redraw view
            view.x = rawWindowX;
            view.y = rawWindowY;
            view.z = rawWindowZ;
            view.invalidate();
        });


        //ACCELLEROMETER DATA RECEIVING

        this.accel = new Accellerometer(context, (double[] output) -> {
            //raw data to window for recording and displaying purposes
            rawWindowerX.addDataPoint(output[0]);
            rawWindowerY.addDataPoint(output[1]);
            rawWindowerZ.addDataPoint(output[2]);
            //filter incoming values
            filter.filter(output[0],output[1],output[2]);
        });
    }

    void foundGesture(GestureType type) {

        //output everything but the "NOTHING" gesture
        if (type == GestureType.NOTHING) { return; }

        Intent intent = new Intent("GESTURE_DETECTED");
        intent.putExtra("GESTURE_TYPE", type.name());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
