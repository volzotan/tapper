package de.volzo.tapper.GestureDetector;

/**
 * Created by volzotan on 23.11.16.
 */
public class Filter {

    public boolean lowpass      = false; // averaging is more effective than lowpass
    public boolean averaging    = true;
    public boolean cutoff       = false;
    public boolean quantization = true;

    public double lowpass_alpha         = 0.5d;
    public double[] averaging_kernel    = {0.3, 0.3, 0.5, 0.8, 0.8, 0.5};
    public double averaging_divider     = 3.2;
    public double cutoffThreshold       = 0.2d;

    public Filter() {}

    public double workZ(Double input, Double[] previousInputs) {
        return this.work(input, previousInputs, new double[]{0, 0.3, 3, 5});
    }

    public double workXY(Double input, Double[] previousInputs) {
        return this.work(input, previousInputs, new double[]{0, 0.5, 3, 7});
    }

    public double work(Double input, Double[] previousInputs, double[] quantizationSteps) {

        Double[] absPreviousInputs = new Double[previousInputs.length];

        input = Math.abs(input);
        // use new array for absolute values (old one is used for drawing on the canvas)
        if (previousInputs != null) {
            for (int i=0; i < previousInputs.length; i++) {
                if (previousInputs[i] == null) break;
                absPreviousInputs[i] = Math.abs(previousInputs[i]);
            }
        }

        input = lowpass ? lowpass(input, previousInputs) : input;

        input = averaging ? averaging(input, absPreviousInputs) : input;

        input = cutoff ? cutoff(input) : input;

        input = quantization ? quantize(input, quantizationSteps) : input;

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
                Double prev = previousInputs[previousInputs.length-((averaging_kernel.length-i)+1)];
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
     * @param quantizationSteps
     * @return the numerical value of the quantization step (i.e. 0 for nothing, 1 for peak etc.)
     */
    public Double quantize(Double input, double[] quantizationSteps) {
        // quantization
        double sign = input > 0 ? 1 : -1;
        double i = 0;
        while (i < quantizationSteps.length - 1 && quantizationSteps[(int)i+1] <= Math.abs(input)) {
            i++;
        }
        return i * sign;
    }

}
