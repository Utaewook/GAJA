package com.example.gaja;

import androidx.annotation.NonNull;

public class CurrentLoginedUser {

    private static CurrentLoginedUser Instance = null;
    private String id = null;
    private String password = null;
    private String nickname = null;
    private int city = -1;

    private CurrentLoginedUser() {}
    public static CurrentLoginedUser GetInstance(){
        if(Instance == null){
            Instance = new CurrentLoginedUser();
        }
        return Instance;
    }

    public void SetUserData(String id, String nickname, String password, int city){
        this.id = id;
        this.nickname = nickname;
        this.password = password;
        this.city = city;
    }

    public void ResetUserData(){
        this.id = null;
        this.nickname = null;
        this.password = null;
        this.city = -1;
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

    public void SetID(String id) { this.id = id; }
    public void SetPassword(String password) { this.password = password; }
    public void SetNickname(String nickname) { this.nickname = nickname; }
    public void SetCity(int city) { this.city = city; }
}
