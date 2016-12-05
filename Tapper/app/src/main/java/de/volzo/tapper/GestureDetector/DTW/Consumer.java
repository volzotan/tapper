package de.volzo.tapper.GestureDetector.DTW;

/**
 * Created by tassilokarge on 05.12.16.
 */

@FunctionalInterface
public interface Consumer<T> {
    void process(T object);
}
