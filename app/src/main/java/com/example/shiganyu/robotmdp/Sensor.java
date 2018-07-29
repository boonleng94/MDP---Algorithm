package com.example.shiganyu.robotmdp;

import java.util.ArrayList;

import static com.example.shiganyu.robotmdp.MainActivity.mRobot;

/**
 * Created by shiganyu on 7/2/18.
 */

public class Sensor {
    private static final String LOG_TAG = Sensor.class.getName();
    private int mRange;
    private Direction mDirection;
    private int mNumber;
    private Orientation mOrientation;
    private int mDistance;

    /*public Sensor(Coordinate coordinate, Orientation orientation, int distance) {
        this.mCoordinate = coordinate;
        this.mOrientation = orientation;
        this.mDistance = distance;
    }*/

    public Sensor(Direction direction, int number, int range) {
        this.mDirection = direction;
        this.mNumber = number;
        this.mRange = range;
    }

    public ArrayList<Coordinate> sense() {
        Coordinate sensorCoordinate = getCoordinate();
        return sensorCoordinate.getNeighbours(mOrientation, mDistance);
    }

    public int getDist() {
        return mDistance;
    }

    public void setmDistance(int distance) {
        this.mDistance = distance;
    }

    public Orientation getOrientation() {
        Orientation sensorOrientation = mRobot.calculateOrientaion(mDirection);
        return sensorOrientation;
    }

    public Coordinate getCoordinate() {
        Coordinate robotCoordinate = mRobot.getCoordinate();
        Orientation sensorOrientation = getOrientation();
        Coordinate sensorCoordinate;

        sensorCoordinate = robotCoordinate.getNeighbour(sensorOrientation);
        if (mNumber == 0) {
            Orientation pos0Orientation = sensorOrientation.previous();
            sensorCoordinate = sensorCoordinate.getNeighbour(pos0Orientation);
        } else if (mNumber == 2) {
            Orientation pos2Orientation = sensorOrientation.next();
            sensorCoordinate = sensorCoordinate.getNeighbour(pos2Orientation);
        }

        return sensorCoordinate;
    }

    public int getRange() {
        return mRange;
    }
}
