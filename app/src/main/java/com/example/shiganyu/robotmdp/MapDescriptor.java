package com.example.shiganyu.robotmdp;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.InvalidParameterException;

import static com.example.shiganyu.robotmdp.MainActivity.mArena;
import static com.example.shiganyu.robotmdp.MainActivity.mMode;

/**
 * Created by shiganyu on 30/1/18.
 */

public class MapDescriptor {
    private static final String LOG_TAG = MapDescriptor.class.getName();
    private static final int BLOCK_SIZE = 4;
    private final int START_INDEX = 2;
    private final int NUM_OF_ROWS = 20;
    private final int NUM_OF_COLS = 15;

    private final int END_INDEX = START_INDEX + NUM_OF_ROWS * NUM_OF_COLS;
    private Context mContext;

    public void loadMap() {
        //String exploreHexString = "ffc07f80ff01fe03fffffff3ffe7ffcfff9c7f38fe71fce3f87ff0ffe1ffc3ff87ff0e0e1c1f";
        //String blockedHexString = "00000100001c80000000001c0000080000060001c00000080000";

        String exploreHexString = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
        //String blockedHexString = "0030006000c00000800380000380000001000200878100040008000008011000020004000c0";
        //String blockedHexString = "000000000400000001c800000000000700000000800000001f8000070000000002000000000";
        //String blockedHexString = "00000000000003800000383800100000000400080010fe3004000000006000c010002000400";
        //String blockedHexString = "0000020008002000800000001f8000200040008438098010002000400880f00000000000080";
        //String blockedHexString = "01c00000000000000000fc4000800100000003c080810002000800208880f00000000000080";

        //String blockedHexString = "000000000000000000000000000000000000000000000000000000000000000000000000000";

        String blockedHexString = "000000000400000001c800000000000700000000800000001f8000070000000002000000000";
        //String blockedHexString = "000000000080010042038400000000000000010c000000000000021f8400080000000000040";
        //String blockedHexString = "0000000000200400080010000070000000000000007e00fc000000010000010000002000000";

        String exploredBinaryString = hexToBinary(exploreHexString);
        String blockedBinaryString = hexToBinary(blockedHexString);


        String exploreBinaryStringTrimmed = exploredBinaryString.replaceAll(" ", "").substring(START_INDEX, END_INDEX);
        String blockedBinaryStingTrimmed = blockedBinaryString.replaceAll(" ", "");
        //Log.i(LOG_TAG, "Trimmed String length: " + exploreBinaryStringTrimmed.length());
        int indexOfExploredString = 0;
        int indexOfBlockedString = 0;
        if (exploreBinaryStringTrimmed.length() != NUM_OF_ROWS * NUM_OF_COLS)
            throw new InvalidParameterException("Unable to parse map descriptor string");

        for (int row = 0; row < NUM_OF_ROWS; row++) {
            for (int col = 0; col < NUM_OF_COLS; col++) {
                Cell curCell = mArena.getCell(col, row);
                if (exploreBinaryStringTrimmed.charAt(indexOfExploredString) == '1') {
                    if (mMode != Mode.SIMULATION)
                        curCell.explore();
                    if (blockedBinaryStingTrimmed.charAt(indexOfBlockedString) == '1') {
                        Log.i(LOG_TAG, "block cell at row: " + row + " col: "+ col);
                        curCell.block();

                    } else if (blockedBinaryStingTrimmed.charAt(indexOfBlockedString) != '0')
                        throw new InvalidParameterException("Unable to parse map");
                    indexOfBlockedString++;


                }
                indexOfExploredString++;

            }
        }

    }

