package com.example.app;

/**
 * Created by Greg on 11/07/2014.
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.UUID;
import java.util.TimeZone;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.FileWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import static com.example.app.FileOpps.*;


public class ServerSend extends AsyncTask<String, Void, String > {

    private Context context;

    public ServerSend(Context context){
        this.context = context;
    }



    public void onPreExecute(){

        ConnectivityManager check = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(check != null){
            NetworkInfo[] info = check.getAllNetworkInfo();
            if(info != null){
                for(int i = 0; i < info.length; i++)
                    if(info[i].getState() == NetworkInfo.State.CONNECTED){
                        Toast.makeText(context, "Internet is Connected", Toast.LENGTH_LONG).show();
                    }
            } else {
                Toast.makeText(context, "Internet is Not Connected", Toast.LENGTH_LONG).show();
            }
        }

        zip my_Zip = new zip();

        if(my_Zip.zipFolder())
        {
            Toast.makeText(context, "File Zipped", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "ERROR: Zip Failed", Toast.LENGTH_LONG).show();

        }

    }

    @Override
    protected String doInBackground(String... arg0) {

        String Address = getPostAddress(arg0[0]);



        return Address;

        /*String Response = "";
        try{

            URL object = new URL(arg0[0]);
            HttpURLConnection conn = (HttpURLConnection) object.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Docking Station");
            conn.connect();
            int responseCode = conn.getResponseCode();

            //ResponseCode = Integer.toString(responseCode);

            if(responseCode == 200){
                //Successful connection
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String getInput;
                StringBuffer getResponse = new StringBuffer();

                while ((getInput = in.readLine()) != null) {
                    getResponse.append(getInput);
                }
                in.close();

                PostAddress = getResponse.toString();
            }
            else if ( responseCode == 404){
                //Resource Not Found Error
                return new String( "Resource Not Found");
            }
            else if ( responseCode == 500){
                //Server Error
                return new String ("Internal Server Error");
            }
            else {
                //Unknown Failure
                return new String ("Error Unknown");
            }


        } catch (Exception e) {
            return new String( " Exceptions: " + e.getMessage());

        }

        return PostAddress;*/


    }

    public String getPostAddress(String getAdd){


        String PostAddress;
        String Response = "";
        try{

            URL object = new URL(getAdd);
            HttpURLConnection conn = (HttpURLConnection) object.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Docking Station");
            conn.connect();
            int responseCode = conn.getResponseCode();
            Response = checkResponse(responseCode);

            //ResponseCode = Integer.toString(responseCode);
            if(Response == null) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String getInput;
                StringBuffer getResponse = new StringBuffer();

                while ((getInput = in.readLine()) != null) {
                    getResponse.append(getInput);
                }
                in.close();

                PostAddress = getResponse.toString();
            }else {
                return Response;

            }


        } catch (Exception e) {
            return new String( " Exceptions: " + e.getMessage());

        }

        return PostAddress;
    }

    /*public boolean PostZip(String Address){

        String boundary = "*****";
        String lineEnd = "\r\n";

        try {


            URL obj = new URL(Address);
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("User-Agent", "Docking Station");
            conn.setRequestProperty("Accept-Charset", "UTF-8");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);



        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }*/

    public String checkResponse( int responseCode ){
        if(responseCode == 200){
            //Successful connection
            return null;
        }
        else if ( responseCode == 404){
            //Resource Not Found Error
            return new String( "Resource Not Found");
        }
        else if ( responseCode == 500){
            //Server Error
            return new String ("Internal Server Error");
        }
        else {
            //Unknown Failure
            return new String ("Error Unknown");
        }
    }
}
