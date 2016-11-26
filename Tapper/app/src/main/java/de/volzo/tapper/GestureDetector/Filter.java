package de.volzo.tapper.GestureDetector;

/**
 * Created by volzotan on 23.11.16.
 */
public class Filter {

    public boolean lowpass      = true;
    public boolean cutoff       = true;
    public boolean quantization = true;

    public double lowpass_alpha         = 0.5d;
    public double[] quantization_steps  = {0, 1, 2, 3};
    public double cutoff_threshold      = 0.2d;

    public Filter() {

    }

    public Double work(Double input) {
        return work(input, null);
    }

    public Double work(Double input, Double previousInput) {

        /*
         * time smoothing constant for low-pass filter
         * 0 ≤ alpha ≤ 1 ; a smaller value basically means more smoothing
         * See: http://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
        */


        // lowpass
        if (lowpass) {
            if (previousInput != null) {
                input = previousInput + this.lowpass_alpha * (input - previousInput);
            }
        }

        // cutoff
        if (cutoff) {
            if (Math.abs(input) < cutoff_threshold) {
                input = 0d;
            }
        }

        // quantization
        if (quantization) {
            int sign = input > 0 ? 1 : -1;
            if (Math.abs(input) > quantization_steps[quantization_steps.length - 1]) {
                input = (double) quantization_steps.length * sign;
            } else {
                for (int i = 0; i < quantization_steps.length - 1; i++) {
                    if (Math.abs(input) >= quantization_steps[i] && Math.abs(input) < quantization_steps[i + 1]) {
                        input = (double) i * sign;
                        break;
                    }
                }
            }
        }

        return input;
    }
}
