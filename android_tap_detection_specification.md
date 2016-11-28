# Considerations for Android Tap Detection App

## Gesture vocabulary

- Tap on back
- Doubletap on back
- Sidetap
- Pick up and drop

## Recognition

TODO: change pipeline and filter descriptions

Pipeline: Accelerometer -> Smoothing -> Collector -> Envelope curve -> Quantization -> Detector

### Smoothing

Smoothing with low-pass filter

Values on Z-Axis:
Values on X/Y-Axis:

### Collection

Collection of 128 samples in circular fifo queue

### Envelope curve

... (specifics about the algorithm)

### Quantization

3 categories:
TODO: limits

- Z
    + nothing: <= a
    + peak: > a, <=b
    + strong peak: > b, <= c
    + very strong peak: > c

- X/Y
    + nothing: <= a
    + peak: > a, <=b
    + strong peak: > b, <= c
    + very strong peak: > c

### Gesture detection

State machine for each gesture
TODO: change timeouts according to code
* Tap
    1. nothing
    2. nothing in X/Y axis and peak (<= 15ms) in Z axis
    3. nothing
* Doubletap
    1. Tap
    2. nothing (<100ms)
    3. Tap
* Sidetap
    1. nothing
    2. peak or strong peak on X or Y axis or both (< 50ms)
    3. nothing
* Pick up and drop
    1. nothing
    2. strong peak on Z axis (> 15ms)
    3. very strong peak on Z axis (< 10ms)
    4. nothing
