package com.example.salazar.bottle;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.UUID;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

/*
* This class is the page where user will be directed to to show their water consumption and
* other data
*
* */
public class WaterConsumptionDisplayer extends ActionBarActivity {
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String address;
    BluetoothAdapter mBluetoothAdapter;
    private final static int REQUEST_ENABLE_BT = 1;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    Handler bluetoothIn;
    final int handlerState = 0;                        //used to identify handler message
    private StringBuilder recDataString = new StringBuilder();
    private ConnectedThread mConnectedThread;
    TextView waterConsumedPercentageTextView;
    TextView minTemperatureTextView;
    TextView maxTemperatureTextView;
    TextView humidityTextView;
    TextView bottlePositionTextView;
    float totalWaterConsumed;
    float expectedValue=2200;
    float percentageWaterConsumed=0;
    float acclValue;
    float loadCellValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_consumption_displayer);
        Log.v("WC class", "inside onCreate");
        //Link the buttons and textViews to respective views
        waterConsumedPercentageTextView = (TextView) findViewById(R.id.waterConsumedPercentage);
        //bottlePositionTextView= (TextView) findViewById(R.id.bottlePositionTextView);

        minTemperatureTextView = (TextView) findViewById(R.id.minTemperature);
        maxTemperatureTextView = (TextView) findViewById(R.id.maxTemperature);
        humidityTextView = (TextView) findViewById(R.id.humidity);


        Log.v("WC class", String.valueOf((int) percentageWaterConsumed));
        waterConsumedPercentageTextView.setTextColor(Color.BLUE);

        waterConsumedPercentageTextView.setText(String.valueOf((int) percentageWaterConsumed) + "%");
        recDataString.delete(0, recDataString.length());                    //clear all string data


        enableBluetooth();


        //System.out.println("before send data!!!!");
               // mConnectedThread.write("1");    // Send "1" via Bluetooth
                //Toast.makeText(getBaseContext(), "Turn on LED", Toast.LENGTH_SHORT).show();


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_water_consumption_displayer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id==R.id.action_refresh){
            FetchWeatherTask weatherTask=new FetchWeatherTask();
            //weatherTask.execute();
            weatherTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void enableBluetooth()
    {
        Log.v("WC Class","in enableBluetooth()");
        //get adapter object
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }

        //enable bluetooth for mobile
        if (!mBluetoothAdapter.isEnabled()) {

            Log.v("WC Class", "Enabling Bluetooth");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        //BlueTooth ON!


    }
    @Override
    public void onResume() {



        super.onResume();
        enableBluetooth();
        Log.v("WC class","in onResume");
        // connection methods are best here in case program goes into the background etc

        //Get MAC address from DeviceListActivity
        Intent intent = getIntent();
        address = intent.getStringExtra(MainActivity.EXTRA_DEVICE_ADDRESS);

        // Set up a pointer to the remote device using its address.
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        //Attempt to create a bluetooth socket for comms
        try {
            btSocket = device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        } catch (IOException e1) {
            Toast.makeText(getBaseContext(), "ERROR - Could not create Bluetooth socket", Toast.LENGTH_SHORT).show();
        }

        // Establish the connection.
        try {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();        //If IO exception occurs attempt to close socket
            } catch (IOException e2) {
                Toast.makeText(getBaseContext(), "ERROR - Could not close Bluetooth socket", Toast.LENGTH_SHORT).show();
            }
        }

        // Create a data stream so we can talk to the device
        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "ERROR - Could not create bluetooth outstream", Toast.LENGTH_SHORT).show();
        }
        //When activity is resumed, attempt to send a piece of junk data ('x') so that it will fail if not connected
        // i.e don't wait for a user to press button to recognise connection failure
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("x");
    }

    @Override
    public void onPause() {
        super.onPause();
        //Pausing can be the end of an app if the device kills it or the user doesn't open it again
        //close all connections so resources are not wasted

        //Close BT socket to device
        try     {
            btSocket.close();
        } catch (IOException e2) {
            Toast.makeText(getBaseContext(), "ERROR - Failed to close Bluetooth socket", Toast.LENGTH_SHORT).show();
        }
    }
    //takes the UUID and creates a comms socket
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }
    private void sendData(String message) {
        byte[] msgBuffer = message.getBytes();

        try {
            //attempt to place data on the outstream to the BT device
            outStream.write(msgBuffer);
        } catch (IOException e) {
            //if the sending fails this is most likely because device is no longer there
            Toast.makeText(getBaseContext(), "ERROR - Device not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);            //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application

                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                //finish();

            }
        }
    }


    public void onStart(){
        super.onStart();

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {

                //Log.v("", "inside handler");
                if (msg.what == handlerState) {                                     //if message is what we want
                    String readMessage = (String) msg.obj;
                    System.out.println("Data Received = " + readMessage);                                                             // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);        //keep appending to string until ~
                    int endOfLineIndex = recDataString.indexOf("~");    // determine the end-of-line
                    String rcvmsg;
                    if (endOfLineIndex > 0) {                              // make sure there is data before ~
                        rcvmsg=recDataString.toString();
                        System.out.println("rcvmsg"+rcvmsg);
                        String[] lines = rcvmsg.split("\n");
                        String temp2;
                        for(int i=0;i< lines.length;i++){
                            if(lines[i].contains("x~")){
                                //waterConsumedPercentageTextView.setTextColor(Color.RED);
                                //bottlePositionTextView.setTextColor(Color.RED);
                                //bottlePositionTextView.setText("Tilted");

                            }
                            else {
                                String wchash[]=lines[i].split("#");
                                if(wchash.length==2){
                                    temp2=wchash[1];
                                }
                                else{
                                    temp2=wchash[0];
                                }
                                String wctilde[]=temp2.split("~");
                                String wc=wctilde[0];


                                //bottlePositionTextView.setTextColor(Color.BLUE);
                                //bottlePositionTextView.setText("Up");
                                /*String[] parts = lines[i].split(":");
                                if (parts.length == 2) {
                                    acclValue = Float.parseFloat(parts[0].substring(1,parts[0].length()));
                                    loadCellValue=Integer.parseInt(parts[1].substring(0, parts[1].length() - 2));
                                    System.out.println("accl value"+acclValue);
                                    System.out.println("load cell value"+loadCellValue);
                                    System.out.println("Accelerometer value = " + acclValue);
                                    System.out.println("load cell value = " + loadCellValue);
                                   */

                                    totalWaterConsumed=Float.parseFloat(wc);
                                    System.out.println("totalWaterConsumed = " + totalWaterConsumed);
                                    percentageWaterConsumed=((totalWaterConsumed/expectedValue))*100;
                                    System.out.println("percentageWaterConsumed" + percentageWaterConsumed);
                                    waterConsumedPercentageTextView.setTextColor(Color.BLUE);
                                    waterConsumedPercentageTextView.setText(String.valueOf((int)percentageWaterConsumed)+"%"
                                            + " ("+totalWaterConsumed+"/ 2200 )");

                            }

                        }



                        /*if (recDataString.charAt(0) == '~')    //if it starts with # we know it is what we are looking for
                        {
                            String value=dataInPrint;
                        }
                        */
                        //rcvmsg = "";

                    }
                    recDataString.setLength(0);
                }
            }
        };
    }



    public class FetchWeatherTask extends AsyncTask<Void,Void,String> {

        /* The date/time conversion code is going to be moved outside the asynctask later,
 * so for convenience we're breaking it out into its own method now.
 */
        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }



        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        private String getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";
            final String OWM_HUMIDITY="humidity";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);


            //Time dayTime = new Time();
            //dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            //int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            //dayTime = new Time();

            String resultStrs = new String("");
            for(int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;
                String humidity;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                //long dateTime;
                //dateTime = dayTime.setJulianDay(julianStartDay+i);
                //day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);
                humidity=dayForecast.getString(OWM_HUMIDITY);

                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs = highAndLow+"/"+humidity;
            }

            /*for (String s : resultStrs) {
                Log.v( "Forecast entry: ",s);
            }*/
            return resultStrs;

        }



        @Override
        protected String doInBackground(Void... params) {
            if (params.length == 0) {
                return null;

            }
            Log.v(" ","inside do in bg");
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http")
                        .authority("api.openweathermap.org")
                        .appendPath("data")
                        .appendPath("2.5")
                        .appendPath("forecast")
                        .appendPath("daily")
                        .appendQueryParameter("q", "110020")
                        .appendQueryParameter("mode", "json")
                        .appendQueryParameter("units", "metric")
                        .appendQueryParameter("cnt", "1")
                        .appendQueryParameter("appid", "61a3902274dcb1d137df87bcfe582a12");
                String myUrl = builder.build().toString();
                Log.v("BuildURI", myUrl);
                //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=110020&mode=json&units=metric&cnt=7&appid=61a3902274dcb1d137df87bcfe582a12");
                URL url = new URL(myUrl);
                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();

                System.out.println("forecastJsonStr" + forecastJsonStr);
            } catch (IOException e) {
                Log.e("FragmentForecast", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("FragmentForecast", "Error closing stream", e);
                    }
                }
            }

            String forecastStringArray = new String();
            try {
                forecastStringArray= getWeatherDataFromJson(forecastJsonStr, 1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return forecastStringArray;

        }

        @Override


        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        protected void onPostExecute(String forecastStrings) {
            //View rootView= findViewById(R.layout.fragment_main_activity2);

            //forecastAdapter=new ArrayAdapter<String>(getActivity(),
            //      R.layout.list_item_forecast,R.id.list_item_forecasr_textview,forecast);
            System.out.println(" post execute!:  forecastStrings  "+forecastStrings);
            String[] weatherStrings=forecastStrings.split("/");
            maxTemperatureTextView.setText(weatherStrings[0]);
            minTemperatureTextView.setText(weatherStrings[1]);
            humidityTextView.setText(weatherStrings[2]);

            //forecastAdapter.clear();
            //forecastAdapter.addAll(forecastStrings);

        }



    }
}
