package com.example.app;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by Greg on 14/07/2014.
 */
public class FileOpps extends ActionBarActivity {

    private Context context;
    String[][] Data;
    int MEM_SIZE;

    public FileOpps(Context context, String[][] dataArray, int memory) {

        this.context = context;
        Data = dataArray;
        MEM_SIZE = memory;

        WriteToFile();
    }

    public void WriteToFile() {
        /*
        generate file
        filewriter
        flush
        close
        */
        if (!isExternalStorageWritable()) {
            Toast.makeText(context, "External Storage Unavailable", Toast.LENGTH_LONG).show();
            return;
        }
        File directory = new File(Environment.getExternalStorageDirectory(), "SWIM APP DATA");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        try {
            File SwimData = new File(directory, "SwimData.csv");
            FileWriter writer = new FileWriter(SwimData);
            writer.append("X,Y,Z,\n");
            for (int j = 1; j < MEM_SIZE; j++) {
                for (int i = 0; i < 3; i++) {
                    writer.append(Data[i][j]);
                    writer.append(",");
                }
                writer.append("\n ");
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        Toast.makeText(context, "Data Saved to File", Toast.LENGTH_LONG).show();


    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

}

