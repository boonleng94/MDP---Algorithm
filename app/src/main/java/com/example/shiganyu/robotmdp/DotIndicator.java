package com.example.shiganyu.robotmdp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.View;

/**
 * Created by shiganyu on 22/1/18.
 */

public class DotIndicator extends View{
    private static final String LOG_TAG = DotIndicator.class.getName();
    private ShapeDrawable mDotView;

    private int row;
    private int col;

    private final int RADIUS = (int) getResources().getDimension(R.dimen.dot_radius);
    private final int SIDE = (int) getResources().getDimension(R.dimen.cell_size);


    public DotIndicator(Context context, int row, int col) {
        super(context);
        this.row = row;
        this.col = col;
    }

    protected void onDraw(Canvas canvas) {
        int top = row * SIDE - RADIUS;
        int bottom = row * SIDE + RADIUS;
        int left = col * SIDE - RADIUS;
        int right = col * SIDE + RADIUS;

        mDotView = new ShapeDrawable(new OvalShape());
        mDotView.getPaint().setColor(Color.WHITE);
        mDotView.setBounds(left, top, right, bottom);
        mDotView.draw(canvas);


    }
}
