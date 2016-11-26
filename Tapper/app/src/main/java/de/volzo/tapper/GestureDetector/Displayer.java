package de.volzo.tapper.GestureDetector;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.provider.ContactsContract;
import android.util.AttributeSet;
import android.view.View;

import java.util.Collections;

import de.volzo.tapper.MainActivity;

/**
 * Created by volzotan on 14.11.16.
 */
public class Displayer extends View {

    private Context context;
    private MainActivity main;
    private DataCollector collector;

    private Paint paint = new Paint();

    private float global_min = 0;
    private float global_max = 7; // enter value and comment lines 63-71 for fixed magnitude

    public Displayer(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;
    }


    @Override
    protected void onDraw(Canvas canvas) {

        if (main == null) {
            try {
                collector = ((MainActivity) context).dataCollector;

                paint.setColor(Color.RED);
            } catch (Exception ce) {
                return;
            }
        }

        int height = canvas.getHeight();
        int width = canvas.getWidth();
        int elements = collector.ax.length;

        if (elements == 0) {
            return;
        }

        if (collector.ax[0] == null) {
            return;
        }

        int fractionHeight = height / 6;

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

        drawLine(canvas, 0, width, fractionHeight * 0, fractionHeight * 1, collector.ax, Color.RED);
        drawLine(canvas, 0, width, fractionHeight * 1, fractionHeight * 2, collector.rawax, Color.RED);
        drawLine(canvas, 0, width, fractionHeight * 2, fractionHeight * 3, collector.ay, Color.BLUE);
        drawLine(canvas, 0, width, fractionHeight * 3, fractionHeight * 4, collector.raway, Color.BLUE);
        drawLine(canvas, 0, width, fractionHeight * 4, fractionHeight * 5, collector.az, Color.BLACK);
        drawLine(canvas, 0, width, fractionHeight * 5, fractionHeight * 6, collector.rawaz, Color.BLACK);
        //drawLine(canvas, 0, width, fractionHeight * 3, fractionHeight * 4, collector.am, Color.BLACK);

    }

    private void drawLine(Canvas canvas, int x1, int x2, int y1, int y2, Double[] v, int color) {
        paint.setColor(color);
        int offset = (y2 - y1) / 2;

        float lastX = x1;
        float lastY = y1 + offset;

        int step = (x2 - x1) / v.length;

        for (int i = 0; i < v.length; i++) {
            if (v[i] == null) {continue;}

            float vX = i * step;
            float vY = (float) (double) v[i];

            // normalize
            vY = (vY - global_min) / (global_max - global_min);
            vY = vY * (y2 - y1) + y1;

            canvas.drawLine(lastX, lastY, vX, vY, paint);

            lastX = vX;
            lastY = vY;
        }

    }

    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(200, 200);
    }


}
