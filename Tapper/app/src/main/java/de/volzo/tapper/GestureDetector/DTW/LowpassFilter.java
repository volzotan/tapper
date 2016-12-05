package de.volzo.tapper.GestureDetector.DTW;

/**
 * Created by tassilokarge on 05.12.16.
 */

public class LowpassFilter extends StreamElement<Double> {

    LowpassFilter(Consumer<Double> lowpassConsumer) {
        super(lowpassConsumer);
    }

    /**
     * time smoothing constant for low-pass filter
     * 0 ≤ alpha ≤ 1 ; a smaller value basically means more smoothing
     * See: http://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
     *
     * @param input the unsmoothed input
     * @param previousInputs the previous smoothed output
     * @param lowpassAlpha the alpha value for the lowpass filter
     * @return the smoothed input
     */
    public void lowpass(Double input, Double[] previousInputs, double lowpassAlpha) {
        if (previousInputs != null && previousInputs.length > 0) {
            input = previousInputs[previousInputs.length-1];
            input += lowpassAlpha * (input - previousInputs[previousInputs.length-1]);
        }

        super.passProcessedElement(input);
    }
}
