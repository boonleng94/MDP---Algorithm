package com.example.shiganyu.robotmdp;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import static com.example.shiganyu.robotmdp.MainActivity.mArena;
import static com.example.shiganyu.robotmdp.MainActivity.mRobot;

public class FastestPathNavigator {
    private static final String LOG_TAG = FastestPathNavigator.class.getName();
    private Cell currentCell;                   // Current Cell
    private Cell[] neighbourCells;              // Array of neighbour Cells in 4 directions - F,L,R,B
    private ArrayList<Cell> cellsToVisit;       // Array of Cells to be visited
    private ArrayList<Cell> cellsVisited;       // Array of visited Cells
    private HashMap<Cell, Cell> parentCells;    // HashMap of Cell mapped to Parent Cell <Child, Parent>
    private Orientation currentOrientation;     // Current Orientation of mRobot
    private double[][] travelCost;              // Array of path traversal cost from START to GOAL for each cell (node)
    private static int MAX_COST = 99999;        // Cost of reaching an impossible cell (node)
    private static int MOVEMENT_COST = 10;      // Cost of a single Cell movement (in ms) TO BE DETERMINED WHEN TRYING
    private static int TURNING_COST = 30;       // Cost of a single Orientation turn (in ms) TO BE DETERMINED WHEN TRYING
    private int loopCount;                      // Loop count for finding optimal fastest path
    private boolean explorationMode;            // For exploration mode run to START
    private int waypointX;                      // X-coordinate of the waypoint
    private int waypointY;                      // Y-coordinate of the waypoint
    private boolean waypointGiven;
    StringBuilder outputString = new StringBuilder("AR|");

    /**
     * Constructor for virtual run-through simulation
     */
    public FastestPathNavigator() {
        initializeVariables(false);
    }

    /**
     * Constructor for virtual run-through simulation with waypoint
     */
    public FastestPathNavigator(int waypointX, int waypointY) {
        this.waypointX = waypointX;
        this.waypointY = waypointY;
        initializeVariables(true);
    }

