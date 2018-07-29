package com.example.shiganyu.robotmdp;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by shiganyu on 6/2/18.
 */

public class DotIndicators extends View {
    private final int NUM_OF_ROWS = getResources().getInteger(R.integer.num_of_rows);
    private final int NUM_OF_COLS = getResources().getInteger(R.integer.num_of_cols);


    public DotIndicators(Context context) {
        super(context);
        init(null);
    }

    public DotIndicators(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
    }


    @Override
    protected void onDraw(Canvas canvas) {
        for (int row = 0; row < NUM_OF_ROWS; row++) {
            for (int col = 0; col < NUM_OF_COLS; col++) {
                if (row * col == 0) {
                    continue;
                }
                DotIndicator dot = new DotIndicator(getContext(), row, col);
                dot.draw(canvas);
            }
        }
    }
}
