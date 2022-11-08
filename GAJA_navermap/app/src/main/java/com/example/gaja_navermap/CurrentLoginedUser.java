package com.example.gaja_navermap;

import androidx.annotation.NonNull;

import com.naver.maps.map.util.FusedLocationSource;


public class CurrentLoginedUser {

    private static CurrentLoginedUser Instance = null;
    private String id = null;
    private String password = null;
    private String nickname = null;
    private int city = -1;
    private boolean autologin = false;
    private FusedLocationSource locationSource = null;

    private CurrentLoginedUser() {}
    public static CurrentLoginedUser GetInstance(){
        if(Instance == null){
            Instance = new CurrentLoginedUser();
        }
        return Instance;
    }

    public void SetUserData(String id, String nickname, String password, int city, boolean autologin){
        this.id = id;
        this.nickname = nickname;
        this.password = password;
        this.city = city;
        this.autologin = autologin;
    }

    public void ResetUserData(){
        this.id = null;
        this.nickname = null;
        this.password = null;
        this.city = -1;
        this.autologin = false;
    }

    public boolean HasLoginUserData(){
        if(id != null && nickname != null && password != null && city != -1){
            return true;
        }
        return  false;
    }

    @NonNull
    @Override
    public String toString() {
        return "ID : " + id + "\nNickname : " + nickname + "\nPassword : " + password+ "\nCity : " + city;
    }

    public String GetID() { return id; }
    public String GetPassword() { return password; }
    public String GetNickname() { return nickname; }
    public int GetCity() { return city; }
    public boolean GetAutoLogin(){ return autologin; }
    public FusedLocationSource GetLocationSource() { return locationSource; }

    public void SetID(String id) { this.id = id; }
    public void SetPassword(String password) { this.password = password; }
    public void SetNickname(String nickname) { this.nickname = nickname; }
    public void SetCity(int city) { this.city = city; }
    public void SetAutoLogin(boolean autologin) { this.autologin = autologin; }
    public void SetLocationSource(FusedLocationSource locationSource){ this.locationSource = locationSource; }
}
