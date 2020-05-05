package com.example.myweatherforecast;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    String ApiKey = "a8934a33b74774a52a92e072ab3b4d55";
    String addressApi = "http://api.openweathermap.org/data/2.5/weather?%s&appid=%s";

    Button searchCityButton;
    TextView WeatherShowText;
    EditText InputAddress;

    private final int MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 100;
    private FusedLocationProviderClient fusedLocationClient;

    String locationLat = "";
    String locationLon = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchCityButton = findViewById(R.id.SearchCityButton);
        WeatherShowText = findViewById(R.id.WeatherShowText);
        InputAddress = findViewById(R.id.InputAddress);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_ACCESS_COARSE_LOCATION);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location)
                    {
                        if (location != null)
                        {
                            locationLat = Double.toString(location.getLatitude());
                            locationLon = Double.toString(location.getLongitude());

                            Log.i("MyResult", "locationLat: " + locationLat);
                            Log.i("MyResult", "locationLon: " + locationLon);
                        }
                    }
                });

    }




    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public void SearchCityButton_onClick(View view)
    {
        String city = InputAddress.getText().toString().trim();
        if(!city.isEmpty())
        {
            DownloadWeather downloadWeather = new DownloadWeather();
            String finalUrl = String.format(addressApi, "q=" + city, ApiKey);
            downloadWeather.execute(finalUrl);
        }
        else
        {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Город не может быть пустым!",
                    Toast.LENGTH_SHORT);
            toast.show();
        }

    }

    public void SearchGPS_onClick(View view)
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_ACCESS_COARSE_LOCATION);
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location)
                    {
                        if (location != null)
                        {
                            locationLat = Double.toString(location.getLatitude());
                            locationLon = Double.toString(location.getLongitude());

                            Log.i("MyResult", "locationLat: " + locationLat);
                            Log.i("MyResult", "locationLon: " + locationLon);
                        }
                    }
                });


        if(!locationLat.isEmpty() && !locationLon.isEmpty())
        {
            DownloadWeather downloadWeather = new DownloadWeather();
            String data =  "lat=" + locationLat + "&lon=" + locationLon;
            String finalUrl = String.format(addressApi, data, ApiKey);
            downloadWeather.execute(finalUrl);
        }
        else
        {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Координаты еще не определены!",
                    Toast.LENGTH_SHORT);
            toast.show();
        }

    }


    private class DownloadWeather extends AsyncTask<String, Void, String>
    {

        @Override
        protected String doInBackground(String... strings) {

            URL url = null;
            HttpURLConnection connection = null;
            StringBuilder fullString = new StringBuilder();

            try
            {
                url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(reader);

                String line = bufferedReader.readLine();
                while (line != null)
                {
                    fullString.append(line);
                    line = bufferedReader.readLine();
                }

            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            finally
            {
                if(connection != null)
                {
                    connection.disconnect();
                }
            }
            return fullString.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try
            {
                JSONObject WeatherObjectJSON = new JSONObject(s);
                String city = WeatherObjectJSON.getString("name");
//                Integer tempInt = Math.round(Float.parseFloat(WeatherObjectJSON.
//                        getJSONObject("main").getString("temp")) / 274.15f);
//                String temp =  tempInt.toString();
                Integer temp = Math.round(Float.parseFloat(WeatherObjectJSON.getJSONObject("main").getString("temp")) - 273f);
                String description = WeatherObjectJSON
                        .getJSONArray("weather").getJSONObject(0).getString("description");

                String weather = String.format("%s\nТемпература: %sС°\nНа улице: %s",city,temp.toString(),description);
                WeatherShowText.setText(weather);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_ACCESS_COARSE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {

                }
                else
                {

                }

                return;
            }
        }
    }
}
