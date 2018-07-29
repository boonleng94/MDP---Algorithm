package com.example.shiganyu.robotmdp;

import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.util.Log;
import java.util.Scanner;

import static com.example.shiganyu.robotmdp.Navigator.GOAL_X;
import static com.example.shiganyu.robotmdp.Navigator.GOAL_Y;

enum Mode {
    SIMULATION,
    FASTESTPATH,
    ACTUAL
}

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getName();
    public static Robot mRobot;
    public static Arena mArena;
    public static MapDescriptor mMapDescriptor;
    public static Navigator mNavigator;
    public static SensorMgr mSensorMgr;
    public static FastestPathNavigator mFastestPathNavigator;
    public static Mode mMode = Mode.SIMULATION;
    public final Handler handler = new Handler();
    public Runnable runnable;
    public int steps;
    public int explrPct;
    public int timeLimit = 99999;
    public static int cellCount = 0;
    public long startTime = 0;
    public Scanner sc;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorMgr = new SensorMgr();
        mMapDescriptor = new MapDescriptor(this);
        mNavigator = new Navigator();

        mRobot = findViewById(R.id.robot_view);

        mArena = findViewById(R.id.arena_view);


        LayoutInflater layoutInflater = getLayoutInflater();

        for (int row = 0; row < mArena.getNumOfRows(); row++) {
            for (int col = 0; col < mArena.getNumOfCols(); col++) {
                View cellView = layoutInflater.inflate(R.layout.cell_item, null, false);
                mArena.addView(cellView);

            }
        }
        mMapDescriptor.loadMap();


        final Button exploreBtn = findViewById(R.id.explore_button);
        exploreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                steps = Integer.parseInt(((EditText) findViewById(R.id.steps)).getText().toString());
                explrPct = Integer.parseInt(((EditText) findViewById(R.id.explrPercent)).getText().toString());
                String tempTime = ((EditText) findViewById(R.id.timeLimited)).getText().toString();

                sc = new Scanner(tempTime);
                sc.useDelimiter(":");
                int temp = sc.nextInt();
                int temp2 = sc.nextInt();
                sc.close();
                timeLimit = temp*60 + temp2;

                startTime = System.currentTimeMillis();

                runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (timeLimit != 99999) {
                            timerStart(timeLimit);
                        }
                        mSensorMgr.setSensorData(null);
                        mArena.updateMaze();
                        mNavigator.explore();
                        handler.postDelayed(this, 1000/steps);

                        if (checkUnexplored()) {
                            //run fastest path to unexplored(s) and back to start
                            FastestPathNavigator mfp1 = new FastestPathNavigator();
                            String result = mfp1.executeFastestPathToUnexplored();
                        }

                        if (cellCount > 300*explrPct/100) {
                            terminateRobot();
                        }
                    }
                };
                handler.post(runnable);
            }
        });

        Button fastesPathBtn = findViewById(R.id.fastest_path_button);
        fastesPathBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                terminateRobot();
                mMode=Mode.FASTESTPATH;
                mArena.initCell();
                //For waypoint
                int wpX = 8;
                int wpY = 11;

                if (wpX != 0 && wpY != 0) {
                    mFastestPathNavigator = new FastestPathNavigator(8, 11);
                } else {
                    mFastestPathNavigator = new FastestPathNavigator();
                }

                String result = mFastestPathNavigator.executeFastestPath(GOAL_X, GOAL_Y);
                Log.i(LOG_TAG,  "AR|FP");   //first msg send to AR for FP
                Log.i(LOG_TAG,  result);        //second msg send to AR for commands
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_main.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_connect_bluetooth:
                //TODO:
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_view_maps:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean checkUnexplored() {
        //If fully explored, cellCount = 300
        if (cellCount<300) {
            return true;
        }

        return false;
    }

    public void timerStart(int time) {
        // Time is in milliseconds: 30000 = 30s, interval of 1s, do something
        CountDownTimer cdt = new CountDownTimer(time*1000, 1000) {
            public void onTick(long millisUntilFinished) {
                //Log.i(LOG_TAG, "Seconds remaining: " + millisUntilFinished / 1000);
            }
            public void onFinish() {
                terminateRobot();
            }
        }.start();
    }

    public void terminateRobot() {
        handler.removeCallbacks(runnable);

        long endTime = System.currentTimeMillis();
        long difference = endTime - startTime;
        double elapsedSeconds = difference / 1000.0;
        //Log.i(LOG_TAG, "Time taken: " + elapsedSeconds + "s");
    }
}
