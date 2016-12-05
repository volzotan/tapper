package de.volzo.tapper.GestureDetector.DTW;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import de.volzo.tapper.GestureDetector.GestureType;

/**
 * Created by tassilokarge on 05.12.16.
 */

public class DTWDetector {

    private final Accellerometer accel;

    private double[] averagingKernel = {0.3, 0.3, 0.5, 0.8, 0.8, 0.5};
    private double averagingDivider  = 3.2;
    private double[] quantiles       = {0, 0.2, 0.5, 1.0, 1.5, 2.0, 3.0, 4.0, 5.0, 6.0, 8.0};

    private Double absX;

    //private CircularFifoQueue<Double> previousInputsX = new CircularFifoQueue<>(averagingKernel.length + 1);
    //private CircularFifoQueue<Double> previousInputsY = new CircularFifoQueue<>(averagingKernel.length + 1);
    private CircularFifoQueue<Double> previousInputsXY = new CircularFifoQueue<>(averagingKernel.length + 1);
    private CircularFifoQueue<Double> previousInputsZ = new CircularFifoQueue<>(averagingKernel.length + 1);

    //private Double[] windowX;
    //private Double[] windowY;
    private Double[] windowXY;
    private Double[] windowZ;

    private Context context;



    /**
     * Pipeline:
     * accelerometer -> absX -> square -> add -> sqrt -> avg -> quantile -> windower -> gestureAnalyzer -> foundGesture
     *             |--> absY -> square ----'                                             |
     *             '--> absZ --------------------------> avg -> quantile -> windower ----'
     *
     * Alternative pipeline:
     * accelerometer -> absX -> avg -> quantile -> windower -> gestureAnalyzer -> foundGesture
     *             |--> absY -> avg -> quantile -> windower ----|
     *             '--> absZ -> avg -> quantile -> windower ----'
     */
    public DTWDetector(Context context) {

        this.context = context;

        //init pipeline in reverse order
        GestureAnalyzer gestureAnalyzer = new GestureAnalyzer(this::foundGesture);

        //Windower windowerX = new Windower((Double[] values) -> this.windowX = values);
        //Windower windowerY = new Windower((Double[] values) -> this.windowY = values);
        Windower windowerXY = new Windower((Double[] values) -> {
            this.windowXY = values;
        });
        Windower windowerZ = new Windower((Double[] values) -> {this.windowZ = values;
            //gestureAnalyzer.analyze(windowX, windowY, windowZ);
            gestureAnalyzer.analyze(windowXY, windowZ);
        });

        //Quantizer quantizerX = new Quantizer(windowerX::analyze);
        //Quantizer quantizerY = new Quantizer(windowerY::analyze);
        Quantizer quantizerXY = new Quantizer(windowerXY::updateData);
        Quantizer quantizerZ = new Quantizer(windowerZ::updateData);

        //AveragingFilter averagingFilterX = new AveragingFilter((input) -> quantizerX.quantize(input, quantiles));
        //AveragingFilter averagingFilterY = new AveragingFilter((input) -> quantizerY.quantize(input, quantiles));
        AveragingFilter averagingFilterXY = new AveragingFilter((input) -> quantizerXY.quantize(input, quantiles));
        AveragingFilter averagingFilterZ = new AveragingFilter((input) -> quantizerZ.quantize(input, quantiles));

        final Double[] d = new Double[0];
        AbsoluteFilter absoluteFilterX = new AbsoluteFilter((input) -> {
            //this.previousInputsX.add(input);
            //averagingFilterX.averaging(input, previousInputsX.toArray(d), averagingKernel, averagingDivider);
            absX = input;
        });
        AbsoluteFilter absoluteFilterY = new AbsoluteFilter((input) -> {
            //this.previousInputsY.add(input);
            //averagingFilterY.averaging(input, previousInputsY.toArray(d), averagingKernel, averagingDivider);
            Double xy = Math.sqrt(Math.pow(absX, 2) + Math.pow(input, 2));
            this.previousInputsXY.add(xy);
            averagingFilterXY.averaging(xy, previousInputsXY.toArray(d), averagingKernel, averagingDivider);
        });
        AbsoluteFilter absoluteFilterZ = new AbsoluteFilter((input) -> {
            this.previousInputsZ.add(input);
            averagingFilterZ.averaging(input, previousInputsZ.toArray(d), averagingKernel, averagingDivider);
        });

        this.accel = new Accellerometer(context, (double[] reading) -> {
            absoluteFilterX.absolute(reading[0]);
            absoluteFilterY.absolute(reading[1]);
            absoluteFilterZ.absolute(reading[2]);
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
