package de.volzo.tapper.GestureDetector.DTW;

/**
 * Created by tassilokarge on 05.12.16.
 */

public class StreamElement<T> {

    Consumer<T> consumer;

    StreamElement(Consumer<T> consumer) {
        this.consumer = consumer;
    }

    void passProcessedElement(T element) {
        consumer.process(element);
    }
}
