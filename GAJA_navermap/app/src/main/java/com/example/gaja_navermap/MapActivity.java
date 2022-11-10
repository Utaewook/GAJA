package com.example.gaja_navermap;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.PathOverlay;
import com.naver.maps.map.util.FusedLocationSource;

import java.util.ArrayList;
import java.util.HashMap;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static LatLng CENTER;

    private final String DB_ROUTES_TABLE = "ROUTES";
    private final String[] CITY = {"서울","부산","대구","인천","광주","대전","울산"};

    private boolean canIdraw = false;
    private ArrayList<LatLng> myPathDots = new ArrayList<LatLng>();
    private PathOverlay myPath;

    private Switch mapdrawSwitch;
    private Button saveRouteButton;
    private EditText routeNameEDT;

    private FirebaseFirestore database = FirebaseFirestore.getInstance();
    private CurrentLoginedUser currUser = CurrentLoginedUser.GetInstance();

    private NaverMap naverMap;
    private FusedLocationSource locationSource;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.route_map);
        setTitle("나만의 경로 만들기");

        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                //Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        checkPermissions(permissions);
        Intent thisIntent = getIntent();

        CENTER = new LatLng(thisIntent.getDoubleArrayExtra("CENTER")[0],thisIntent.getDoubleArrayExtra("CENTER")[1]);
        // CENTER = new LatLng(currUser.GetLocationSource().getLastLocation().getLatitude(),currUser.GetLocationSource().getLastLocation().getLongitude());

        routeNameEDT = (EditText) findViewById(R.id.edt_routename);
        routeNameEDT.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (i == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(routeNameEDT.getWindowToken(), 0);    //hide keyboard
                    return true;
                }
                return false;
            }
        });

        mapdrawSwitch = (Switch) findViewById(R.id.mapdraw_Switch);
        mapdrawSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                canIdraw = isChecked;
            }
        });

        saveRouteButton = (Button) findViewById(R.id.mapdrawSaveButton);
        saveRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myPathDots.isEmpty()||myPathDots.size()==1){
                    Toast.makeText(getApplicationContext(),"산책로를 선택해주세요",Toast.LENGTH_SHORT).show();
                    return;
                }

                String routeNameInput = routeNameEDT.getText().toString();
                if(routeNameInput.isEmpty()){
                    Toast.makeText(getApplicationContext(),"산책로 이름을 입력해주세요",Toast.LENGTH_SHORT).show();
                    return;
                }

                String id = CITY[currUser.GetCity()]+"_"+currUser.GetNickname()+"_"+routeNameInput;
                HashMap<String,Object> route_save = new HashMap<>();
                route_save.put("routename",routeNameInput);
                route_save.put("nickname",currUser.GetNickname());
                route_save.put("city",currUser.GetCity());
                route_save.put("dots",myPathDots);
                database.collection(DB_ROUTES_TABLE).document(id).set(route_save).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getApplicationContext(),"저장 되었습니다.",Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("경로 DB저장", "Failure");
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                });
            }
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map);
        if(mapFragment == null){
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map,mapFragment).commit();
        }
        mapFragment.getMapAsync(this);

        locationSource = new FusedLocationSource(this,LOCATION_PERMISSION_REQUEST_CODE);
        Log.d("확인좀2", "onMapReady: "+locationSource.isActivated());

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
    public void onMapReady(NaverMap map) {
        this.naverMap = map;
        naverMap.setMinZoom(5);
        //naverMap.setLocationSource(locationSource);
        naverMap.moveCamera(CameraUpdate.scrollTo(CENTER));
//        Log.d("확인좀", "onMapReady: "+locationSource.toString());
//        Log.d("확인좀", "onMapReady: "+locationSource.isActivated());
//        Log.d("확인좀", "onMapReady: lat = "+Double.toString(locationSource.getLastLocation().getLatitude()));
//        Log.d("확인좀", "onMapReady: lng = "+Double.toString(locationSource.getLastLocation().getLongitude()));
        myPath = new PathOverlay();
        myPath.setColor(Color.GREEN);

        naverMap.setOnMapClickListener(new NaverMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull PointF pointF, @NonNull LatLng latLng) {
                if(canIdraw) {
                    myPathDots.add(latLng);
                    if(myPathDots.size()>=2) {
                        myPath.setCoords(myPathDots);
                        myPath.setMap(naverMap);
                    }
                }else{
                    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(routeNameEDT.getWindowToken(), 0);    //hide keyboard
                }
            }
        });
    }

    protected void checkPermissions(String[] permissions){
        ArrayList<String> targetList = new ArrayList<>();
        for(int i = 0; i < permissions.length; i++){
            String curPermission = permissions[i];
            int permissionCheck = ContextCompat.checkSelfPermission(this,curPermission);
            if(permissionCheck == PackageManager.PERMISSION_GRANTED){
                Log.d("Permission granting/", "checkPermissions: " + curPermission + " 권한 있음");
            }else {
                Log.d("Permission granting/", "checkPermissions: " + curPermission + " 권한 없음");
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,curPermission)){
                    Log.d("Permission granting/", "checkPermissions: " + curPermission + " 권한 설명 필요");
                }else{
                    targetList.add(curPermission);
                }
            }
        }

        String[] targets = new String[targetList.size()];
        targetList.toArray(targets);

        ActivityCompat.requestPermissions(this,targets,LOCATION_PERMISSION_REQUEST_CODE);
    }
}