package de.volzo.tapper.GestureDetector;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by volzotan on 14.11.16.
 */
public class Displayer extends View {

    private Context context;

    public Double[] x;
    public Double[] y;
    public Double[] z;

    // add here additional variables for additional graphs

    private Paint paint = new Paint();

    private float global_min = 0;
    private float global_max = 7; // enter value and comment lines 63-71 for fixed magnitude

    public Displayer(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;
    }

    public void clear() {
        this.x = null;
        this.y = null;
        this.z = null;
    }


    @Override
    protected void onDraw(Canvas canvas) {

        if (x == null) {
            return;
        }

        int height = canvas.getHeight();
        int width = canvas.getWidth();
        int elements = x.length;

        if (elements == 0) {
            return;
        }

        if (x[0] == null) {
            return;
        }

        int fractionHeight = height / 3; // Change if more graphs should be displayed

//        global_min = (float) (double) Collections.min(collector.x);
//        if (global_min > Collections.min(collector.y)) { global_min = (float) (double) Collections.min(collector.y); }
//        if (global_min > Collections.min(collector.z)) { global_min = (float) (double) Collections.min(collector.z); }
//        if (global_min > Collections.min(collector.m)) { global_min = (float) (double) Collections.min(collector.m); }
//        if (global_min > Collections.min(collector.rawx)) { global_min = (float) (double) Collections.min(collector.rawx); }
//        if (global_min > Collections.min(collector.rawy)) { global_min = (float) (double) Collections.min(collector.rawy); }
//        if (global_min > Collections.min(collector.rawz)) { global_min = (float) (double) Collections.min(collector.rawz); }
//
//        global_max = (float) (double) Collections.max(collector.x);
//        if (global_max < Collections.max(collector.y)) { global_max = (float) (double) Collections.max(collector.y); }
//        if (global_max < Collections.max(collector.z)) { global_max = (float) (double) Collections.max(collector.z); }
//        if (global_max < Collections.max(collector.m)) { global_max = (float) (double) Collections.max(collector.m); }
//        if (global_max < Collections.max(collector.rawx)) { global_max = (float) (double) Collections.max(collector.rawx); }
//        if (global_max < Collections.max(collector.rawy)) { global_max = (float) (double) Collections.max(collector.rawy); }
//        if (global_max < Collections.max(collector.rawz)) { global_max = (float) (double) Collections.max(collector.rawz); }
        
        drawLine(canvas, 0, width, fractionHeight * 0, fractionHeight * 1, x, Color.RED);
        drawLine(canvas, 0, width, fractionHeight * 1, fractionHeight * 2, y, Color.GREEN);
        drawLine(canvas, 0, width, fractionHeight * 2, fractionHeight * 3, z, Color.BLUE);

        //drawLine(canvas, 0, width, fractionHeight * 3, fractionHeight * 4, collector.am, Color.BLACK);

    }

    private void drawLine(Canvas canvas,
                          int leftEdge, int rightEdge,
                          int upperEdge, int lowerEdge, Double[] values,
                          int color) {

        paint.setColor(color);
        paint.setStrokeWidth(2.0f);

        //0 is in the middle of the drawing area
        float offset = (lowerEdge - upperEdge) / 2f;

        //set start to the middle of the left edge
        float lastX = leftEdge;
        float lastY = upperEdge + offset;

        //draw line from each previous point to current point
        for (int i = 0; i < values.length; i++) {
            //leave out null values
            if (values[i] == null) {continue;}

            //calculate x and y coordinates of line end
            //divide steps equally over width
            float xCoordinate = ((rightEdge - leftEdge) * i) / ((float) values.length);
            float yCoordinate = values[i].floatValue();

            // normalize
            yCoordinate = (yCoordinate - global_min) / (global_max - global_min);
            yCoordinate = yCoordinate * (lowerEdge - upperEdge) + upperEdge + offset;

            //draw line from last to current point
            canvas.drawLine(lastX, lastY, xCoordinate, yCoordinate, paint);

            lastX = xCoordinate;
            lastY = yCoordinate;
        }

    }

    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(200, 200);
    }


}
