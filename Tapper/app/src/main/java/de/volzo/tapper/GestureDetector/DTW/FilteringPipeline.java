package de.volzo.tapper.GestureDetector.DTW;

import de.volzo.tapper.GestureDetector.DTW.lowlevelElements.AbsoluteFilter;
import de.volzo.tapper.GestureDetector.DTW.lowlevelElements.AveragingFilter;
import de.volzo.tapper.GestureDetector.DTW.lowlevelElements.CutoffHighFilter;
import de.volzo.tapper.GestureDetector.DTW.lowlevelElements.CutoffLowFilter;
import de.volzo.tapper.GestureDetector.DTW.streamSystem.StreamPassthrough;
import de.volzo.tapper.GestureDetector.DTW.streamSystem.StreamReceiver;

/**
 * Created by tassilokarge on 06.12.16.
 */

public class FilteringPipeline extends StreamPassthrough<Number[], Double[]> {

    //constants for filters
    private final double[] averagingKernel = {0.1, 0.2, 0.3, 0.4, 0.6, 0.4};
    private final double averagingDivider  = 2.0;
    private final Double[] quantiles       = {0d, 0.2, 0.5, 1.0, 1.5, 2.0, 3.0, 4.0, 5.0, 6.0, 8.0};
    private final double cutoffHigh = 10;
    private final double cutoffLow = 0.04;

    //intermediate x value for combining x and y
    private Double absX;

    //intermediate value for combining quantized xy and z into array
    private Double outputXY;
    private Double outputZ;

    //start filter
    private final AbsoluteFilter absoluteFilterX;
    private final AbsoluteFilter absoluteFilterY;
    private final AbsoluteFilter absoluteFilterZ;


    public FilteringPipeline(StreamReceiver<Number[]> filteredStreamReceiver) {

        super(filteredStreamReceiver);

        //QUANTIZER

        //Quantizer quantizerXY = new Quantizer(quantiles, (output) -> outputXY = output);
        //Quantizer quantizerZ = new Quantizer(quantiles, (output) -> outputZ = output);


        //AVERAGING

        AveragingFilter averagingFilterXY = new AveragingFilter(averagingKernel, averagingDivider, (output) -> outputXY = output /*quantizerXY*/);
        AveragingFilter averagingFilterZ = new AveragingFilter(averagingKernel, averagingDivider, (output) -> outputZ = output/*quantizerZ*/);

        //CUTOFF
        CutoffHighFilter cutoffFilterXY = new CutoffHighFilter(cutoffHigh, new CutoffLowFilter(cutoffLow, averagingFilterXY));
        CutoffHighFilter cutoffFilterZ = new CutoffHighFilter(cutoffHigh, new CutoffLowFilter(cutoffLow, averagingFilterZ));

        //ABSOLUTE
        absoluteFilterX = new AbsoluteFilter((output) -> absX = output);
        absoluteFilterY = new AbsoluteFilter((output) -> {
            Double xy = Math.sqrt(Math.pow(absX, 2) + Math.pow(output, 2));
            cutoffFilterXY.process(xy);
        });
        absoluteFilterZ = new AbsoluteFilter(cutoffFilterZ);
    }

    @Override
    public void process(Double[] input) {
        super.emitElement(filter(input));
    }

    private Double[] filter(Double[] xyz) {
        //kickoff filtering process
        this.absoluteFilterX.process(xyz[0]);
        this.absoluteFilterY.process(xyz[1]);
        this.absoluteFilterZ.process(xyz[2]);

        return new Double[]{outputXY, outputZ};
    }
}
