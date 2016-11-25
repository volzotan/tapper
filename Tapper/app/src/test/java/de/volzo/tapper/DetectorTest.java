package de.volzo.tapper;

import junit.framework.Assert;

import org.junit.Test;

import de.volzo.tapper.GestureDetector.Detector;
import de.volzo.tapper.GestureDetector.Quantile;

/**
 * Created by tassilokarge on 24.11.16.
 */

public class DetectorTest extends Detector {

    private boolean doubleTapRecognized, sideTapRecognized, pickUpDropRecognized;

    @Test
    public void doubleTapDetection() {
        //input double tap sequence(s) to dataUpdated and check if doubleTapRecognized is set

        //first tap
        this.dataUpdated(Quantile.NOTHING, Quantile.NOTHING, Quantile.NOTHING);
        this.dataUpdated(Quantile.NOTHING, Quantile.NOTHING, Quantile.PEAK);
        this.dataUpdated(Quantile.NOTHING, Quantile.NOTHING, Quantile.NOTHING);

        //second tap
        this.dataUpdated(Quantile.NOTHING, Quantile.NOTHING, Quantile.NOTHING);
        this.dataUpdated(Quantile.NOTHING, Quantile.NOTHING, Quantile.PEAK);
        this.dataUpdated(Quantile.NOTHING, Quantile.NOTHING, Quantile.NOTHING);

        Assert.assertTrue("Detector should detect double tap", this.doubleTapRecognized);
    }

    @Test
    public void sideTapDetection() {
        //input double tap sequence(s) to dataUpdated and check if sideTapRecognized is set
        // first tap
        this.dataUpdated(Quantile.NOTHING, Quantile.NOTHING, Quantile.NOTHING);
        this.dataUpdated(Quantile.NOTHING, Quantile.PEAK, Quantile.NOTHING);
        this.dataUpdated(Quantile.NOTHING, Quantile.NOTHING, Quantile.NOTHING);

        Assert.assertTrue("Detector should detect sidetap", this.sideTapRecognized);

        // second tap
        this.dataUpdated(Quantile.NOTHING, Quantile.NOTHING, Quantile.NOTHING);
        this.dataUpdated(Quantile.NOTHING, Quantile.STRONG_PEAK, Quantile.NOTHING);
        this.dataUpdated(Quantile.NOTHING, Quantile.NOTHING, Quantile.NOTHING);

        Assert.assertTrue("Detector should detect sidetap", this.sideTapRecognized);

        // third tap
        this.dataUpdated(Quantile.NOTHING, Quantile.NOTHING, Quantile.NOTHING);
        this.dataUpdated(Quantile.PEAK, Quantile.NOTHING, Quantile.NOTHING);
        this.dataUpdated(Quantile.NOTHING, Quantile.NOTHING, Quantile.NOTHING);

        Assert.assertTrue("Detector should detect sidetap", this.sideTapRecognized);

        // fourth tap
        this.dataUpdated(Quantile.NOTHING, Quantile.NOTHING, Quantile.NOTHING);
        this.dataUpdated(Quantile.STRONG_PEAK, Quantile.NOTHING, Quantile.NOTHING);
        this.dataUpdated(Quantile.NOTHING, Quantile.NOTHING, Quantile.NOTHING);

        Assert.assertTrue("Detector should detect sidetap", this.sideTapRecognized);

        // fifth tap
        this.dataUpdated(Quantile.NOTHING, Quantile.NOTHING, Quantile.NOTHING);
        this.dataUpdated(Quantile.PEAK, Quantile.PEAK, Quantile.NOTHING);
        this.dataUpdated(Quantile.NOTHING, Quantile.NOTHING, Quantile.NOTHING);

        Assert.assertTrue("Detector should detect sidetap", this.sideTapRecognized);

        // sixth tap
        this.dataUpdated(Quantile.NOTHING, Quantile.NOTHING, Quantile.NOTHING);
        this.dataUpdated(Quantile.STRONG_PEAK, Quantile.STRONG_PEAK, Quantile.NOTHING);
        this.dataUpdated(Quantile.NOTHING, Quantile.NOTHING, Quantile.NOTHING);

        Assert.assertTrue("Detector should detect sidetap", this.sideTapRecognized);
    }

    @Test
    public void pickUpDropDetection() {
        //TODO: input double tap sequence(s) to dataUpdated and check if pickUpDropRecognized is set
    }

    @Override
    public void handleRecognizedGestures(boolean doubleTapRecognized, boolean sideTapRecognized, boolean pickUpDropRecognized) {
        this.doubleTapRecognized = doubleTapRecognized;
        this.sideTapRecognized = sideTapRecognized;
        this.pickUpDropRecognized = pickUpDropRecognized;
    }
}
