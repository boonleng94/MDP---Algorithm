package com.example.shiganyu.robotmdp;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.security.InvalidParameterException;

import static com.example.shiganyu.robotmdp.MainActivity.mArena;
import static com.example.shiganyu.robotmdp.MainActivity.mRobot;
import static com.example.shiganyu.robotmdp.MainActivity.mSensorMgr;

/**
 * Created by shiganyu on 6/2/18.
 */

public class Arena extends ViewGroup {

    private static final String LOG_TAG = Arena.class.getName();
    private final int SENSOR_RANGE = getResources().getInteger(R.integer.sensor_range);
    private final int NUM_OF_ROWS = getResources().getInteger(R.integer.num_of_rows);
    private final int NUM_OF_COLS = getResources().getInteger(R.integer.num_of_cols);
    public final int NUM_OF_SENSORS = getResources().getInteger(R.integer.num_of_sensors);


    public Arena(Context context) {
        super(context);
        init(null);

    }

    public Arena(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {

    }

    public void initCell() {
        for (int row = 0; row < NUM_OF_ROWS; row++) {
            for (int col = 0; col < NUM_OF_COLS; col++) {
                Cell cell = getCell(col, row);
                cell.setCoordinate(col, row);
            }
        }
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int row, col, left, top;
        for (int i = 0; i < getChildCount(); i++) {
            row = i / NUM_OF_COLS;
            col = i % NUM_OF_COLS;
            View child = getChildAt(i);
            left = col * child.getMeasuredWidth();
            top = row * child.getMeasuredHeight();

            child.layout(left, top, left + child.getMeasuredWidth(), top + child.getMeasuredHeight());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize, heightSize;

        widthSize = getDefaultSize(0, widthMeasureSpec);
        heightSize = getDefaultSize(0, heightMeasureSpec);

        int blockDimension = widthSize / NUM_OF_COLS;
        int blockSpec = MeasureSpec.makeMeasureSpec(blockDimension, MeasureSpec.EXACTLY);
        measureChildren(blockSpec, blockSpec);
        setMeasuredDimension(widthSize, heightSize);
    }

    public boolean notBlocked(Direction direction) {

        Orientation orientation = mRobot.calculateOrientaion(direction);
        if (notBlocked(orientation)) {
            return true;
        } else {
            return false;
        }

    }

    public boolean notBlocked(Orientation orientation) {
        Coordinate robotCoordinate = mRobot.getCoordinate();
        Coordinate coordinate1 = robotCoordinate.getNeighbour(orientation).getNeighbour(orientation);
        Coordinate coordinate0 = coordinate1.getNeighbour(orientation.previous());
        Coordinate coordinate2 = coordinate1.getNeighbour(orientation.next());

        boolean pos0Blocked = mArena.getCell(coordinate0).isBlocked();
        boolean pos1Blocked = mArena.getCell(coordinate1).isBlocked();
        boolean pos2Blocked = mArena.getCell(coordinate2).isBlocked();

        if (pos0Blocked || pos1Blocked || pos2Blocked) {
            Log.d(LOG_TAG, "notBlocked: BLOCKED");
            return false;
        } else {
            Log.d(LOG_TAG, "notBlocked: NOT BLOCKED");
            return true;
        }
    }

    public void setExplored(int x, int y) {
            //left most col
            getCell(x - 1, y - 1).explore();
            getCell(x - 1, y).explore();
            getCell(x - 1, y + 1).explore();

            //middle col
            getCell(x, y - 1).explore();
            getCell(x, y).explore();
            getCell(x, y + 1).explore();

            //right col
            getCell(x + 1, y - 1).explore();
            getCell(x + 1, y).explore();
            getCell(x + 1, y + 1).explore();
    }

    public int getNumOfRows() {
        return NUM_OF_ROWS;
    }

    public int getNumOfCols() {
        return NUM_OF_COLS;
    }

    public Cell getCell(int x, int y) {
        if (x < 0 || y < 0 || x > NUM_OF_COLS || y > NUM_OF_ROWS)
            return null;
        int col = x;
        int row = NUM_OF_ROWS - y - 1;
        Cell cell = (Cell) getChildAt(row * NUM_OF_COLS + col);
        return cell;
    }

    public Cell getCell(Coordinate coordinate) {
        int x = coordinate.getX();
        int y = coordinate.getY();
        return getCell(x, y);
    }

    public int[] getSimulatedSensorDist() {
        Orientation orientation = mRobot.getOrientation();
        Orientation leftOrientation = mRobot.calculateOrientaion(Direction.LEFT);
        Orientation rightOrientation = mRobot.calculateOrientaion(Direction.RIGHT);

        int[] sensorDist = new int[6];
        sensorDist[0] = getSimulatedSensorDist(leftOrientation, 0, 4);
        sensorDist[1] = getSimulatedSensorDist(leftOrientation, 2, 4);
        sensorDist[2] = getSimulatedSensorDist(orientation, 0, 4);
        sensorDist[3] = getSimulatedSensorDist(orientation, 1, 4);
        sensorDist[4] = getSimulatedSensorDist(orientation, 2, 4);
        sensorDist[5] = getSimulatedSensorDist(rightOrientation, 2, 7);
        Log.d(LOG_TAG, "getSimulatedSensorDist: sensor dist 0 " + sensorDist[0]);
        Log.d(LOG_TAG, "getSimulatedSensorDist: sensor dist 1 " + sensorDist[1]);
        Log.d(LOG_TAG, "getSimulatedSensorDist: sensor dist 2 " + sensorDist[2]);
        Log.d(LOG_TAG, "getSimulatedSensorDist: sensor dist 3 " + sensorDist[3]);
        Log.d(LOG_TAG, "getSimulatedSensorDist: sensor dist 4 " + sensorDist[4]);
        Log.d(LOG_TAG, "getSimulatedSensorDist: sensor dist 5 " + sensorDist[5]);

        return sensorDist;
    }

    public int getSimulatedSensorDist(Orientation orientation, int position, int range) { //number is 0-2
        if (position < 0 || position > 2) {
            Log.d(LOG_TAG, "getSimulatedSensorDist: " + "number of sensor must be 0-2");
            throw new InvalidParameterException("Number of sensor must be 0-2");
        }

        int dist = 0;
        Coordinate curCoordinate;

        curCoordinate = mSensorMgr.getSensorCoordinate(orientation, position);

        while (curCoordinate.getNeighbour(orientation) != null) {
            curCoordinate = curCoordinate.getNeighbour(orientation);
            if (getCell(curCoordinate) != null) {
                if (getCell(curCoordinate).isBlocked() == false) {
                    dist++;
                    if (dist == range)
                        break;
                } else
                    break;
            }

        }
        Log.d(LOG_TAG, "getSimulatedSensorDist: orientation: " + orientation + " number: " + position + " dist: " + dist);

        return dist;


    }


    public void updateMaze() {
        for (int i = 0; i < NUM_OF_SENSORS; i++) {
            Sensor sensor = mSensorMgr.getSensor(i);
            int sensorDist = sensor.getDist();
            Orientation sensorOrientation = sensor.getOrientation();
            Coordinate sensorCoordinate = sensor.getCoordinate();

            Coordinate sawCellCoordinate = sensorCoordinate;
            for (int j = 0; j < sensorDist; j++) {
                sawCellCoordinate = sawCellCoordinate.getNeighbour(sensorOrientation);
                mArena.getCell(sawCellCoordinate).explore();
            }

            if (sensorDist < sensor.getRange()) {
                Coordinate blockCoordinate = sawCellCoordinate.getNeighbour(sensorOrientation);

                if (blockCoordinate != null) {
                    Log.d(LOG_TAG, "updateMaze: block cell: " + blockCoordinate.toString());
                    mArena.getCell(blockCoordinate).explore();
                    mArena.getCell(blockCoordinate).block();

                }
            }


        }
    }

    public void setFastestPath(int x, int y) {
        if (Coordinate.valid(x, y)) {
            Log.d(LOG_TAG, "setFastestPath: VALILD COORDINATE");
            getCell(x, y).setFastPath();
        }

    }
}
