package de.volzo.tapper.GestureDetector.FSM;

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
}