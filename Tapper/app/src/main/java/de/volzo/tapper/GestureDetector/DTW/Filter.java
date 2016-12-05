package de.volzo.tapper.GestureDetector.DTW;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.util.function.Consumer;

/**
 * Created by volzotan on 23.11.16.
 */
public class Filter {

    private double lowpassAlpha      = 0.5d;
    private double[] averagingKernel = {0.3, 0.3, 0.5, 0.8, 0.8, 0.5};
    private double averagingDivider  = 3.2;
    private double[] quantiles       = {0, 0.2, 0.5, 1.0, 1.5, 2.0, 3.0, 4.0, 5.0, 6.0, 8.0};
    private double cutoffThreshold   = 0.2d;

    private CircularFifoQueue<Double> previousInputs = new CircularFifoQueue<>(averagingKernel.length + 1);

    private Consumer<Double> resultConsumer;

    public Filter(Consumer<Double> resultConsumer) {
        this.resultConsumer = resultConsumer;
    }

    public void dataUpdate(Double input) {

        input = absolute(input);

        this.previousInputs.add(input);

        input = lowpass(input, (Double[]) previousInputs.toArray(), lowpassAlpha);
        input = averaging(input, (Double[]) previousInputs.toArray(), averagingKernel, averagingDivider);
        input = quantize(input, quantiles);

        resultConsumer.accept(input);
    }

    private double absolute(double input) {
        return Math.abs(input);
    }

    private Double[] absolute(Double[] previousInputs) {
        Double[] absPreviousInputs = new Double[previousInputs.length];

        // use new array for absolute values (old one is used for drawing on the canvas)
        for (int i=0; i < previousInputs.length; i++) {
            if (previousInputs[i] == null) break;
            absPreviousInputs[i] = absolute(previousInputs[i]);
        }
        return absPreviousInputs;
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
    public Double lowpass(Double input, Double[] previousInputs, double lowpassAlpha) {
        if (previousInputs != null && previousInputs.length > 0) {
            input = previousInputs[previousInputs.length-1];
            input += lowpassAlpha * (input - previousInputs[previousInputs.length-1]);
        }
        return input;
    }

    /**
     * weighted averaging. averagingDivider should be the sum of all weights in the kernel
     * @param input the input
     * @param previousInputs the previous inputs (raw)
     * @param averagingKernel the kernel values for averaging
     * @param averagingDivider divider for the averaged values
     * @return the averaged output value
     */
    public Double averaging(Double input, Double[] previousInputs, double[] averagingKernel, double averagingDivider) {
        double output = 0;
        if (previousInputs != null && previousInputs.length > averagingKernel.length) {
            for (int i = 0; i < averagingKernel.length - 2; i++) {
                Double prev = previousInputs[previousInputs.length-((averagingKernel.length-i)+1)];
                output += averagingKernel[i] * (prev != null ? prev : 0);
            }
            output += input * averagingKernel[averagingKernel.length - 1];
            output /= averagingDivider;
        }
        return output;
    }

    /**
     * cuts off when certain threshold is not surpassed
     *
     * @param input the uncut input
     * @param cutoffThreshold the threshold under which everything is zero
     * @return the cut input
     */
    public Double cutoff(Double input, double cutoffThreshold) {
        // cutoff
        if (input < cutoffThreshold) {
            input = 0d;
        }
        return input;
    }

    /**
     * quantizes input into major steps (nothing, peak, strong peak, very strong peak)
     * according to thresholds in quantizationSteps (lower or equal quantizationStep[i] yields i)
     *
     * @param input the unquantized input
     * @param quantizationSteps the steps for the quantiles
     * @return the numerical value of the quantization step (i.e. 0 for nothing, 1 for peak etc.)
     */
    public Double quantize(Double input, double[] quantizationSteps) {
        // quantization
        double sign = input > 0 ? 1 : -1;
        double i = 0;
        while (i < quantizationSteps.length - 1 && quantizationSteps[(int)i+1] <= input) {
            i++;
        }
        return i * sign;
    }

}
