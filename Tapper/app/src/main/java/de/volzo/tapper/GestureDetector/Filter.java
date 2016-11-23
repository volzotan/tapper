package de.volzo.tapper.GestureDetector;

/**
 * Created by volzotan on 23.11.16.
 */
public class Filter {

    public boolean lowpass      = true;
    public boolean cutoff       = true;
    public boolean quantization = true;

    public double lowpass_alpha         = 0.5d;
    public double cutoff_threshold      = 0.3d;
    public double[] quantization_steps  = {0, 1, 2};

    public Filter() {

    }

    public Double work(Double input) {
        return work(input, null);
    }

    public Double work(Double input, Double previousInput) {

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
            if (input > quantization_steps[quantization_steps.length - 1]) {
                input = (double) quantization_steps.length * (input/input);
            } else {
                for (int i = 0; i < quantization_steps.length - 1; i++) {
                    if (Math.abs(input) >= quantization_steps[i] && Math.abs(input) < quantization_steps[i + 1]) {
                        input = (double) i * (input/input);
                        break;
                    }
                }
            }
        }

        return input;
    }
}
