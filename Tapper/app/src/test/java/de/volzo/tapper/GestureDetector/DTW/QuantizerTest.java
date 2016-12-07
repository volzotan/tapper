package de.volzo.tapper.GestureDetector.DTW;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by tassilokarge on 07.12.16.
 */
public class QuantizerTest {
    @Test
    public void process() throws Exception {
        ArrayList<Integer> processed = new ArrayList<>();
        Quantizer qt = new Quantizer(new Double[]{0d, 0.2, 0.5, 1.0, 1.5, 2.0, 3.0, 4.0, 5.0, 6.0, 8.0}, processed::add);
        qt.process(0.0);
        qt.process(0.3);
        qt.process(0.5);
        qt.process(1.2);
        qt.process(3.0);
        qt.process(5.0);
        System.out.println(Arrays.deepToString(processed.toArray()));
    }

}