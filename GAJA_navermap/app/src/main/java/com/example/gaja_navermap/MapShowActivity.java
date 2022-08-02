package com.example.gaja_navermap;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.PathOverlay;

import java.util.ArrayList;

public class MapShowActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static LatLng CENTER;
    private ArrayList<LatLng> dots_draw;
    private double[] dots_input;

    private TextView nickNametv;
    private TextView routeNametv;
    private Button removeButton;


    private String city;
    private String nickname;
    private String routeName;
    private boolean removable;

    private final String[] CITY = {"서울","부산","대구","인천","광주","대전","울산"};
    private final String DB_ROUTE_TABLE = "ROUTES";
    private FirebaseFirestore database = FirebaseFirestore.getInstance();
    private CurrentLoginedUser currUser = CurrentLoginedUser.GetInstance();

    protected void getIntentInfo(){
        Intent thisIntent = getIntent();
        city = thisIntent.getStringExtra("cityString");
        nickname = thisIntent.getStringExtra("nickname");
        routeName = thisIntent.getStringExtra("routename");
        removable = thisIntent.getBooleanExtra("removeable",false);
        dots_input =  thisIntent.getDoubleArrayExtra("dots");
        CENTER = new LatLng(dots_input[0],dots_input[1]);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapshow);
        getIntentInfo();

        nickNametv = (TextView) findViewById(R.id.mapshow_nicknametv);
        routeNametv = (TextView) findViewById(R.id.mapshow_routenametv);
        removeButton = (Button) findViewById(R.id.routeDeleteButton);

        removeButton.setEnabled(removable);
        removeButton.setVisibility(removable ?View.VISIBLE:View.INVISIBLE);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(MapShowActivity.this);
                dlg.setTitle("산책로 삭제");
                dlg.setMessage("삭제하시겠습니까?");
                dlg.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String id = city+"_"+currUser.GetNickname()+"_"+ routeName;
                        database.collection(DB_ROUTE_TABLE).document(id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(getApplicationContext(),"삭제가 완료되었습니다.",Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                    }
                }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });

                dlg.show();
            }
        });

        nickNametv.setText(city+"에 사는 "+nickname+"님의 산책로");
        routeNametv.setText(routeName);

        dots_draw = convertDotsArray(dots_input);

        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.mapshow);
        if(mapFragment == null){
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.mapshow,mapFragment).commit();
        }
    }

    @Override
    public void onMapReady(@NonNull NaverMap map) {
        map.setMinZoom(5);
        map.moveCamera(CameraUpdate.scrollTo(CENTER));
        PathOverlay myPath = new PathOverlay();
        myPath.setCoords(dots_draw);
        myPath.setMap(map);
//      googleMap.addMarker(new MarkerOptions().position(CENTER).title("산책로 시작점"));
    }

    private ArrayList<LatLng> convertDotsArray(double[] src_dots){
        ArrayList<LatLng> convertedDots = new ArrayList<LatLng>();

        for(int i=0;i<src_dots.length/2;i++){
            convertedDots.add(i,new LatLng(src_dots[2*i],src_dots[2*i+1]));
        }
        return convertedDots;
    }
}
