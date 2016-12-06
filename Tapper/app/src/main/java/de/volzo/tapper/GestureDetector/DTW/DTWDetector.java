package de.volzo.tapper.GestureDetector.DTW;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import de.volzo.tapper.GestureDetector.FSM.Displayer;
import de.volzo.tapper.GestureDetector.GestureType;
import de.volzo.tapper.R;

/**
 * Created by tassilokarge on 05.12.16.
 */

public class DTWDetector {

    private Accellerometer accel;

    //constants for filters
    private final double[] averagingKernel = {0.3, 0.3, 0.5, 0.8, 0.8, 0.5};
    private final double averagingDivider  = 3.2;
    private final double[] quantiles       = {0, 0.2, 0.5, 1.0, 1.5, 2.0, 3.0, 4.0, 5.0, 6.0, 8.0};

    //intermediate x value for combining x and y
    private Double absX;

    //intermediate values for averaging filter
    //private CircularFifoQueue<Double> previousInputsX = new CircularFifoQueue<>(averagingKernel.length + 1);
    //private CircularFifoQueue<Double> previousInputsY = new CircularFifoQueue<>(averagingKernel.length + 1);
    private CircularFifoQueue<Double> previousInputsXY = new CircularFifoQueue<>(averagingKernel.length + 1);
    private CircularFifoQueue<Double> previousInputsZ = new CircularFifoQueue<>(averagingKernel.length + 1);

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
            //gestureAnalyzer.analyze(windowX, windowY, windowZ);
            gestureAnalyzer.analyze(windowXY, windowZ);
        });


        //QUANTIZER

        //Quantizer quantizerX = new Quantizer(windowerX::analyze);
        //Quantizer quantizerY = new Quantizer(windowerY::analyze);
        Quantizer quantizerXY = new Quantizer((output) -> windowerXY.addDataPoint(output));
        Quantizer quantizerZ = new Quantizer((output) -> windowerZ.addDataPoint(output));


        //AVERAGING

        //AveragingFilter averagingFilterX = new AveragingFilter((output) -> quantizerX.quantize(output, quantiles));
        //AveragingFilter averagingFilterY = new AveragingFilter((output) -> quantizerY.quantize(output, quantiles));
        AveragingFilter averagingFilterXY = new AveragingFilter((output) -> quantizerXY.quantize(output, quantiles));
        AveragingFilter averagingFilterZ = new AveragingFilter((output) -> quantizerZ.quantize(output, quantiles));


        //ABSOLUTE

        final Double[] d = new Double[0];
        AbsoluteFilter absoluteFilterX = new AbsoluteFilter((output) -> {
            //this.previousInputsX.add(output);
            //averagingFilterX.averaging(output, previousInputsX.toArray(d), averagingKernel, averagingDivider);
            absX = output;
        });
        AbsoluteFilter absoluteFilterY = new AbsoluteFilter((output) -> {
            //this.previousInputsY.add(output);
            //averagingFilterY.averaging(output, previousInputsY.toArray(d), averagingKernel, averagingDivider);
            Double xy = Math.sqrt(Math.pow(absX, 2) + Math.pow(output, 2));
            this.previousInputsXY.add(xy);
            averagingFilterXY.averaging(xy, previousInputsXY.toArray(d), averagingKernel, averagingDivider);
        });
        AbsoluteFilter absoluteFilterZ = new AbsoluteFilter((output) -> {
            this.previousInputsZ.add(output);
            averagingFilterZ.averaging(output, previousInputsZ.toArray(d), averagingKernel, averagingDivider);
        });


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

            absoluteFilterX.absolute(output[0]);
            absoluteFilterY.absolute(output[1]);
            absoluteFilterZ.absolute(output[2]);
        });
    }

    void foundGesture(GestureType type) {

        //output everything but the "NOTHING" gesture
        if (type == GestureType.NOTHING) { return; }

        Intent intent = new Intent("GESTURE_DETECTED");
        intent.putExtra("GESTURE_TYPE", GestureType.DOUBLETAP.name());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
