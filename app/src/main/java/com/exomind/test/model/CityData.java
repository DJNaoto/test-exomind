package com.exomind.test.model;

import com.google.gson.annotations.SerializedName;

public class CityData {

    public String cityName;

    @SerializedName("main")
    public MainData main;
    @SerializedName("clouds")
    public CloudsData clouds;


    public class MainData {
        @SerializedName("temp")
        public Float temp;
    }

    public class CloudsData {
        @SerializedName("all")
        public Integer all;
    }

    public CityData(String cityName, MainData main, CloudsData clouds){
        this.cityName = cityName;
        this.main = main;
        this.clouds = clouds;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }
}
