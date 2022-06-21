package com.exomind.test.api;

import com.exomind.test.model.CityData;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface APIInterface {

    String UNITS = "metric"; // For temperature in Celsius
    String API_KEY = "658d2da2d3cc63e0f2957bf6885f342a";

    @GET("/data/2.5/weather?")
    Call<CityData> getCityWeatherData(@Query("q") String cityName, @Query("units") String units, @Query("APPID") String apiKey);
}
