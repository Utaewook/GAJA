package com.example.gaja_navermap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

public class InitActivity extends Activity {
    private Intent intent;

    public static int pc = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.init_main);

        if(SaveSharedPreference.getUserAutoLogin(getApplicationContext()) == false){
            intent = new Intent(InitActivity.this, Login_MainActivity.class);
            startActivity(intent);
            this.finish();
        }else{
            intent = new Intent(InitActivity.this, Menu_MainActivity.class);
            startActivity(intent);
            this.finish();
        }
    }
}
