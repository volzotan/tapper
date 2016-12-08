package de.volzo.tapper.GestureDetector.DTW.lowlevelElements;

import de.volzo.tapper.GestureDetector.DTW.streamSystem.StreamPassthrough;
import de.volzo.tapper.GestureDetector.DTW.streamSystem.StreamReceiver;

/**
 * Created by tassilokarge on 05.12.16.
 */

public class LowpassFilter extends StreamPassthrough<Double, Double> {

    private Double previousInput = 0d;
    private final Double lowpassAlpha;

    public LowpassFilter(Double lowpassAlpha, StreamReceiver<Double> lowpassStreamReceiver) {
        super(lowpassStreamReceiver);
        this.lowpassAlpha = lowpassAlpha;
    }

    @Override
    public void process(Double input) {
        super.emitElement(lowpass(input));
    }

    /**
     * time smoothing constant for low-pass filter
     * 0 ≤ alpha ≤ 1 ; a smaller value basically means more smoothing
     * See: http://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
     *
     * @param input the unsmoothed input
     * @return the smoothed input
     */
    public Double lowpass(Double input) {

        input += previousInput + lowpassAlpha * (input - previousInput);

        previousInput = input;

        return input;
    }
}
