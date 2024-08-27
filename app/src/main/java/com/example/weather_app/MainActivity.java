package com.example.weather_app;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;

public class MainActivity extends AppCompatActivity {
    TextView cityName;
    Button search;
    TextView show;
    String url;

    // Replace with your API key
    private final String API_KEY = "3830555481938540432a7295b8ca89aa";

    // Create a background executor
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize your views here
        cityName = findViewById(R.id.cityName);
        search = findViewById(R.id.search);
        show = findViewById(R.id.weather);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = cityName.getText().toString().trim();
                if (!city.isEmpty()) {
                    url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + API_KEY;
                    fetchWeatherData(url);
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a city", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void fetchWeatherData(String url) {
        executor.execute(() -> {
            StringBuilder result = new StringBuilder();
            try {
                URL weatherUrl = new URL(url);
                HttpURLConnection urlConnection = (HttpURLConnection) weatherUrl.openConnection();
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }
                String response = result.toString();

                // Update UI on the main thread
                handler.post(() -> parseWeatherData(response));

            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() -> show.setText("Unable to retrieve weather data"));
            }
        });
    }

    private void parseWeatherData(String result) {
        if (result == null) {
            show.setText("Unable to retrieve weather data");
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONObject main = jsonObject.getJSONObject("main");
            String weatherInfo =
                    "Temperature: " + main.getString("temp") + "K\n" +
                            "Feels Like: " + main.getString("feels_like") + "K\n" +
                            "Temperature Max: " + main.getString("temp_max") + "K\n" +
                            "Temperature Min: " + main.getString("temp_min") + "K\n" +
                            "Pressure: " + main.getString("pressure") + " hPa\n" +
                            "Humidity: " + main.getString("humidity") + "%";
            show.setText(weatherInfo);
        } catch (Exception e) {
            e.printStackTrace();
            show.setText("Error parsing weather data");
        }
    }
}