    public String[] storeMap(Arena arena) {
        if(mMode==Mode.SIMULATION){

        }
        StringBuilder exploredBinaryStringBuilder = new StringBuilder();
        StringBuilder blockedBinaryStringBuilder = new StringBuilder();

        for (int row = 0; row < NUM_OF_ROWS; row++) {
            for (int col = 0; col < NUM_OF_COLS; col++) {
                Cell cell = arena.getCell(col, row);
                if (cell.hasExplored()) {
                    exploredBinaryStringBuilder.append("1");
                    if (cell.isBlocked()) {
                        blockedBinaryStringBuilder.append("1");
                    } else {
                        blockedBinaryStringBuilder.append("0");
                    }
                } else {
                    exploredBinaryStringBuilder.append("0");
                }
            }
        }


        exploredBinaryStringBuilder.append("11"); // ending 11
        exploredBinaryStringBuilder.insert(0, "11"); //leading 11
        String exploredHexString = binaryToHex(exploredBinaryStringBuilder.toString());
        String blockedHexString = binaryToHex(blockedBinaryStringBuilder.toString());
        String[] mapDescripter = new String[2];
        mapDescripter[0] = exploredHexString;
        Log.i(LOG_TAG, "explored hex string: " + exploredHexString);

        mapDescripter[1] = blockedHexString;
        Log.i(LOG_TAG, "Blocked hex string: " + blockedHexString);

        return mapDescripter;

    }

    public MapDescriptor(Context context) {

        mContext = context;
    }

    private String binaryToHex(String binaryString) {

        StringBuilder hexStringBuilder = new StringBuilder();

        //Log.i(LOG_TAG, "binary string: " + binaryString);

        String rightPaddedBinary = rightPad(binaryString); //right padding to make length of binary string a multiple of 4;
        //Log.i(LOG_TAG, "right padded: " + rightPaddedBinary.toString());

        for (int i = 0; i < rightPaddedBinary.length(); i += BLOCK_SIZE){
            int startIndex = i;
            int endIndex = i + BLOCK_SIZE;
            if (endIndex > rightPaddedBinary.length() -1){
                endIndex = rightPaddedBinary.length();
            }
            String binaryBlock = rightPaddedBinary.substring(startIndex, endIndex);
            //Log.i(LOG_TAG, "sub string: " + binaryBlock);
            String hexBlock = binaryBlockToHexBlock(binaryBlock);
            //Log.i(LOG_TAG, "hex sub string: " + hexBlock);
            hexStringBuilder.append(hexBlock);
        }
        return hexStringBuilder.toString();
    }

    private String hexToBinary(String hexString) {
        StringBuilder binaryStringBuilder = new StringBuilder();
        //Log.i(LOG_TAG, "Hex string " + hexString);

        for (int i = 0; i< hexString.length(); i++){

            String hexBlock = hexString.substring(i, i+1);
            //Log.i(LOG_TAG, "Hex block: "+ hexBlock);
            String binaryBlock = hexBlockToBinaryBlock(hexBlock);
            //Log.i(LOG_TAG, "Binary block: " + binaryBlock);
            binaryStringBuilder.append(binaryBlock);
        }
        return binaryStringBuilder.toString();

    }

    private String binaryBlockToHexBlock(String binaryString) {
        int decimal = Integer.parseInt(binaryString, 2);
        String hexString = Integer.toHexString(decimal);
        return hexString;
    }

    private String hexBlockToBinaryBlock(String hexString) {
        int decimal = Integer.parseInt(hexString,16);
        String binaryString = Integer.toBinaryString(decimal);
        StringBuilder binaryStringBuilder = new StringBuilder(binaryString);
        if (binaryString.length() %4 != 0){
            int numOfPadZero = 4 - binaryString.length() %4;
            for (int i = 0; i< numOfPadZero; i++){
                binaryStringBuilder.insert(0, "0");
            }
        }
        return binaryStringBuilder.toString();
    }

    private String rightPad(String binaryString) {
        StringBuilder rightPaddedBinary = new StringBuilder(binaryString);
        if (binaryString.length() % 4 != 0) {
            int remainder = binaryString.length() % 4;
            for (int i = 0; i < 4 - remainder; i++) {
                rightPaddedBinary.append("0");
            }
        }
        return rightPaddedBinary.toString();
    }

    private String[] readFromFile(String fileName){
        String[] mapStrings = new String[2];
        File sdcard = Environment.getExternalStorageDirectory();

        File file = new File(sdcard,"file.txt");


        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            mapStrings[0] = br.readLine();
            mapStrings[1] = br.readLine();
            br.close();
        }
        catch (IOException e) {
            Log.i(LOG_TAG, "Unable to read file" + fileName);
        }

        return mapStrings;
    }

    public void writeToFile(String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("testing.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }



}
