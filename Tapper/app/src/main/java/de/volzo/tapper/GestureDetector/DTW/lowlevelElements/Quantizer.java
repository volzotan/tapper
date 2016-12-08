package de.volzo.tapper.GestureDetector.DTW.lowlevelElements;

import de.volzo.tapper.GestureDetector.DTW.StreamPassthrough;
import de.volzo.tapper.GestureDetector.DTW.StreamReceiver;

/**
 * Created by tassilokarge on 05.12.16.
 */

public class Quantizer extends StreamPassthrough<Integer, Double> {

    private final Double[] quantizationSteps;

    /**
     *
     * @param quantizationSteps the steps for the quantiles
     * @param quantileStreamReceiver receiver of quantized values
     */
    Quantizer(Double[] quantizationSteps, StreamReceiver<Integer> quantileStreamReceiver) {
        super(quantileStreamReceiver);
        this.quantizationSteps = quantizationSteps;
    }

    @Override
    public void process(Double input) {
        super.emitElement(quantize(input));
    }

    /**
     * quantizes input into major steps (nothing, peak, strong peak, very strong peak)
     * according to thresholds in quantizationSteps (lower or equal quantizationStep[i] yields i)
     *
     * @param input the unquantized input
     * @return the numerical value of the quantization step (i.e. 0 for nothing, 1 for peak etc.)
     */
    private int quantize(Double input) {
        // quantization
        double sign = input > 0 ? 1 : -1;
        double i = 0;
        while (i < quantizationSteps.length - 1 && quantizationSteps[(int)i+1] <= input) {
            i++;
        }

        return (int) (i * sign);
    }
}
