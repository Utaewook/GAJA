package com.example.gaja_navermap;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.PathOverlay;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.route_map);
        setTitle("나만의 경로 만들기");

        Intent thisIntent = getIntent();
        CENTER = new LatLng(thisIntent.getDoubleArrayExtra("CENTER")[0],thisIntent.getDoubleArrayExtra("CENTER")[1]);

        routeNameEDT = (EditText) findViewById(R.id.edt_routename);

        mapdrawSwitch = (Switch) findViewById(R.id.mapdraw_Switch);
        mapdrawSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                canIdraw = isChecked;
            }
        });

        saveRouteButton = (Button) findViewById(R.id.mapdrawSaveButton);
        saveRouteButton.setOnClickListener(new View.OnClickListener() {
            boolean routenameDup = true;
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

    }


    @Override
    public void onMapReady(NaverMap map) {
        map.setMinZoom(5);
        map.moveCamera(CameraUpdate.scrollTo(CENTER));
        myPath = new PathOverlay();
        myPath.setColor(Color.GREEN);

        map.setOnMapClickListener(new NaverMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull PointF pointF, @NonNull LatLng latLng) {
                if(canIdraw) {
                    myPathDots.add(latLng);
                    if(myPathDots.size()>=2) {
                        myPath.setCoords(myPathDots);
                        myPath.setMap(map);
                    }
                }
            }
        });
    }
}