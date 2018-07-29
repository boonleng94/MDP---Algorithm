package com.example.shiganyu.robotmdp;

import java.security.InvalidParameterException;
import java.util.ArrayList;

/**
 * Created by shiganyu on 6/2/18.
 */

public class Coordinate {

    private static final int NUM_OF_ROWS = 20;
    private static final int NUM_OF_COLS = 15;

    int mX;
    int mY;


    public Coordinate(int x, int y) {
        this.mX = x;
        this.mY = y;
    }

    public int getX() {
        return mX;
    }

    public int getY() {
        return mY;
    }

    public Coordinate getNeighbour(Orientation orientation) {
        Coordinate neighbour;
        switch (orientation) {
            case NORTH:
                neighbour = new Coordinate(mX, mY + 1);
                break;
            case SOUTH:
                neighbour = new Coordinate(mX, mY - 1);
                break;
            case EAST:
                neighbour = new Coordinate(mX + 1, mY);
                break;
            case WEST:
                neighbour = new Coordinate(mX - 1, mY);
                break;
            default:
                throw new InvalidParameterException("Invalid orientation");

        }
        if (neighbour.getX()>= NUM_OF_COLS ||neighbour.getX() < 0 || neighbour.getY() >= NUM_OF_ROWS || neighbour.getY()<0 )
            return null;
        return neighbour;
    }
    public ArrayList<Coordinate> getNeighbours (Orientation orientation, int number){
        ArrayList<Coordinate> neighbours = new ArrayList<Coordinate>();
        Coordinate curCoordinate = this;
        for (int i = 0 ;i < number; i++){
            Coordinate neighbour = curCoordinate.getNeighbour(orientation);
            if(neighbour != null)
                neighbours.add(neighbour);
            else
                break;
            curCoordinate = neighbour;
        }
        return neighbours;
    }

    public static boolean valid(int x, int y ){
        if (x >= NUM_OF_COLS || x < 0 || y > NUM_OF_ROWS || y <0)
            return false;
        else return true;

    }

    @Override
    public String toString() {
        return "Coordinate x: " + getX() + " y: " + getY();
    }
}
