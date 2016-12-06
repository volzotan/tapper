package de.volzo.tapper.GestureDetector.DTW;

import org.apache.commons.collections4.queue.CircularFifoQueue;

/**
 * Created by tassilokarge on 06.12.16.
 */

public class FilteringPipeline {

    //private final Consumer<Double> filteredXConsumer;
    //private final Consumer<Double> filteredYConsumer;
    private final Consumer<Double> filteredXYConsumer;
    private final Consumer<Double> filteredZConsumer;

    //constants for filters
    private final double[] averagingKernel = {0.3, 0.3, 0.5, 0.8, 0.8, 0.5};
    private final double averagingDivider  = 3.2;
    private final double[] quantiles       = {0, 0.2, 0.5, 1.0, 1.5, 2.0, 3.0, 4.0, 5.0, 6.0, 8.0};

    //intermediate x value for combining x and y
    private Double absX;

    //intermediate values for averaging filter
    //private CircularFifoQueue<Double> previousInputsX = new CircularFifoQueue<>(averagingKernel.length + 1);
    //private CircularFifoQueue<Double> previousInputsY = new CircularFifoQueue<>(averagingKernel.length + 1);
    private CircularFifoQueue<Double> previousInputsXY = new CircularFifoQueue<>(averagingKernel.length + 1);
    private CircularFifoQueue<Double> previousInputsZ = new CircularFifoQueue<>(averagingKernel.length + 1);

    //start filter
    private final AbsoluteFilter absoluteFilterX;
    private final AbsoluteFilter absoluteFilterY;
    private final AbsoluteFilter absoluteFilterZ;


    FilteringPipeline(//Consumer<Double> filteredXConsumer,
            //Consumer<Double> filteredYConsumer,
            Consumer<Double> filteredXYConsumer,
            Consumer<Double> filteredZConsumer) {

        //this.filteredXConsumer = filteredXConsumer;
        //this.filteredYConsumer = filteredYConsumer;
        this.filteredXYConsumer = filteredXYConsumer;
        this.filteredZConsumer = filteredZConsumer;

        //QUANTIZER

        //Quantizer quantizerX = new Quantizer((output) -> filteredXConsumer.process(output));
        //Quantizer quantizerY = new Quantizer((output) -> filteredYConsumer.process(output));
        Quantizer quantizerXY = new Quantizer((output) -> filteredXYConsumer.process(output));
        Quantizer quantizerZ = new Quantizer((output) -> filteredZConsumer.process(output));


        //AVERAGING

        //AveragingFilter averagingFilterX = new AveragingFilter((output) -> quantizerX.quantize(output, quantiles));
        //AveragingFilter averagingFilterY = new AveragingFilter((output) -> quantizerY.quantize(output, quantiles));
        AveragingFilter averagingFilterXY = new AveragingFilter((output) -> quantizerXY.quantize(output, quantiles));
        AveragingFilter averagingFilterZ = new AveragingFilter((output) -> quantizerZ.quantize(output, quantiles));


        //ABSOLUTE

        final Double[] d = new Double[0];
        //this.previousInputsX.add(output);
        //averagingFilterX.averaging(output, previousInputsX.toArray(d), averagingKernel, averagingDivider);
        absoluteFilterX = new AbsoluteFilter((output) -> {
            //this.previousInputsX.add(output);
            //averagingFilterX.averaging(output, previousInputsX.toArray(d), averagingKernel, averagingDivider);
            absX = output;
        });
        //this.previousInputsY.add(output);
        //averagingFilterY.averaging(output, previousInputsY.toArray(d), averagingKernel, averagingDivider);
        absoluteFilterY = new AbsoluteFilter((output) -> {
            //this.previousInputsY.add(output);
            //averagingFilterY.averaging(output, previousInputsY.toArray(d), averagingKernel, averagingDivider);
            Double xy = Math.sqrt(Math.pow(absX, 2) + Math.pow(output, 2));
            this.previousInputsXY.add(xy);
            averagingFilterXY.averaging(xy, previousInputsXY.toArray(d), averagingKernel, averagingDivider);
        });
        absoluteFilterZ = new AbsoluteFilter((output) -> {
            this.previousInputsZ.add(output);
            averagingFilterZ.averaging(output, previousInputsZ.toArray(d), averagingKernel, averagingDivider);
        });
    }

    public void filter(Double x, Double y, Double z) {
        //kickoff filtering process
        this.absoluteFilterX.absolute(x);
        this.absoluteFilterY.absolute(y);
        this.absoluteFilterZ.absolute(z);
    }
}
