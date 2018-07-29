package com.example.shiganyu.robotmdp;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by shiganyu on 6/2/18.
 */

public class Cell extends View {
    private boolean mBlock = false;
    private boolean mExplored = false;
    private boolean mFastestPath = false;
    private int mRow;
    private int mCol;

    public Cell(Context context) {
        super(context);
        init(null);
    }

    public Cell(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }


    public Cell(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);

    }


    private void init(AttributeSet attrs) {
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int color;

        if (mExplored) {
            if (mBlock)
                color = getResources().getColor(R.color.cellBlockColor);
            else
                color = getResources().getColor(R.color.cellExploredColor);
        } else {
            color = getResources().getColor(R.color.cellDafaultColor);
        }
        if(mFastestPath)
            color = getResources().getColor(R.color.fastestPathCellColor);
        canvas.drawColor(color);
    }

    public void block() {
        mBlock = true;
        postInvalidate();
    }

    public boolean hasExplored() {
        return mExplored;
    }

    public boolean isBlocked() {
        return mBlock;
    }

    public void explore() {
        mExplored = true;
        postInvalidate();
    }


    public void setCoordinate(int col, int row) {
        mRow = row;
        mCol = col;
    }

    public int getRow() {
        return mRow;
    }

    public int getCol() {
        return mCol;
    }

    public void setFastPath() {
        mFastestPath = true;
        postInvalidate();
    }
}
