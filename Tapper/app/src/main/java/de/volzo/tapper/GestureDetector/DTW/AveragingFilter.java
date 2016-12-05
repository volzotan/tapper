package de.volzo.tapper.GestureDetector.DTW;

/**
 * Created by tassilokarge on 05.12.16.
 */

public class AveragingFilter extends StreamElement<Double> {

    AveragingFilter(Consumer<Double> averageConsumer) {
        super(averageConsumer);
    }

    /**
     * weighted averaging. averagingDivider should be the sum of all weights in the kernel
     * @param input the input
     * @param previousInputs the previous inputs (raw)
     * @param averagingKernel the kernel values for averaging
     * @param averagingDivider divider for the averaged values
     * @return the averaged output value
     */
    public void averaging(Double input, Double[] previousInputs, double[] averagingKernel, double averagingDivider) {
        double output = 0;
        if (previousInputs != null && previousInputs.length > averagingKernel.length) {
            for (int i = 0; i < averagingKernel.length - 2; i++) {
                Double prev = previousInputs[previousInputs.length-((averagingKernel.length-i)+1)];
                output += averagingKernel[i] * (prev != null ? prev : 0);
            }
            output += input * averagingKernel[averagingKernel.length - 1];
            output /= averagingDivider;
        }

        super.passProcessedElement(output);
    }
}
