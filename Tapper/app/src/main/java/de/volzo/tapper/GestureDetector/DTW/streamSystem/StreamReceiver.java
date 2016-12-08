package de.volzo.tapper.GestureDetector.DTW.streamSystem;

/**
 * Created by tassilokarge on 05.12.16.
 */

@FunctionalInterface
public interface StreamReceiver<T> {
    void process(T input);
}
