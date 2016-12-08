package de.volzo.tapper.GestureDetector.DTW.lowlevelElements;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import de.volzo.tapper.GestureDetector.DTW.StreamPassthrough;
import de.volzo.tapper.GestureDetector.DTW.StreamReceiver;

/**
 * Created by tassilokarge on 05.12.16.
 */

public class AveragingFilter extends StreamPassthrough<Double, Double> {

    private final double[] averagingKernel;
    private final double averagingDivider;
    private final CircularFifoQueue<Double> previousInputs;

    /**
     *
     * @param averagingKernel the kernel values for averaging
     * @param averagingDivider divider for the averaged values
     * @param averageStreamReceiver the receiver of the processed values
     */
    AveragingFilter(double[] averagingKernel, double averagingDivider, StreamReceiver<Double> averageStreamReceiver) {
        super(averageStreamReceiver);
        this.averagingKernel = averagingKernel;
        this.averagingDivider = averagingDivider;
        previousInputs = new CircularFifoQueue<>(averagingKernel.length + 1);
    }

    @Override
    public void process(Double input) {
        super.emitElement(averaging(input));
    }

    /**
     * weighted averaging. averagingDivider should be the sum of all weights in the kernel
     * @param input the input
     * @return the averaged output value
     */
    private Double averaging(Double input) {

        this.previousInputs.add(input);

        Double output = 0d;
        if (previousInputs.size() > averagingKernel.length) {
            for (int i = 0; i < averagingKernel.length - 2; i++) {
                Double prev = previousInputs.get(previousInputs.size()-((averagingKernel.length-i)+1));
                output += averagingKernel[i] * (prev != null ? prev : 0);
            }
            output += input * averagingKernel[averagingKernel.length - 1];
            output /= averagingDivider;
        }

        return output;
    }
}
