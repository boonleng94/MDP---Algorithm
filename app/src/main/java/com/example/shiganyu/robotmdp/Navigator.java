package com.example.shiganyu.robotmdp;

import android.util.Log;

import static com.example.shiganyu.robotmdp.MainActivity.mArena;
import static com.example.shiganyu.robotmdp.MainActivity.mMapDescriptor;
import static com.example.shiganyu.robotmdp.MainActivity.mRobot;
import static com.example.shiganyu.robotmdp.MainActivity.mSensorMgr;

/**
 * Created by shiganyu on 6/2/18.
 */

enum Direction {
    FORWARD,
    RIGHT,
    BACK,
    LEFT;

    public static String toString(Direction m) {
        switch (m) {
            case FORWARD:
                return "Forward";
            case RIGHT:
                return "Right";
            case LEFT:
                return "Left";
            case BACK:
                return "Back";
            default:
                return "Error";
        }
    }
}


public class Navigator {
    private static final String LOG_TAG = Navigator.class.getName();
    private boolean mGoalReached = false;

    private static final int START_X = 1;
    private static final int START_Y = 1;
    public static final int GOAL_X = 13;
    public static final int GOAL_Y = 18;


    public void explore() {
        Direction direction;
        int successiveLeft = 0;
        if (mRobot.getXPos() != targetX() || mRobot.getYPos() != targetY()) {
            Log.i(LOG_TAG, "current x: " + mRobot.getXPos() + " y: " + mRobot.getYPos());
            Log.i(LOG_TAG, "facing: " + mRobot.getOrientation().name());
            int curX = mRobot.getXPos();
            int curY = mRobot.getYPos();

            if (mSensorMgr.noWall(Direction.LEFT) && mArena.notBlocked(Direction.LEFT)) {
                direction = Direction.LEFT;
                successiveLeft++;
                if (successiveLeft == 5) {
                    mRobot.move(Direction.RIGHT, 1);
                    mRobot.move(Direction.LEFT, 0);
                    successiveLeft = 0;
                }
            } else if (mSensorMgr.noWall(Direction.FORWARD) && mArena.notBlocked(Direction.FORWARD)) {
                direction = Direction.FORWARD;
                successiveLeft = 0;
            } else if (mSensorMgr.noWall(Direction.RIGHT) && mArena.notBlocked(Direction.RIGHT)) {
                direction = Direction.RIGHT;
                successiveLeft = 0;
            } else {
                mRobot.move(Direction.BACK, 0);
                successiveLeft = 0;
                return;
            }
            mRobot.move(direction, 1);
            Log.i(LOG_TAG, "move: " + direction);
            if (mRobot.getXPos() == GOAL_X && mRobot.getYPos() == GOAL_Y) {
                mGoalReached = true;
            }
            if(mRobot.getXPos() == START_X && mRobot.getYPos() == START_Y){
                mMapDescriptor.storeMap(mArena);
            }
        }

    }

    private int targetY() {
        if (mGoalReached)
            return START_Y;
        else
            return GOAL_Y;
    }

    private int targetX() {
        if (mGoalReached)
            return START_X;
        else
            return GOAL_X;
    }
}
