package de.volzo.tapper.GestureDetector;

/**
 * Created by volzotan on 23.11.16.
 */
public class Filter {

    public boolean lowpass      = true;
    public boolean cutoff       = true;
    public boolean quantization = true;

    public double lowpass_alpha         = 0.5d;
    public double[] quantizationSteps  = {0, 1, 2, 3};
    public double cutoffThreshold = 0.2d;

    public Filter() {

    }

    public Double work(Double input) {
        return work(input, null);
    }

    public Double work(Double input, Double previousInput) {

        input = lowpass ? lowpass(input, previousInput) : input;

        //for what? Quantization should do this already
        input = cutoff ? cutoff(input) : input;

        input = quantization ?  quantize(input) : input;

        return input;
    }

    /**
     * time smoothing constant for low-pass filter
     * 0 ≤ alpha ≤ 1 ; a smaller value basically means more smoothing
     * See: http://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
     *
     * @param input the unsmoothed input
     * @param previousInput the previous smoothed output
     * @return the smoothed input
     */
    public Double lowpass(Double input, Double previousInput) {
        // lowpass
        if (previousInput != null) {
            input = previousInput + this.lowpass_alpha * (input - previousInput);
        }
        return input;
    }

    /**
     * cuts off when certain threshold is not surpassed
     *
     * @param input the uncut input
     * @return the cut input
     */
    public Double cutoff(Double input) {
        // cutoff
        if (Math.abs(input) < cutoffThreshold) {
            input = 0d;
        }
        return input;
    }

    /**
     * quantizes input into major steps (nothing, peak, strong peak, very strong peak)
     * according to thresholds in quantizationSteps (lower or equal quantizationStep[i] yields i)
     *
     * @param input the unquantized input
     * @return the numerical value of the quantization step (i.e. 0 for nothing, 1 for peak etc.)
     */
    public Double quantize(Double input) {
        // quantization
        double sign = input > 0 ? 1 : -1;
        double i = 0;
        while (i < quantizationSteps.length - 1 && quantizationSteps[(int)i+1] <= Math.abs(input)) {
            i++;
        }
        return i * sign;
    }
}
