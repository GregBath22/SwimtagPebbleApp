package com.example.app;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.UUID;
import java.util.TimeZone;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.FileWriter;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.PebbleKit.PebbleDataReceiver;
import com.getpebble.android.kit.PebbleKit.PebbleDataLogReceiver;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.google.common.primitives.UnsignedInteger;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;


public class Main extends ActionBarActivity {

    //Constants
    public static final String TAG = Main.class.getName();
    private static final int NUM_SAMPLES = 1;
    private static final int MEM_SIZE = 10000;

    //State
    private int[] latest_data;
    int tempX = 0;
    int progressStatus =0;
    private int totalData = 0;

    int memTransfer = 0;
    String[][] Data = new String[3][MEM_SIZE];

    TextView xView;
    TextView yView;
    TextView zView;
    TextView timeView;
    TextView dataView;
    Button startButton;
    Button stopButton;
    Button storedData;
    Button writeToFile;
    Button testServer;
    ProgressBar mProgress;
    PendingIntent mPermissionIntent;
    private BroadcastReceiver mUsbReceiver;
    IntentFilter filter = null;

    private final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    //private PebbleDataLogReceiver mLogReceiver = null;
    private PebbleDataReceiver receiver = null;
    private UUID uuid = UUID.fromString("31335cd9-9950-4f41-8672-e9bf483f24d1");
    private Handler handler = new Handler();

    /*public Main(BroadcastReceiver mUsbReceiver) {
        this.mUsbReceiver = mUsbReceiver;
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        filter = new IntentFilter(ACTION_USB_PERMISSION);

        new Thread(new Runnable() {
            public void run() {
                while(progressStatus < 100){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            mProgress.setProgress(progressStatus);
                           //windowProgress.setProgress(progressStatus);
                        }
                    });
                }
            }
        }).start();

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            xView = (TextView)rootView.findViewById(R.id.x_view);
            yView = (TextView)rootView.findViewById(R.id.y_view);
            zView = (TextView)rootView.findViewById(R.id.z_view);
            timeView = (TextView)rootView.findViewById(R.id.time_view);
            dataView = (TextView)rootView.findViewById(R.id.data_view);

            startButton = (Button)rootView.findViewById(R.id.start_button);
            stopButton = (Button)rootView.findViewById(R.id.stop_button);
            storedData = (Button)rootView.findViewById(R.id.transfer_button);
            writeToFile =  (Button)rootView.findViewById(R.id.write_button);
            testServer = (Button)rootView.findViewById(R.id.server_test);

            mProgress = (ProgressBar) rootView.findViewById(R.id.progressBar2);
            PebbleKit.startAppOnPebble(getApplication(), uuid);


            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    memTransfer = 0;
                    //buttonClick(0);
                    //receiveLogData();
                    //UsbStart();
                }
            });
            stopButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view){
                    memTransfer = 0;
                    buttonClick(1);
                }
            });
            storedData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view){
                    memTransfer = 1;
                    tempX = 0;
                    buttonClick(2);
                }
            });
            writeToFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view){
                   // WriteToFile();
                    new FileOpps(Main.this, Data, MEM_SIZE);
                }
            });
            testServer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view){
                    String url = "http://processing.swimtag.net/api/rest/status";
                    //sendToServer();
                    String Out = null;
                    try {
                        Out = new ServerSend(Main.this).execute(url).get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(Main.this, Out, Toast.LENGTH_LONG).show();
                }
            });

            return rootView;

        }
    }

    public void buttonClick(int key){
        PebbleDictionary dict = new PebbleDictionary();
        dict.addInt32(key, 0);
        PebbleKit.sendDataToPebble(getApplication(), uuid, dict);
    }

  /*  public void UsbStart(){

        UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        UsbDevice device = deviceList.get("deviceName");
        String temp = device.toString();
        Toast.makeText(this, temp, Toast.LENGTH_LONG).show();
    }*/



    public void onResume() {
        super.onResume();

       mUsbReceiver = new BroadcastReceiver() {


               //Toast.makeText(Main.this, "here", Toast.LENGTH_LONG).show();



            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();
                Toast.makeText(Main.this, action, Toast.LENGTH_LONG).show();
                if (ACTION_USB_PERMISSION.equals(action)) {
                    synchronized (this) {
                        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            if(device != null){
                                //call method to set up device communication
                                Toast.makeText(Main.this, "Usb device", Toast.LENGTH_LONG).show();
                            }
                        }
                        else {
                            Log.d(TAG, "permission denied for device " + device);
                        }
                    }
                }
            }
        };


        registerReceiver(mUsbReceiver, filter);

       receiver = new PebbleDataReceiver(uuid) {
            @Override
            public void receiveData(Context context, int transactionId, PebbleDictionary data) {
                PebbleKit.sendAckToPebble(getApplicationContext(), transactionId);

       /* mLogReceiver = new PebbleDataLogReceiver(uuid) {


                public void receiveData(Context context, UUID logUuid, final int tag, byte[] data ) {*/
                //Count total data
                totalData += 4 * NUM_SAMPLES * 4;

                //Get data
                latest_data = new int[3 * NUM_SAMPLES];
				Log.d(TAG, "NEW DATA PACKET");


                for(int i = 0; i < NUM_SAMPLES; i++) {
                    for(int j = 0; j < 3; j++) {
                        try {
                                Data[j][tempX] = data.getInteger(j).toString();
                        } catch(Exception e) {
                            Data[j][tempX] = "X";
                        }
                    }
                }
                    tempX++;

                if(memTransfer == 1)
                {

                    for(int i=0; i<3; i++)
                    {
                        try {
                            Data[i][tempX] = (data.getInteger(i+4).toString());
                            } catch (Exception e) {
                            Data[i][tempX] = "X";
                        }
                    }

                    tempX++;
                    if(tempX == 1500 ){
                        Toast.makeText(context, "Check", Toast.LENGTH_LONG).show();
                    }
                    if(tempX == (MEM_SIZE -1))
                    {
                        Toast.makeText(context, "Data Transferred", Toast.LENGTH_LONG).show();
                        buttonClick(3);
                        dataView.setText("");
                        memTransfer = 0;
                        progressStatus = 0;
                    }
                }

                //Show
                if(memTransfer == 1)
                {
                    dataView.setText("Transfer in Progress: " + tempX + "/6000");
                    progressStatus = tempX/60;
                    //initiatePopUp();
                } else {
                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            xView.setText("X: " + latest_data[0] );//+ ": " + temp_data[0]);
                            yView.setText("Y: " + latest_data[1] );//+ ": " + temp_data[1]);
                            zView.setText("Z: " + latest_data[2] );//+ ": " + temp_data[2]);

                        }

                    });
                }
            }

        };

       PebbleKit.registerReceivedDataHandler(this, receiver);

        //PebbleKit.registerDataLogReceiver(this, mLogReceiver);

        //PebbleKit.requestDataLogsForApp(this, uuid);

    }

    @Override
    protected void onStop() {
        super.onStop();

        PebbleKit.closeAppOnPebble(getApplicationContext(), uuid);
        unregisterReceiver(receiver);
    }

    @Override
    protected void onPause() {
        super.onPause();

        //unregisterReceiver(mLogReceiver);
        unregisterReceiver(receiver);
    }

}

