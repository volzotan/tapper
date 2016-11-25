package de.volzo.tapper;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import de.volzo.tapper.GestureDetector.FSM.DoubleTapFSM;

import static de.volzo.tapper.GestureDetector.FSM.DoubleTapFSM.doubleTapTimeout;

/**
 * Created by tassilokarge on 24.11.16.
 */

public class DoubleTapFSMUnitTest extends FSMUnitTest {
    private DoubleTapFSM fsm;

    @Before
    public void setupFSM() {
        this.fsm = new DoubleTapFSM();
    }

    //success

    @Test
    public void doubleTap() {
        Assert.assertEquals("fresh machine should be in INIT state",
                DoubleTapFSM.DoubleTapState.INIT, fsm.getCurrentDoubleTapState());
        //Initial -> Tap1
        initToTap1();
        //Tap1 -> Tap1
        tap1ToTap1();
        //Tap1 -> Tap2
        tap1ToTap2();
    }

    //failure

    public void stayInInit() {
        toInitByStrongZMovement();
        toInitByXYShake();
    }

    @Test
    public void xyShake() {
        //Initial -> Tap1
        initToTap1();
        //Tap1 -> Initial
        toInitByXYShake();
    }

    @Test
    public void tooStrongTap() {
        //Initial -> Tap1
        initToTap1();
        //Tap1 -> Initial
        toInitByStrongZMovement();
    }

    @Test
    public void timeout() throws InterruptedException {
        //Initial -> Tap1
        initToTap1();
        //Tap1 -> Initial
        toInitByTimeout();
    }

    private void initToTap1() {
        Assert.assertEquals("FSM has to be in INIT state for going to Tap1",
                DoubleTapFSM.DoubleTapState.INIT, fsm.getCurrentDoubleTapState());
        Assert.assertFalse("FSM should not go to end state from INIT",
                fsm.stateTransition(
                        EVENT_TAP
                ));
        Assert.assertEquals("Machine should now be in TAP1 state",
                DoubleTapFSM.DoubleTapState.TAP1, fsm.getCurrentDoubleTapState());
    }

    private void tap1ToTap1() {
        Assert.assertEquals("FSM has to be in Tap1 state for remaining in Tap1",
            DoubleTapFSM.DoubleTapState.TAP1, fsm.getCurrentDoubleTapState());
        Assert.assertFalse("FSM should not be in end state yet",
                fsm.stateTransition(
                        EVENT_NOTHING
                ));
        Assert.assertEquals("Machine should still be in Tap1 state",
                DoubleTapFSM.DoubleTapState.TAP1, fsm.getCurrentDoubleTapState());
    }

    private void tap1ToTap2() {
        Assert.assertEquals("FSM has to be in Tap1 state for going to Tap2",
                DoubleTapFSM.DoubleTapState.TAP1, fsm.getCurrentDoubleTapState());
        Assert.assertTrue("FSM should go from Tap1 to final state Tap2",
                fsm.stateTransition(
                        EVENT_TAP
                ));
        Assert.assertEquals("Machine should now be in TAP2 state",
                DoubleTapFSM.DoubleTapState.TAP2, fsm.getCurrentDoubleTapState());
    }

    private void toInitByXYShake() {
        Assert.assertFalse("FSM should not go to end state when shaking in X/Y axis",
                fsm.stateTransition(
                        EVENT_PEAK_XY
                ));
        Assert.assertEquals("Machine should now be in INIT state",
                DoubleTapFSM.DoubleTapState.INIT, fsm.getCurrentDoubleTapState());
    }

    private void toInitByStrongZMovement() {
        Assert.assertFalse("FSM should not go to end state when shaking in X/Y axis",
                fsm.stateTransition(
                        EVENT_STRONG_PEAK_Z
                ));
        Assert.assertEquals("Machine should now be in INIT state",
                DoubleTapFSM.DoubleTapState.INIT, fsm.getCurrentDoubleTapState());
    }

    private void toInitByTimeout() throws InterruptedException {
        //timeout can only occur on Tap1 state
        Assert.assertEquals("FSM has to be in Tap1 state for timeout",
                DoubleTapFSM.DoubleTapState.TAP1, fsm.getCurrentDoubleTapState());
        //timeout should occur after 500ms, additional 5ms account for threading delays
        Thread.sleep(doubleTapTimeout + 5);
        Assert.assertEquals("Machine should now be in INIT state",
                DoubleTapFSM.DoubleTapState.INIT, fsm.getCurrentDoubleTapState());
    }
}
