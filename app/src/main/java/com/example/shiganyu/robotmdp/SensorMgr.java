package com.example.shiganyu.robotmdp;

import android.util.Log;

import java.security.InvalidParameterException;

import static com.example.shiganyu.robotmdp.MainActivity.mArena;
import static com.example.shiganyu.robotmdp.MainActivity.mMode;
import static com.example.shiganyu.robotmdp.MainActivity.mRobot;

/**
 * Created by shiganyu on 6/2/18.
 */

public class SensorMgr {
    private static final String LOG_TAG = SensorMgr.class.getName();

    public static final int NUM_OF_SENSOR = 6;

    private static final int SAFE_DISTANCE = 0;

    Sensor[] mSensors;

    public SensorMgr() {
        mSensors = new Sensor[NUM_OF_SENSOR];
        mSensors[0] = new Sensor(Direction.LEFT, 0, 4);
        mSensors[1] = new Sensor(Direction.LEFT, 2, 4);
        mSensors[2] = new Sensor(Direction.FORWARD, 0, 4);
        mSensors[3] = new Sensor(Direction.FORWARD, 1, 4);
        mSensors[4] = new Sensor(Direction.FORWARD, 2, 4);
        mSensors[5] = new Sensor(Direction.RIGHT, 2, 4);

    }

    public int getLeftDist() {
        return Math.min(mSensors[0].getDist(), mSensors[1].getDist());
    }

    public int getFrontDist() {
        return Math.min(Math.min(mSensors[2].getDist(), mSensors[3].getDist()), mSensors[4].getDist());
    }

    public int getRightDist() {
        return mSensors[5].getDist();
    }

    public boolean noWall(Direction direction) {
        int distance;
        switch (direction) {
            case LEFT:
                distance = getLeftDist();
                break;
            case FORWARD:
                distance = getFrontDist();
                break;
            case RIGHT:
                distance = getRightDist();
                break;
            default:
                throw new InvalidParameterException("Sensor direction incorret");
        }


        return distance > SAFE_DISTANCE;
    }

    public void setSensorData(int[] sensorDist) {
        if (mMode == Mode.SIMULATION) {
            sensorDist = mArena.getSimulatedSensorDist();
            for (int i = 0; i < sensorDist.length; i++) {
                mSensors[i].setmDistance(sensorDist[i]);
            }
        } else if (mMode == Mode.ACTUAL) {
            for (int i = 0; i < sensorDist.length; i++) {
                mSensors[i].setmDistance(sensorDist[i]);
            }
        }
    }

    public Coordinate getSensorCoordinate(Orientation orientation, int number) {
        if (number < 0 || number > 2) {
            Log.d(LOG_TAG, "getSimulatedSensorDist: " + "number of sensor must be 0-2");
            throw new InvalidParameterException("Number of sensor must be 0-2");
        }

        Coordinate robotCenterCoordinate = mRobot.getCoordinate();
        Coordinate nwCorner = robotCenterCoordinate.getNeighbour(Orientation.NORTH).getNeighbour(Orientation.WEST);
        Coordinate neCorner = robotCenterCoordinate.getNeighbour(Orientation.NORTH).getNeighbour(Orientation.EAST);
        Coordinate swCorner = robotCenterCoordinate.getNeighbour(Orientation.SOUTH).getNeighbour(Orientation.WEST);
        Coordinate seCorner = robotCenterCoordinate.getNeighbour(Orientation.SOUTH).getNeighbour(Orientation.EAST);
        Coordinate north = robotCenterCoordinate.getNeighbour(Orientation.NORTH);
        Coordinate south = robotCenterCoordinate.getNeighbour(Orientation.SOUTH);
        Coordinate east = robotCenterCoordinate.getNeighbour(Orientation.EAST);
        Coordinate west = robotCenterCoordinate.getNeighbour(Orientation.WEST);

        Coordinate curCoordinate;

        switch (orientation) {
            case NORTH: {
                if (number == 0)
                    curCoordinate = nwCorner;
                else if (number == 1)
                    curCoordinate = north;
                else if (number == 2)
                    curCoordinate = neCorner;
                else
                    return null;
                break;
            }

            case SOUTH: {
                if (number == 0)
                    curCoordinate = seCorner;
                else if (number == 1)
                    curCoordinate = south;
                else if (number == 2)
                    curCoordinate = swCorner;
                else
                    return null;
                break;
            }
            case EAST: {
                if (number == 0)
                    curCoordinate = neCorner;
                else if (number == 1)
                    curCoordinate = east;
                else if (number == 2)
                    curCoordinate = seCorner;
                else
                    return null;
                break;
            }
            case WEST: {
                if (number == 0) {
                    curCoordinate = swCorner;
                } else if (number == 1)
                    curCoordinate = west;
                else if (number == 2)
                    curCoordinate = nwCorner;
                else
                    return null;
                break;
            }
            default: {
                Log.i(LOG_TAG, "Wrong orientation");
                throw new InvalidParameterException("Wrong orientation");
            }


        }
        return curCoordinate;
    }

    public Sensor getSensor(int i) {
        return mSensors[i];
    }
}
