package com.example.gaja_navermap;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.util.FusedLocationSource;

import java.util.ArrayList;

public class InitActivity extends AppCompatActivity implements OnMapReadyCallback {
    private Intent intent;

    private NaverMap naverMap;
    private FusedLocationSource locationSource;

    private boolean isLocationLoaded = false;
    private double lat = LOCATIONSOURCE_LATLNG_FIND_FAIL;
    private double lng = LOCATIONSOURCE_LATLNG_FIND_FAIL;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private static final double LOCATIONSOURCE_LATLNG_FIND_FAIL = -1000.0;


    public static int pc = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.init_main);

        // 경로 추천을 위한 현재위치 잠깐 쓰기!
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        checkPermissions(permissions);
        locationSource = new FusedLocationSource(this,LOCATION_PERMISSION_REQUEST_CODE);


        if(SaveSharedPreference.getUserAutoLogin(getApplicationContext()) == false){// 현재 로그인된 정보가 없는 경우 로그인 창으로
            intent = new Intent(InitActivity.this, Login_MainActivity.class);
            startActivity(intent);
            this.finish();
        }else{  // 현재 로그인된 정보가 있는 경우 메인화면으로
            intent = new Intent(InitActivity.this, Menu_MainActivity.class);

            FragmentManager fm = getSupportFragmentManager();
            MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.init_location_map);
            if (mapFragment == null) {
                mapFragment = MapFragment.newInstance();
                fm.beginTransaction().add(R.id.init_location_map, mapFragment).commit();
            }
            mapFragment.getMapAsync(this);

            Log.d(this.getLocalClassName(), "onCreate: (" + Double.toString(lat) + ", " + Double.toString(lng) + ")");

            double[] latlng = new double[]{lat, lng};
            intent.putExtra("latlng", latlng);
            Log.d(this.getLocalClassName(), "onCreate: (" + Double.toString(lat) + ", " + Double.toString(lng) + ")");


            startActivity(intent);
            this.finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(requestCode,permissions,grantResults)){
            if(!locationSource.isActivated()){
                naverMap.setLocationTrackingMode(LocationTrackingMode.None);
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onMapReady(@NonNull NaverMap map) {
        this.naverMap = map;
        naverMap.setLocationSource(locationSource);
        Log.d("", "맵 떴나?");
//        lat = locationSource.getLastLocation().getLatitude();
//        lng = locationSource.getLastLocation().getLongitude();
    }

    protected void checkPermissions(String[] permissions) {
        ArrayList<String> targetList = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            String curPermission = permissions[i];
            int permissionCheck = ContextCompat.checkSelfPermission(this, curPermission);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permission granting/", "checkPermissions: " + curPermission + " 권한 있음");
            } else {
                Log.d("Permission granting/", "checkPermissions: " + curPermission + " 권한 없음");
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, curPermission)) {
                    Log.d("Permission granting/", "checkPermissions: " + curPermission + " 권한 설명 필요");
                } else {
                    targetList.add(curPermission);
                }
            }
        }

        String[] targets = new String[targetList.size()];
        targetList.toArray(targets);

        ActivityCompat.requestPermissions(this, targets, LOCATION_PERMISSION_REQUEST_CODE);
    }
}
