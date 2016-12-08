package de.volzo.tapper.GestureDetector.DTW;

import de.volzo.tapper.GestureDetector.DTW.lowlevelElements.AbsoluteFilter;
import de.volzo.tapper.GestureDetector.DTW.lowlevelElements.AveragingFilter;
import de.volzo.tapper.GestureDetector.DTW.lowlevelElements.Quantizer;
import de.volzo.tapper.GestureDetector.DTW.streamSystem.StreamPassthrough;
import de.volzo.tapper.GestureDetector.DTW.streamSystem.StreamReceiver;

/**
 * Created by tassilokarge on 06.12.16.
 */

public class FilteringPipeline extends StreamPassthrough<Integer[], Double[]> {

    //constants for filters
    private final double[] averagingKernel = {0.3, 0.3, 0.5, 0.8, 0.8, 0.5};
    private final double averagingDivider  = 3.2;
    private final Double[] quantiles       = {0d, 0.2, 0.5, 1.0, 1.5, 2.0, 3.0, 4.0, 5.0, 6.0, 8.0};

    //intermediate x value for combining x and y
    private Double absX;

    //intermediate value for combining quantized xy and z into array
    private Integer quantizedXY;
    private Integer quantizedZ;

    //start filter
    private final AbsoluteFilter absoluteFilterX;
    private final AbsoluteFilter absoluteFilterY;
    private final AbsoluteFilter absoluteFilterZ;


    public FilteringPipeline(StreamReceiver<Integer[]> filteredStreamReceiver) {

        super(filteredStreamReceiver);

        //QUANTIZER

        Quantizer quantizerXY = new Quantizer(quantiles, (output) -> quantizedXY = output);
        Quantizer quantizerZ = new Quantizer(quantiles, (output) -> quantizedZ = output);


        //AVERAGING

        AveragingFilter averagingFilterXY = new AveragingFilter(averagingKernel, averagingDivider, quantizerXY);
        AveragingFilter averagingFilterZ = new AveragingFilter(averagingKernel, averagingDivider, quantizerZ);


        //ABSOLUTE
        absoluteFilterX = new AbsoluteFilter((output) -> absX = output);
        absoluteFilterY = new AbsoluteFilter((output) -> {
            Double xy = Math.sqrt(Math.pow(absX, 2) + Math.pow(output, 2));
            averagingFilterXY.process(xy);
        });
        absoluteFilterZ = new AbsoluteFilter(averagingFilterZ);
    }

    @Override
    public void process(Double[] input) {
        super.emitElement(filter(input));
    }

    private Integer[] filter(Double[] xyz) {
        //kickoff filtering process
        this.absoluteFilterX.process(xyz[0]);
        this.absoluteFilterY.process(xyz[1]);
        this.absoluteFilterZ.process(xyz[2]);

        return new Integer[]{quantizedXY, quantizedZ};
    }
}
