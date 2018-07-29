package com.example.shiganyu.robotmdp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import static com.example.shiganyu.robotmdp.MainActivity.mArena;
import static com.example.shiganyu.robotmdp.MainActivity.mMode;

/**
 * Created by shiganyu on 6/2/18.
 */

enum Orientation {
    NORTH,
    EAST,
    SOUTH,
    WEST;
    private static Orientation[] vals = values();

    public Orientation next() {
        return vals[(this.ordinal() + 1) % vals.length];
    }

    public Orientation previous() {
        return vals[(this.ordinal() + vals.length - 1) % vals.length];
    }
}

public class Robot extends View {
    private static final String LOG_TAG = Robot.class.getName();
    private final float ROBOT_RADIUS = getResources().getDimension(R.dimen.robot_radius);
    private final float CELL_SIZE = getResources().getDimension(R.dimen.cell_size);
    private Paint mPaint;
    private int mX, mY;
    private Orientation mOrientation;
    int currentCellCount = 0;
    public int numOfCellsToExplr;

    public Robot(Context context) {
        super(context);
        init(null);
    }

    public Robot(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public Robot(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(getResources().getColor(R.color.robotColor));
        mX = 1;
        mY = 1;
        //mArena.setExplored(1,1);
        mOrientation = Orientation.EAST;

    }

    public Coordinate getCoordinate() {
        return new Coordinate(mX, mY);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float x = (CELL_SIZE) * (mX - 1) + ROBOT_RADIUS;
        float y = getHeight() - (CELL_SIZE * (mY - 1) + ROBOT_RADIUS);
        canvas.drawCircle(x, y, ROBOT_RADIUS, mPaint);
    }

    public void setmX(int mX) {
        this.mX = mX;
    }

    public void setmY(int mY) {
        this.mY = mY;
    }

    public int getXPos() {
        return mX;
    }

    public int getYPos() {
        return mY;
    }

    public void move(Direction direction, int numberOfGrid) {
        Log.d(LOG_TAG, "move: robot original: x: " + mX + " y: " + mY);
        mOrientation = calculateOrientaion(direction);
        forward(numberOfGrid);
        if (mMode == Mode.FASTESTPATH)
            mArena.setFastestPath(mX, mY);
        else {
            mArena.setExplored(mX, mY);

            currentCellCount = 0;
            for (int x = 0; x < 15; x++) {
                for (int y = 0; y < 20; y++) {
                    Cell c = mArena.getCell(x, y);
                    if (c.hasExplored()) {
                        currentCellCount++;
                    }
                }
            }

            MainActivity.cellCount = currentCellCount;
        }
        Log.d(LOG_TAG, "move: robot final: x: " + mX + " y: " + mY);
    }


    private void forward(int numberOfGrid) {
        switch (mOrientation) {
            case NORTH:
                mY += numberOfGrid;
                break;
            case SOUTH:
                mY -= numberOfGrid;
                break;
            case EAST:
                mX += numberOfGrid;
                break;
            case WEST:
                mX -= numberOfGrid;
                break;
        }
        postInvalidate();
    }

    public void moveAnimation(int numOfGrid) {
        Log.i(LOG_TAG, "move animation");
        Log.i(LOG_TAG, "X: " + mX + " Y: " + mY);
        float moveDist = numOfGrid * getResources().getDimension(R.dimen.cell_size);
        float toXDelta = 0;
        float toYDelta = 0;
        switch (mOrientation) {
            case NORTH:
                toYDelta += moveDist;
                break;
            case SOUTH:
                toYDelta -= moveDist;
                break;
            case EAST:
                toXDelta += moveDist;
                break;
            case WEST:
                toXDelta -= moveDist;
                break;
        }
        Animation animation = new TranslateAnimation(0, toXDelta, 0, toYDelta);
        animation.setDuration(500);
        animation.setFillAfter(true);
        startAnimation(animation);
    }

    public Orientation calculateOrientaion(Direction direction) {
        int newOrdinal = (mOrientation.ordinal() + direction.ordinal()) % 4;
        Orientation newOrientation = Orientation.values()[newOrdinal];
        return newOrientation;
    }

    public Orientation getOrientation() {
        return mOrientation;
    }

}