    /**
     * Initialization of variables required
     */
    private void initializeVariables(boolean waypointGiven) {
        this.cellsToVisit = new ArrayList<>();
        this.cellsVisited = new ArrayList<>();
        this.parentCells = new HashMap<>();
        this.neighbourCells = new Cell[4];
        this.currentCell = mArena.getCell(mRobot.getXPos(), mRobot.getYPos());
        this.currentOrientation = mRobot.getOrientation();
        this.travelCost = new double[15][20];
        this.loopCount = 0;
        this.waypointGiven = waypointGiven;

        // Initialise travelCost array for each cell
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 20; j++) {
                Cell cell = mArena.getCell(i, j);

                //Boundary cells
                if (i == 0 || j == 0 || i == 14 || j == 19) {
                    travelCost[i][j] = MAX_COST;
                }

                if (!isNotObstacle(cell)) {
                    //Not supposed to traverse an obstacle cell (node) thus assigned infinite cost
                    travelCost[i][j] = MAX_COST;
                    //To setup virtual wall of an obstacle cell
                    setCellVirtualWall(i, j);
                }
                else {
                    if (!isImpassableCell(cell)) {
                        travelCost[i][j] = 0;
                    }
                }
            }
        }
        cellsToVisit.add(currentCell);

        // Initialise cost of START cell
        travelCost[mRobot.getXPos()][mRobot.getYPos()] = 0;
    }

    /**
     * Sets up the virtual wall of an obstacle Cell (border of Cell)
     */
    private void setCellVirtualWall(int col, int row) {
        if (row >= 1) {
            travelCost[col][row - 1] = MAX_COST;
            if (col < 14) {
                travelCost[col + 1][row - 1] = MAX_COST;
            }
            if (col >= 1) {
                travelCost[col - 1][row - 1] = MAX_COST;
            }
        }
        if (row < 19) {
            travelCost[col][row + 1] = MAX_COST;
            if (col < 14) {
                travelCost[col + 1][row + 1] = MAX_COST;
            }
            if (col >= 1) {
                travelCost[col - 1][row + 1] = MAX_COST;
            }
        }
        if (col >= 1) {
            travelCost[col - 1][row] = MAX_COST;
        }
        if (col < 14) {
            travelCost[col + 1][row] = MAX_COST;
        }
    }

    /**
     * Returns true if the given coordinates are within the Arena boundaries
     */
    private boolean checkValidCoordinates(int row, int col) {
        return row >= 0 && col >= 0 && row < 20 && col < 15;
    }

    /**
     * Returns true if the cell is not obstacle (can be traversed)
     */
    private boolean isNotObstacle(Cell cell) {
        return cell.hasExplored() && !cell.isBlocked();
    }

    /**
     * Returns true if the Cell is an obstacle or virtual wall
     */
    private boolean isImpassableCell(Cell cell) {
        return (travelCost[cell.getCol()][cell.getRow()] == MAX_COST);
    }

    /**
     * Returns true if it is possible for the mRobot to move Forward (the Cell ahead is a non-obstacle Cell within Arena boundaries)
     * This method is used for running home, to see if it is possible to advance forward, used for debugging later
     */
    private boolean moveForward() {
        int col = mRobot.getXPos();
        int row = mRobot.getYPos();

        switch (mRobot.getOrientation()) {
            case NORTH:
                if (!mArena.getCell(col - 1, row + 2).isBlocked() && !mArena.getCell(col, row +2).isBlocked() && !mArena.getCell(col + 1, row + 2).isBlocked()) {
                    return true;
                }
                break;
            case EAST:
                if (!mArena.getCell(col + 2, row + 1).isBlocked() && !mArena.getCell(col + 2, row).isBlocked() && !mArena.getCell(col + 2, row - 1).isBlocked()) {
                    return true;
                }
                break;
            case SOUTH:
                if (!mArena.getCell(row - 2, col - 1).isBlocked() && !mArena.getCell(row - 2, col).isBlocked() && !mArena.getCell(row - 2, col + 1).isBlocked()) {
                    return true;
                }
                break;
            case WEST:
                if (!mArena.getCell( col - 2, row + 1).isBlocked() && !mArena.getCell(col - 2, row).isBlocked() && !mArena.getCell(col - 2, row - 1).isBlocked()) {
                    return true;
                }
                break;
        }
        return false;
    }

    /**
     * Returns the first optimal cell that can be traversed to reach the Goal (lowest cost - to ensure shortest path)
     */
    private Cell optimalCell(int goalCol, int goalRow) {
        int size = cellsToVisit.size();
        double minCost = MAX_COST;
        Cell result = null;

        // gCost is the cost of the path from a Cell to GOAL
        for (int i = size - 1; i >= 0; i--) {
            double gCost = travelCost[(cellsToVisit.get(i).getCol())][(cellsToVisit.get(i).getRow())];
            double cost = gCost + heuristicCost(cellsToVisit.get(i), goalCol, goalRow);
            if (cost < minCost) {
                minCost = cost;
                result = cellsToVisit.get(i);
            }
        }
        return result;
    }

    /**
     * Returns the Orientation that the mRobot need to turn to move from current Cell to the target Cell
     */
    private Orientation getNextOrientation(int robotCurrentCol, int robotCurrentRow, Orientation botCurrentOrientation, Cell targetCell) {
        if (robotCurrentCol - targetCell.getCol() > 0) {
            return Orientation.WEST;
        } else if (targetCell.getCol() - robotCurrentCol > 0) {
            return Orientation.EAST;
        } else {
            if (robotCurrentRow - targetCell.getRow() > 0) {
                return Orientation.SOUTH;
            } else if (targetCell.getRow() - robotCurrentRow > 0) {
                return Orientation.NORTH;
            } else {
                return botCurrentOrientation;
            }
        }
    }

    /**
     * Returns the total turning cost required for the mRobot to change Orientations
     */
    private double getTurnCost(Orientation originalOrientation, Orientation nextOrientation) {
        int numOfTurn = Math.abs(originalOrientation.ordinal() - nextOrientation.ordinal());
        if (numOfTurn > 2) {
            numOfTurn = numOfTurn % 2;
        }
        return (numOfTurn * TURNING_COST);
    }

    /**
     * Returns the moving/travel cost required for the mRobot to move from origin Cell to destination Cell(assuming both cells are neighbours of each other).
     */
    private double getMoveCost(Cell originCell, Cell destinationCell, Orientation orientation) {
        double moveCost = MOVEMENT_COST; // one movement to neighbor

        double turnCost;
        Orientation targetDir = getNextOrientation(originCell.getCol(), originCell.getRow(), orientation, destinationCell);
        turnCost = getTurnCost(orientation, targetDir);

        return moveCost + turnCost;
    }

    /**
     * Returns the heuristic cost needed to traverse from a given Cell to the Goal
     * Heuristics cost - estimates the cost of the cheapest path from Cell to the GOAL. (time taken)
     * Wiki - For the algorithm to find the actual shortest path, the heuristic function must be admissible,
     * meaning that it never overestimates the actual cost to get to the nearest goal node.
     */
    private double heuristicCost(Cell cell, int goalCol, int goalRow) {
        // The actual number of moves required (difference in the rows and columns)
        double movementCost = (Math.abs(goalCol - cell.getCol()) + Math.abs(goalRow - cell.getRow())) * MOVEMENT_COST;

        if (movementCost == 0) {
            return 0;
        }

        // If goal is not in the same row or column, one orientation turn will be required
        double turnCost = 0;
        if (goalCol - cell.getCol() != 0 || goalRow - cell.getRow() != 0) {
            turnCost = TURNING_COST;
        }

        return movementCost + turnCost;
    }

    /**
     * Returns the Direction the mRobot needs to turn to face the next Orientation from the original Orientation
     */
    private Direction getDirectionToTurn(Orientation originalOrientation, Orientation nextOrientation) {
        switch (originalOrientation) {
            case NORTH:
                switch (nextOrientation) {
                    case SOUTH:
                        return Direction.BACK;
                    case WEST:
                        return Direction.LEFT;
                    case EAST:
                        return Direction.RIGHT;
                }
                break;
            case SOUTH:
                switch (nextOrientation) {
                    case NORTH:
                        return Direction.BACK;
                    case WEST:
                        return Direction.RIGHT;
                    case EAST:
                        return Direction.LEFT;
                }
                break;
            case EAST:
                switch (nextOrientation) {
                    case NORTH:
                        return Direction.LEFT;
                    case SOUTH:
                        return Direction.RIGHT;
                    case WEST:
                        return Direction.BACK;

                }
                break;
            case WEST:
                switch (nextOrientation) {
                    case NORTH:
                        return Direction.RIGHT;
                    case SOUTH:
                        return Direction.LEFT;
                    case EAST:
                        return Direction.BACK;
                }
                break;
        }
        return null;
    }

    /**
     * Returns the String (sequence of Movements) that the mRobot should take to reach the Goal the fastest
     * This method is for main program to call
     */
    public String executeFastestPath(int goalCol, int goalRow) {
        if (waypointGiven) {
            waypointGiven = false;
            executeFastestPath(waypointX, waypointY);
            initializeVariables(false);
        }

        Log.i(LOG_TAG, "Fastest path from (" + currentCell.getCol() + ", " + currentCell.getRow() + ") to (" + goalCol + ", " + goalRow + ")...");
        // Stack to contain the shortest path/mandatory cells the mRobot should take (for backtracking later)
        Stack<Cell> shortestPathStack = new Stack<>();

        do {
            loopCount++;

            // Get the Cell with the smallest cost from the array of Cells to visit and assign it as the current Cell
            currentCell = optimalCell(goalCol, goalRow);
            //Log.i(LOG_TAG, "Current cell = (" + currentCell.getCol() + ", " + currentCell.getRow() + ")");

            // Change the Orientation of the mRobot to face the current Cell from the previous Cell
            if (parentCells.containsKey(currentCell)) {
                currentOrientation = getNextOrientation(parentCells.get(currentCell).getCol(), parentCells.get(currentCell).getRow(), currentOrientation, currentCell);
            }

            // Add current Cell to array of visited Cells and remove from array of Cells to visit
            cellsVisited.add(currentCell);
            cellsToVisit.remove(currentCell);

            // If the Goal cell is in the array of visited Cells - a path to the Goal cell has been found, find the fastest path
            if (cellsVisited.contains(mArena.getCell(goalCol, goalRow))) {
                Cell temp = mArena.getCell(goalCol, goalRow);
                while (true) {
                    shortestPathStack.push(temp);

                    temp = parentCells.get(temp);
                    if (temp == null) {
                        break;
                    }
                }
                return calcFastestPath(shortestPathStack, goalCol, goalRow);
            }

            //Log.i(LOG_TAG, "Current cell: " + currentCell.getCol() + " " + currentCell.getRow());

            // Store the neighbour cells of the current Cell into the array (Up, down, left, right) - null if the respective neighbour cell is an obstacle
            // Up neighbour cell
            if (checkValidCoordinates(currentCell.getRow() + 1, currentCell.getCol())) {
                neighbourCells[0] = mArena.getCell(currentCell.getCol(), currentCell.getRow() + 1);
                if (isImpassableCell(neighbourCells[0])) {
                    neighbourCells[0] = null;
                }
            }
            // Down neighbour cell
            if (checkValidCoordinates(currentCell.getRow() - 1, currentCell.getCol())) {
                neighbourCells[1] = mArena.getCell(currentCell.getCol(), currentCell.getRow() - 1);
                if (isImpassableCell(neighbourCells[1])) {
                    neighbourCells[1] = null;
                }
            }
            // Left neighbour cell
            if (checkValidCoordinates(currentCell.getRow(), currentCell.getCol() - 1)) {
                neighbourCells[2] = mArena.getCell(currentCell.getCol() - 1, currentCell.getRow());
                if (isImpassableCell(neighbourCells[2])) {
                    neighbourCells[2] = null;
                }
            }
            // Right neighbour cell
            if (checkValidCoordinates(currentCell.getRow(), currentCell.getCol() + 1)) {
                neighbourCells[3] = mArena.getCell(currentCell.getCol() + 1, currentCell.getRow());
                if (isImpassableCell(neighbourCells[3])) {
                    neighbourCells[3] = null;
                }
            }

            // For-loop to iterate through the neighbour Cells and update the travel cost of each Cell
            for (int i = 0; i < neighbourCells.length; i++) {
                if (neighbourCells[i] != null) {
                    // The neighbour cell has already been visited previously
                    if (cellsVisited.contains(neighbourCells[i])) {
                        continue;
                    }

                    if (!(cellsToVisit.contains(neighbourCells[i]))) {
                        parentCells.put(neighbourCells[i], currentCell);
                        travelCost[neighbourCells[i].getCol()][neighbourCells[i].getRow()] = travelCost[currentCell.getCol()][currentCell.getRow()] + getMoveCost(currentCell, neighbourCells[i], currentOrientation);
                        cellsToVisit.add(neighbourCells[i]);
                    } else {
                        double currentCost = travelCost[neighbourCells[i].getCol()][neighbourCells[i].getRow()];
                        double updatedCost = travelCost[currentCell.getCol()][currentCell.getRow()] + getMoveCost(currentCell, neighbourCells[i], currentOrientation);
                        if (updatedCost < currentCost) {
                            travelCost[neighbourCells[i].getCol()][neighbourCells[i].getRow()] = updatedCost;
                            parentCells.put(neighbourCells[i], currentCell);
                        }
                    }
                }
            }
        } while (!cellsToVisit.isEmpty());

        return null;
    }

    /**
     * Returns the String (sequence of Movements) that the mRobot should take to reach the Goal the fastest as well as execute it
     */
    @NonNull
    private String calcFastestPath(Stack<Cell> shortestPath, int goalCol, int goalRow) {
        Orientation nextOrientation;
        Direction m;
        ArrayList<Direction> movementsToTake = new ArrayList<>();
        Cell temp = shortestPath.pop();
        int forwardCounter = 0;

        // While mRobot is not at Goal yet
        while ((mRobot.getXPos() != goalCol) || (mRobot.getYPos() != goalRow)) {
            // If mRobot is already at the current Cell from the fastest path, get next Cell to move to
            if (mRobot.getXPos() == temp.getCol() && mRobot.getYPos() == temp.getRow()) {
                temp = shortestPath.pop();
            }

            // Find out which Orientation the mRobot has to turn to to move to the temp Cell
            nextOrientation = getNextOrientation(mRobot.getXPos(), mRobot.getYPos(), mRobot.getOrientation(), temp);

            // Turn the mRobot's direction to the correct Orientation
            if (mRobot.getOrientation() != nextOrientation) {
                m = getDirectionToTurn(mRobot.getOrientation(), nextOrientation);
            } else {
                m = Direction.FORWARD;
            }

            Log.i(LOG_TAG, "mRobot move " + Direction.toString(m) + " from (" + mRobot.getXPos() + ", " + mRobot.getYPos() + ") to (" + temp.getCol() + ", " + temp.getRow() + ")");

            // Command the mRobot to move a step in that Direction
            //Log.i(LOG_TAG, "Robot Current Orientation: " + mRobot.getOrientation());
            mRobot.move(m, 1);

            movementsToTake.add(m);

            //String to send to RPI
            if (m == Direction.FORWARD) {
                forwardCounter++;
            } else if (m == Direction.RIGHT) {
                if (forwardCounter != 0) {
                    outputString.append("f" + forwardCounter + ",");
                    forwardCounter = 0;
                }
                outputString.append("r0,");
            } else if (m == Direction.LEFT) {
                if (forwardCounter != 0) {
                    outputString.append("f" + forwardCounter + ",");
                    forwardCounter = 0;
                }
                outputString.append("l0,");
            } else if (m == Direction.BACK) {
                if (forwardCounter != 0) {
                    outputString.append("f" + forwardCounter + ",");
                    forwardCounter = 0;
                }
                outputString.append("r0,r0,");
            }

            //outputString.append(Direction.toString(m)+"|");

            //If rotate, add a step forward after rotation
            if (m == Direction.RIGHT ||m == Direction.LEFT || m == Direction.BACK) {
                movementsToTake.add(Direction.FORWARD);
                forwardCounter++;
            }
        }

        if (forwardCounter != 0) {
            outputString.append("f" + forwardCounter + ",");
        }
        // For exploration mode run to START
        /*if (explorationMode) {
            for (Direction x : movementsToTake) {
                if (x == Direction.FORWARD) {
                    if (!moveForward()) {
                        return "Failed to pathfind!";
                    }
                }

                mRobot.move(x,1);

                // Sensor data stuff here
//                if (explorationMode) {
//
//                }
            }
        } else {
            int forwardSteps = 0;

            // Commands the mRobot to move
            for (Direction x : movementsToTake) {
                if (x == Direction.FORWARD) {
                    forwardSteps++;
                    // 10 consecutive Forwards
                    if (forwardSteps == 10) {
                        mRobot.move(Direction.FORWARD, forwardSteps);
                        forwardSteps = 0;
                    }
                } else if (x == Direction.LEFT || x == Direction.RIGHT) {
                    if (forwardSteps > 0) {
                        mRobot.move(Direction.FORWARD, forwardSteps);
                        forwardSteps = 0;
                    }
                    mRobot.move(x, 1);
                }
            }

            if (forwardSteps > 0) {
                mRobot.move(Direction.FORWARD, forwardSteps);
            }
        }*/
        return outputString.toString();
    }

    public String executeFastestPathToUnexplored() {
        String fp = "";
        Stack<Cell> unexploredCells = new Stack<Cell>();
        //start point as final cell to go to
        unexploredCells.add(mArena.getCell(1, 1));

        for (int row = 0; row < 20; row++) {
            for (int col = 0; col < 15; col++) {
                Cell cell = mArena.getCell(col, row);
                if (!cell.hasExplored()) {
                    unexploredCells.add(cell);
                }
            }
        }

        while (!unexploredCells.isEmpty()) {
            Cell toGo = unexploredCells.pop();
            // see which side can explore the Cell then go explore
            if (checkGotOpening(toGo).equals("L")) {
                fp += executeFastestPath(toGo.getCol()-1, toGo.getRow());
            } else if (checkGotOpening(toGo).equals("R")) {
                fp += executeFastestPath(toGo.getCol()+1, toGo.getRow());
            } else if (checkGotOpening(toGo).equals("T")) {
                fp += executeFastestPath(toGo.getCol(), toGo.getRow()+1);
            } else if (checkGotOpening(toGo).equals("D")) {
                fp += executeFastestPath(toGo.getCol(), toGo.getRow()-1);
            }
        }

        return fp;
    }

    //Check cell's surrounding 8 cells all empty
    public String checkGotOpening(Cell cell) {
        Cell[] neighbours = new Cell[8];              // Array of neighbour Cells in 4 directions - F,L,R,B
        String result = "";

        // Top neighbour cell
        if (checkValidCoordinates(cell.getRow() + 1, cell.getCol())) {
            neighbours[0] = mArena.getCell(cell.getCol(), cell.getRow() + 1);
            if (travelCost[neighbours[0].getCol()][neighbours[0].getRow()] == MAX_COST) {
                neighbours[0] = null;
            }
        }
        // Down neighbour cell
        if (checkValidCoordinates(cell.getRow() - 1, cell.getCol())) {
            neighbours[1] = mArena.getCell(cell.getCol(), cell.getRow() - 1);
            if (travelCost[neighbours[1].getCol()][neighbours[1].getRow()] == MAX_COST) {
                neighbours[1] = null;
            }
        }
        // Left neighbour cell
        if (checkValidCoordinates(cell.getRow(), cell.getCol() - 1)) {
            neighbours[2] = mArena.getCell(cell.getCol() - 1, cell.getRow());
            if (travelCost[neighbours[2].getCol()][neighbours[2].getRow()] == MAX_COST) {
                neighbours[2] = null;
            }
        }
        // Right neighbour cell
        if (checkValidCoordinates(cell.getRow(), cell.getCol() + 1)) {
            neighbours[3] = mArena.getCell(cell.getCol() + 1, cell.getRow());
            if (travelCost[neighbours[3].getCol()][neighbours[3].getRow()] == MAX_COST) {
                neighbours[3] = null;
            }
        }
        // Top left
        if (checkValidCoordinates(cell.getRow() + 1, cell.getCol()-1)) {
            neighbours[4] = mArena.getCell(cell.getCol()-1, cell.getRow() + 1);
            if (travelCost[neighbours[4].getCol()][neighbours[4].getRow()] == MAX_COST) {
                neighbours[4] = null;
            }
        }
        // Top right
        if (checkValidCoordinates(cell.getRow() + 1, cell.getCol()+1)) {
            neighbours[5] = mArena.getCell(cell.getCol()+1, cell.getRow() + 1);
            if (travelCost[neighbours[5].getCol()][neighbours[5].getRow()] == MAX_COST) {
                neighbours[5] = null;
            }
        }
        // Down left neighbour cell
        if (checkValidCoordinates(cell.getRow() - 1, cell.getCol()-1)) {
            neighbours[6] = mArena.getCell(cell.getCol()-1, cell.getRow() - 1);
            if (travelCost[neighbours[6].getCol()][neighbours[6].getRow()] == MAX_COST) {
                neighbours[6] = null;
            }
        }
        // Down right neighbour cell
        if (checkValidCoordinates(cell.getRow() - 1, cell.getCol()+1)) {
            neighbours[7] = mArena.getCell(cell.getCol()+1, cell.getRow() - 1);
            if (travelCost[neighbours[7].getCol()][neighbours[7].getRow()] == MAX_COST) {
                neighbours[7] = null;
            }
        }

        if (neighbours[4] != null && neighbours[2] != null && neighbours[7] != null ) {
            result = "L";
        }
        if (neighbours[4] != null && neighbours[0] != null && neighbours[5] != null ) {
            result = "T";
        }
        if (neighbours[5] != null && neighbours[3] != null && neighbours[7] != null ) {
            result = "R";
        }
        if (neighbours[6] != null && neighbours[1] != null && neighbours[7] != null ) {
            result = "D";
        }

        return result;
    }
}