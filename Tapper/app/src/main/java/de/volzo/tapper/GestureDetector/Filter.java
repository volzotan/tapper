package de.volzo.tapper.GestureDetector;

/**
 * Created by volzotan on 23.11.16.
 */
public class Filter {

    public boolean lowpass      = false;
    public boolean averaging    = true;
    public boolean cutoff       = false;
    public boolean quantization = true;
    public boolean filler = true;

    public double lowpass_alpha         = 0.5d;
    public double[] averaging_kernel    = {0.3, 0.5, 0.8};
    public double averaging_divider     = 1.6;
    public double[] quantizationSteps   = {0, 1, 4, 5};
    public double cutoffThreshold       = 0.2d;

    public Filter() {}

    public Double work(Double input) {
        return work(input, null);
    }

    public Double work(Double input, Double[] previousInputs) {

        input = Math.abs(input);
        if (previousInputs != null){
            for (Double val : previousInputs) {
                if (val == null) break;
                val = Math.abs(val);
            }
        }

        input = lowpass ? lowpass(input, previousInputs) : input;

        input = averaging ? averaging(input, previousInputs) : input;

        input = cutoff ? cutoff(input) : input;

        input = quantization ? quantize(input) : input;

        if (filler) filler(input, previousInputs);

        return input;
    }

    /**
     * time smoothing constant for low-pass filter
     * 0 ≤ alpha ≤ 1 ; a smaller value basically means more smoothing
     * See: http://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
     *
     * @param input the unsmoothed input
     * @param previousInputs the previous smoothed output
     * @return the smoothed input
     */
    public Double lowpass(Double input, Double[] previousInputs) {
        if (previousInputs != null && previousInputs.length > 0) {
            input = previousInputs[previousInputs.length-1];
            input += this.lowpass_alpha * (input - previousInputs[previousInputs.length-1]);
        }
        return input;
    }

    /**
     * weighted averaging. averaging_divider should be the sum of all weights in the kernel
     * @param input the input
     * @param previousInputs the previous inputs (raw)
     * @return the averaged output value
     */
    public Double averaging(Double input, Double[] previousInputs) {
        double output = 0;
        if (previousInputs != null && previousInputs.length > averaging_kernel.length) {
            for (int i=0; i<this.averaging_kernel.length-2; i++) {
                Double prev = previousInputs[previousInputs.length-(i+1)];
                output += averaging_kernel[i] * (prev != null ? prev : 0);
            }
            output += input * averaging_kernel[averaging_kernel.length-1];
            output /= averaging_divider;
        }
        return output;
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

    /**
     * fills in gaps if the previous 2 inputs are high, the new input is high and the last
     * input is not. Works directly on previous values and does not alter the input variable.
     *
     * @param input the input
     */
    public void filler(Double input, Double[] previousInputs) {

        if (previousInputs != null && previousInputs.length > 3) {
            int len = previousInputs.length;
            if (previousInputs[len - 1] != null) {
                if (previousInputs[len - 1] < previousInputs[len - 3]) {
                    if (previousInputs[len - 1] < previousInputs[len - 2]) {
                        if (previousInputs[len - 1] < previousInputs[len - 2]) {
                            previousInputs[len - 1] = input;
                        }
                    }
                }
            }
        }
    }
}
