package de.volzo.tapper.GestureDetector.DTW.streamSystem;

/**
 * Created by tassilokarge on 05.12.16.
 */

public abstract class StreamEmitter<T> {

    private StreamReceiver<T> streamReceiver;

    public StreamEmitter(StreamReceiver<T> streamReceiver) {
        this.streamReceiver = streamReceiver;
    }

    public void emitElement(T element) {
        streamReceiver.process(element);
    }
}
