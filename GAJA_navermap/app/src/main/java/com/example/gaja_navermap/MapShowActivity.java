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

import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;

import java.util.ArrayList;

public class MapShowActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static LatLng CENTER;
    private ArrayList<double[]> dots_draw;
    private double[] dots_input;
    private Polyline myRoute;

    TextView nicknametv;
    TextView routenametv;
    Button removeButton;

    private String city;
    private String nickname;
    private String routename;
    private boolean removeable;

    private final String[] CITY = {"서울","부산","대구","인천","광주","대전","울산"};
    private final String DB_ROUTE_TABLE = "ROUTES";
    FirebaseFirestore database = FirebaseFirestore.getInstance();
    CurrentLoginedUser currUser = CurrentLoginedUser.GetInstance();

    protected void getIntentInfo(){
        Intent thisIntent = getIntent();
        city = thisIntent.getStringExtra("cityString");
        nickname = thisIntent.getStringExtra("nickname");
        routename = thisIntent.getStringExtra("routename");
        removeable = thisIntent.getBooleanExtra("removeable",false);
        dots_input =  thisIntent.getDoubleArrayExtra("dots");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapshow);
        getIntentInfo();

        nicknametv = (TextView) findViewById(R.id.mapshow_nicknametv);
        routenametv = (TextView) findViewById(R.id.mapshow_routenametv);
        removeButton = (Button) findViewById(R.id.routeDeleteButton);

        removeButton.setEnabled(removeable);
        removeButton.setVisibility(removeable?View.VISIBLE:View.INVISIBLE);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(MapShowActivity.this);
                dlg.setTitle("산책로 삭제");
                dlg.setMessage("삭제하시겠습니까?");
                dlg.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String id = city+"_"+currUser.GetNickname()+"_"+routename;
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

        nicknametv.setText(city+"에 사는 "+nickname+"님의 산책로");
        routenametv.setText(routename);

        dots_draw = convertDotsArray(dots_input);
        CENTER = new LatLng(dots_input[0],dots_input[1]);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapshow);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull NaverMap googleMap) {
        googleMap.setMinZoomPreference(5);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CENTER, 15));

        PolylineOptions myRouteOptions = new PolylineOptions();

        for(int i = 0 ;i<dots_draw.size();i++)
            myRouteOptions.add(new LatLng(dots_draw.get(i)[0],dots_draw.get(i)[1]));

        myRoute = googleMap.addPolyline(myRouteOptions);
        googleMap.addMarker(new MarkerOptions().position(CENTER).title("산책로 시작점"));
    }
    private ArrayList<double[]> convertDotsArray(double[] src_dots){
        ArrayList<double[]> convertedDots = new ArrayList<double[]>();

        for(int i=0;i<src_dots.length/2;i++){
            convertedDots.add(i,new double[] {src_dots[2*i],src_dots[2*i+1]});
        }
        return convertedDots;
    }
}
