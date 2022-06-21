package com.exomind.test;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.exomind.test.api.APIClient;
import com.exomind.test.api.APIInterface;
import com.exomind.test.model.CityData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SecondActivity extends AppCompatActivity {

    private int COUNTDOWN_TIME = 60000;

    private List<String> cityList;
    private List<String> messageList;
    private List<CityData> cityDataList;
    private CountDownTimer messagesCountDownTimer;
    private CountDownTimer getDataCountDownTimer;
    private CountDownTimer progressBarCountDownTimer;

    APIInterface apiInterface;

    TextView messageTextView;
    ProgressBar progressBar;
    Button restartButton;
    TableLayout tl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_activity);
        init();
    }

    private void init(){
        cityList = Arrays.asList(getString(R.string.city_1), getString(R.string.city_2), getString(R.string.city_3), getString(R.string.city_4), getString(R.string.city_5));
        messageList = Arrays.asList(getString(R.string.message_1), getString(R.string.message_2), getString(R.string.message_3));
        cityDataList = new ArrayList<>();

        apiInterface = APIClient.getClient().create(APIInterface.class);

        messageTextView = findViewById(R.id.textViewMessage);
        progressBar = findViewById(R.id.progressBar);
        restartButton = findViewById(R.id.buttonRestart);
        tl = (TableLayout) findViewById(R.id.tableLayout);

        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                restartButton.setVisibility(View.GONE);
                cleanTable();
                startTimers();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        startTimers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        messagesCountDownTimer.cancel();
        getDataCountDownTimer.cancel();
        progressBarCountDownTimer.cancel();
    }

    private void startTimers(){

        // timer for progress bar
        progressBarCountDownTimer = new CountDownTimer(COUNTDOWN_TIME, 1000) {
            @Override
            public void onTick(long l) {
                int progress = (int)(100-((100*l)/COUNTDOWN_TIME));
                progressBar.setProgress(progress);
            }

            @Override
            public void onFinish() {
                progressBar.setProgress(100);
            }
        };
        progressBarCountDownTimer.start();

        // timer for messages
        messagesCountDownTimer = new CountDownTimer(COUNTDOWN_TIME, 6000) {
            int messageIndex = 0;

            @Override
            public void onTick(long l) {
                messageTextView.setText(messageList.get(messageIndex));
                messageIndex = messageIndex == messageList.size()-1 ? 0 : messageIndex+1;
            }

            @Override
            public void onFinish() {
            }
        };
        messagesCountDownTimer.start();

        // timer for weather data
        getDataCountDownTimer = new CountDownTimer(COUNTDOWN_TIME, 10000) {
            int cityIndex = 0;
            @Override
            public void onTick(long l) {
                if(cityIndex < cityList.size()) getWeatherData(cityIndex);
                cityIndex++;
            }

            @Override
            public void onFinish() {
                for(int i=0; i<cityDataList.size(); i++){
                    addRow(cityDataList.get(i));
                }
                messageTextView.setText("");
                progressBar.setVisibility(View.GONE);
                restartButton.setVisibility(View.VISIBLE);
            }
        };
        getDataCountDownTimer.start();
    }

    private void getWeatherData(int cityIndex){
        String cityName = cityList.get(cityIndex);
        Call<CityData> call = apiInterface.getCityWeatherData(cityName, APIInterface.UNITS, APIInterface.API_KEY);
        call.enqueue(new Callback<CityData>() {
            @Override
            public void onResponse(Call<CityData> call, Response<CityData> response) {
                Log.d("TAG",response.code()+"");
                CityData data = response.body();
                data.setCityName(cityName);
                cityDataList.add(data);
            }

            @Override
            public void onFailure(Call<CityData> call, Throwable t) {
                cityDataList.add(new CityData(cityName, null, null));
                call.cancel();
            }
        });
    }

    private void addRow(CityData data){
        String cityName = data.cityName;
        CityData.MainData main = data.main;
        CityData.CloudsData clouds = data.clouds;

        View row = getLayoutInflater().inflate(R.layout.tablerow_item, null);
        TextView city = row.findViewById(R.id.textViewCity);
        city.setText(cityName);

        TextView temperature = row.findViewById(R.id.textViewTemperature);
        if(main != null){
            temperature.setText(String.format("%.1f°C", main.temp)); // 2 decimal places -> 1 decimal place
        } else {
            temperature.setText(getString(R.string.data_error));
        }

        LinearLayout cloudCover = row.findViewById(R.id.linearLayoutCloudCover);
        if(clouds != null){
            int cloudsCount = clouds.all/25;
            // cloud value -> number of cloud icons : 0-24 -> 0 | 25-49 -> 1 | 50-74 -> 2 | 75-100 -> 3
            for(int i=0; i<3; i++){
                cloudCover.getChildAt(i).setVisibility(i < cloudsCount ? View.VISIBLE : View.INVISIBLE);
            }
        }

        tl.addView(row, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
    }

    private void cleanTable(){
        cityDataList = new ArrayList<>();
        for(int i=tl.getChildCount()-1; i > 0; i--){
            tl.removeViewAt(i);
        }
    }
}
