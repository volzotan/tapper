package de.volzo.tapper.GestureDetector.FSM;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by tassilokarge on 27.11.16.
 */
public class FSMPickUpDropTest extends FSMUnitTest {

    private FSMPickUpDrop fsm;

    @Before
    public void setupFSM() throws Exception {
        this.fsm = new FSMPickUpDrop();
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
                FSMPickUpDrop.PickUpDropState.START, fsm.getCurrentPickUpDropState());
    }

    private void startOrPeakToPeak() {
        //Start -> Peak or Peak -> Peak
        Assert.assertFalse("FSM should not be in end state yet",
                fsm.stateTransition(
                        EVENT_STRONG_PEAK_Z
                ));
        Assert.assertEquals("Machine should now be in Peak state",
                FSMPickUpDrop.PickUpDropState.PEAK, fsm.getCurrentPickUpDropState());
    }

    private void peakOrPeak2ToPeak2() {
        //Peak -> Peak2 or Peak2 -> Peak2
        Assert.assertFalse("FSM should not be in end state yet",
                fsm.stateTransition(
                        EVENT_VERY_STRONG_PEAK_Z
                ));
        Assert.assertEquals("Machine should now be in Peak2 state",
                FSMPickUpDrop.PickUpDropState.PEAK2, fsm.getCurrentPickUpDropState());
    }


    private void peak2ToEndByNothing() {
        Assert.assertTrue("FSM should now be in end state",
                fsm.stateTransition(
                        EVENT_NOTHING
                ));
        Assert.assertEquals("Machine should now be in END state",
                FSMPickUpDrop.PickUpDropState.END, fsm.getCurrentPickUpDropState());
    }
}