package de.volzo.tapper.GestureDetector.FSM;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by tassilokarge on 27.11.16.
 */
public class PickUpDropFSMTest extends FSMUnitTest {

    private PickUpDropFSM fsm;

    @Before
    public void setupFSM() throws Exception {
        this.fsm = new PickUpDropFSM();
    }

    //success

    @Test
    public void pickUpDrop() {
        initialOrStartToStart();
        initialOrStartToStart();
        startOrPeakToPeak();
        startOrPeakToPeak();
        peakOrPeak2ToPeak2();
        peakOrPeak2ToPeak2();
        peak2ToEndByNothing();
    }

    //failure

    @Test
    public void timeoutPickUp() {

    }

    @Test
    public void timeoutDrop() {

    }

    @Test
    public void strongXYShake() {

    }

    @Test
    public void noDrop() {

    }

    @Test
    public void noPickup() {

    }


    private void initialOrStartToStart() {
        //Initial -> Start or Start -> Start
        Assert.assertFalse("FSM should not be in end state yet",
                fsm.stateTransition(
                        EVENT_NOTHING
                ));
        Assert.assertEquals("Machine should now be in Start state",
                PickUpDropFSM.PickUpDropState.START, fsm.getCurrentPickUpDropState());
    }

    private void startOrPeakToPeak() {
        //Start -> Peak or Peak -> Peak
        Assert.assertFalse("FSM should not be in end state yet",
                fsm.stateTransition(
                        EVENT_STRONG_PEAK_Z
                ));
        Assert.assertEquals("Machine should now be in Peak state",
                PickUpDropFSM.PickUpDropState.PEAK, fsm.getCurrentPickUpDropState());
    }

    private void peakOrPeak2ToPeak2() {
        //Peak -> Peak2 or Peak2 -> Peak2
        Assert.assertFalse("FSM should not be in end state yet",
                fsm.stateTransition(
                        EVENT_VERY_STRONG_PEAK_Z
                ));
        Assert.assertEquals("Machine should now be in Peak2 state",
                PickUpDropFSM.PickUpDropState.PEAK2, fsm.getCurrentPickUpDropState());
    }


    private void peak2ToEndByNothing() {
        Assert.assertTrue("FSM should now be in end state",
                fsm.stateTransition(
                        EVENT_NOTHING
                ));
        Assert.assertEquals("Machine should now be in END state",
                PickUpDropFSM.PickUpDropState.END, fsm.getCurrentPickUpDropState());
    }
}