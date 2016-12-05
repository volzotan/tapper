package de.volzo.tapper.GestureDetector.DTW;

/**
 * Created by tassilokarge on 05.12.16.
 */

public class Quantizer extends StreamElement<Double> {

    Quantizer(Consumer<Double> quantileConsumer) {
        super(quantileConsumer);
    }

    /**
     * quantizes input into major steps (nothing, peak, strong peak, very strong peak)
     * according to thresholds in quantizationSteps (lower or equal quantizationStep[i] yields i)
     *
     * @param input the unquantized input
     * @param quantizationSteps the steps for the quantiles
     * @return the numerical value of the quantization step (i.e. 0 for nothing, 1 for peak etc.)
     */
    public void quantize(Double input, double[] quantizationSteps) {
        // quantization
        double sign = input > 0 ? 1 : -1;
        double i = 0;
        while (i < quantizationSteps.length - 1 && quantizationSteps[(int)i+1] <= input) {
            i++;
        }

        super.passProcessedElement(i * sign);
    }
}
