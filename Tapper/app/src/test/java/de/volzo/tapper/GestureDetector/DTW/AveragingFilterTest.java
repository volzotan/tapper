package de.volzo.tapper.GestureDetector.DTW;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by tassilokarge on 07.12.16.
 */
public class AveragingFilterTest {

    private final double[] averagingKernel = {0.3, 0.3, 0.5, 0.8, 0.8, 0.5};
    private final double averagingDivider  = 3.2;

    @Test
    public void process() throws Exception {
        ArrayList<Double> processed = new ArrayList<>();
        AveragingFilter av = new AveragingFilter(averagingKernel, averagingDivider, processed::add);
        av.process(0.0);
        av.process(0.0);
        av.process(0.3);
        av.process(0.5);
        av.process(1.2);
        av.process(3.0);
        av.process(5.0);
        av.process(8.0);
        av.process(5.0);
        av.process(3.0);
        av.process(1.2);
        av.process(0.5);
        av.process(0.3);
        av.process(0.0);
        av.process(0.0);
        System.out.println(Arrays.deepToString(processed.toArray()));
    }

}