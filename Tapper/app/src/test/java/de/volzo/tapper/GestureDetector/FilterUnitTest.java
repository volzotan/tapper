package de.volzo.tapper.GestureDetector;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Created by tassilokarge on 26.11.16.
 */
public class FilterUnitTest {

    Filter filter = new Filter();

    @Test
    public void workWithoutPreviousValue() throws Exception {
        //TODO: test filter pipeline
    }

    @Test
    public void workWithPreviousValue() throws Exception {
        //TODO: test filter pipeline
    }

    @Test
    public void lowpass() throws Exception {
        Double[] foo = {9d};
        Double[] foo2 = {10d};

        Assert.assertTrue("lowpass should smoothen curve", filter.lowpass(10d, foo) < 10d);
        Assert.assertTrue("lowpass should smoothen curve", filter.lowpass(9d, foo2) > 9d);
        Assert.assertTrue("lowpass should not change plateaus", filter.lowpass(10d, foo2) == 10d);
    }

    @Test
    public void cutoff() throws Exception {
        double threshold = filter.cutoffThreshold;

        Assert.assertEquals("0 should remain unchanged", 0d, filter.cutoff(0d));
        Assert.assertEquals("everything under threshold should be 0",
                0d, filter.cutoff((0 + threshold)/2d));
        Assert.assertEquals("threshold and above stay the same",
                threshold, filter.cutoff(threshold));
        Assert.assertEquals("threshold and above stay the same",
                threshold + 1, filter.cutoff(threshold + 1));
        Assert.assertEquals("threshold and above stay the same",
                threshold + 1.5, filter.cutoff(threshold + 1.5));
        Assert.assertEquals("threshold and above stay the same",
                threshold + 10, filter.cutoff(threshold + 10));
    }

    @Test
    public void quantize() throws Exception {
        double[] quantizationSteps = {0,1.5,2,4.03};

        //should be the case but atm not testable
        //Assert.assertEquals("Quantization steps and Quantiles should correspond",
        //        Quantile.values().length, quantizationSteps.length);

        Assert.assertEquals("Quantiles should start at value 0", 0d, quantizationSteps[0]);

        double previousQuantileThreshold = 0;

        for (int quantile = 1; quantile < quantizationSteps.length; quantile++) {
            double testValueBorder = previousQuantileThreshold;
            double testValueLow = previousQuantileThreshold + 0.0001;
            double testValueMiddle = (previousQuantileThreshold + quantizationSteps[quantile])/2d;
            double testValueHigh = quantizationSteps[quantile] - 0.0001;

            //positive
            Assert.assertEquals("Everything higher or equal previous threshold " +
                    "and lower current threshold should yield previous quantile index",
                    quantile - 1, filter.quantize(testValueLow, quantizationSteps).intValue());
            Assert.assertEquals("Everything higher or equal previous threshold " +
                            "and lower current threshold should yield previous quantile index",
                    quantile - 1, filter.quantize(testValueMiddle, quantizationSteps).intValue());
            Assert.assertEquals("Everything higher or equal previous threshold " +
                            "and lower current threshold should yield previous quantile index",
                    quantile - 1, filter.quantize(testValueHigh, quantizationSteps).intValue());
            Assert.assertEquals("Everything higher or equal previous threshold " +
                            "and lower current threshold should yield previous quantile index",
                    quantile - 1, filter.quantize(testValueBorder, quantizationSteps).intValue());

            //negative
            Assert.assertEquals("Everything lower or equal -previous threshold " +
                            "and higher -(current threshold) should yield previous quantile index",
                    -quantile + 1, filter.quantize(-testValueLow, quantizationSteps).intValue());
            Assert.assertEquals("Everything lower or equal -previous threshold " +
                            "and higher -(current threshold) should yield previous quantile index",
                    -quantile + 1, filter.quantize(-testValueMiddle, quantizationSteps).intValue());
            Assert.assertEquals("Everything lower or equal -previous threshold " +
                            "and higher -(current threshold) should yield previous quantile index",
                    -quantile + 1, filter.quantize(-testValueHigh, quantizationSteps).intValue());
            Assert.assertEquals("Everything lower or equal -previous threshold " +
                            "and higher -(current threshold) should yield previous quantile index",
                    -quantile + 1, filter.quantize(-testValueBorder, quantizationSteps).intValue());

            previousQuantileThreshold = quantizationSteps[quantile];
        }

        Assert.assertEquals(
                "Everything higher or equal previous threshold should yield previous quantile index",
                quantizationSteps.length - 1,
                filter.quantize(previousQuantileThreshold, quantizationSteps).intValue());
        Assert.assertEquals(
                "Everything higher or equal previous threshold should yield previous quantile index",
                quantizationSteps.length - 1,
                filter.quantize(previousQuantileThreshold + 0.5, quantizationSteps).intValue());
        Assert.assertEquals(
                "Everything higher or equal previous threshold should yield previous quantile index",
                quantizationSteps.length - 1,
                filter.quantize(previousQuantileThreshold + 10, quantizationSteps).intValue());

        Assert.assertEquals(
                "Everything higher or equal previous threshold should yield previous quantile index",
                -quantizationSteps.length + 1,
                filter.quantize(-previousQuantileThreshold, quantizationSteps).intValue());
        Assert.assertEquals(
                "Everything higher or equal previous threshold should yield previous quantile index",
                -quantizationSteps.length + 1,
                filter.quantize(-previousQuantileThreshold - 0.5, quantizationSteps).intValue());
        Assert.assertEquals(
                "Everything higher or equal previous threshold should yield previous quantile index",
                -quantizationSteps.length + 1,
                filter.quantize(-previousQuantileThreshold - 10, quantizationSteps).intValue());
    }
}