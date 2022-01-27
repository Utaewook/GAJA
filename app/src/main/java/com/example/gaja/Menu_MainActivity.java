package com.example.gaja;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.type.Color;
import com.google.type.LatLng;

import java.util.ArrayList;
import java.util.HashMap;

public class Menu_MainActivity extends TabActivity {

    TabHost tabHost;
    TabHost.TabSpec recommendSpec;
    TabHost.TabSpec myRouteSpec;
    TabHost.TabSpec anotherCitySpec;
    TabHost.TabSpec myInfoSpec;

    TextView nicknameTV;
    TextView cityTV;
    TextView routecountTV;

    LinearLayout recommendll;
    LinearLayout myroutell;
    LinearLayout anothercityll;

    Button addbutton;

    private final String[] CITY = {"서울","부산","대구","인천","광주","대전","울산"};
    private int myRouteCount = 0;
    private boolean countStarted = false;
    private enum city {서울,부산,대구,인천,광주,대전,울산};
    private final double[][] latlngs = {{37.566400449054065, 126.97806415190496}, //서울 시청 좌표
                                        {35.17982606079264, 129.07499314916123}, //부산 시청 좌표
                                        {35.87138702960645, 128.60174586138197}, //대구 시청 좌표
                                        {37.456191773597624, 126.70590628875505}, //인천 시청좌표
                                        {37.429518437125346, 127.25520647012537}, //광주 시청좌표
                                        {36.35063814242206, 127.38484015474755}, //대전 시청좌표
                                        {35.53969222181237, 129.31149541054342} //울산 시청좌표
                                        };

    FirebaseFirestore database = FirebaseFirestore.getInstance();
    CurrentLoginedUser currUser = CurrentLoginedUser.GetInstance();

    private final String DB_ROUTE_TABLE = "ROUTES";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);

        Intent intent = getIntent();
        currUser.SetID(intent.getStringExtra("id"));
        currUser.SetNickname(intent.getStringExtra("nickname"));
        currUser.SetCity(intent.getIntExtra("city",-1));

        addbutton = (Button) findViewById(R.id.addRouteButton);
        addbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapIntent = new Intent(getApplicationContext(),MapActivity.class);
                int city = currUser.GetCity();
                mapIntent.putExtra("CENTER",latlngs[city]);
                startActivity(mapIntent);
            }
        });

        nicknameTV = (TextView)findViewById(R.id.info_NicknameTv);
        cityTV = (TextView)findViewById(R.id.info_CityTv);
        routecountTV = (TextView)findViewById(R.id.info_RouteCountTv);

        tabHost = getTabHost();
        recommendll = (LinearLayout) findViewById(R.id.RecommendLinearLayout);
        myroutell = (LinearLayout) findViewById(R.id.MyRouteLinearLayout);
        anothercityll = (LinearLayout) findViewById(R.id.AnotherCityLinearLayout);

        recommendll.setLongClickable(false);
        myroutell.setLongClickable(true);
        anothercityll.setLongClickable(false);

        recommendSpec = tabHost.newTabSpec("Tag1").setIndicator("추천 산책로");
        recommendSpec.setContent(R.id.RecommendedRoutesTab);
        setRecommendll();
        tabHost.addTab(recommendSpec);

        myRouteSpec = tabHost.newTabSpec("Tag2").setIndicator("나만의\n산책로");
        myRouteSpec.setContent(R.id.MyRouteTab);
        setMyRoutell();
        tabHost.addTab(myRouteSpec);

        anotherCitySpec = tabHost.newTabSpec("Tag3").setIndicator("다른 도시");
        anotherCitySpec.setContent(R.id.anotherCityTab);
        setAnotherCityll();
        tabHost.addTab(anotherCitySpec);

        myInfoSpec = tabHost.newTabSpec("Tag4").setIndicator("내정보");
        myInfoSpec.setContent(R.id.MyInformationTab);
        setMyInfoTab();
        tabHost.addTab(myInfoSpec);

        tabHost.setCurrentTab(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setRecommendll();
        setMyRoutell();
        setAnotherCityll();
    }

    private void setRecommendll(){
        database.collection(DB_ROUTE_TABLE).whereEqualTo("city",currUser.GetCity()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                recommendll.removeAllViews();
                if(task.isSuccessful()){
                    for(DocumentSnapshot document : task.getResult()){
                        if(document.getData()!=null){
                            ArrayList<HashMap> db_routeDots = (ArrayList<HashMap>) document.get("dots");
                            int db_city = Integer.parseInt(document.get("city").toString());
                            String db_routename = document.getString("routename");
                            String db_nickname = document.getString("nickname");

                            makeNewContents(recommendll,db_routename,db_nickname,db_city,db_routeDots,false);
                        }else{
                            Toast.makeText(getApplicationContext(),"아직 내 도시의 경로가 없습니다.",Toast.LENGTH_SHORT).show();
                        }
                    }
                }else{
                    Log.e("내 도시의 추천 경로", "DB에 접근실패" );
                }
            }
        });
    }

    private void setMyRoutell(){
        database.collection(DB_ROUTE_TABLE).whereEqualTo("nickname",currUser.GetNickname()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                myroutell.removeAllViews();
                myroutell.setLongClickable(true);
                myRouteCount=0;
                if(task.isSuccessful()){
                    for(DocumentSnapshot document: task.getResult()){
                        if(document.getData()!=null){
                            ArrayList<HashMap> db_routeDots = (ArrayList<HashMap>) document.get("dots");
                            int db_city = Integer.parseInt(document.get("city").toString());
                            String db_routename = document.getString("routename");
                            String db_nickname = document.getString("nickname");
                            myRouteCount++;
                            countStarted = true;

                            makeNewContents(myroutell,db_routename,db_nickname,db_city,db_routeDots,true);
                        }
                    }
                    setMyInfoTab();
                    myroutell.addView(addbutton);
                }else{
                    Log.e("MYROUTE", "onComplete: 나만의 산책로 DB접근 에러");
                }
            }
        });
    }

    private void setAnotherCityll(){
        database.collection(DB_ROUTE_TABLE).whereNotEqualTo("city",currUser.GetCity()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                anothercityll.removeAllViews();
                if(task.isSuccessful()){
                    for(DocumentSnapshot document: task.getResult()){
                        if(document.getData()!=null){
                            ArrayList<HashMap> db_routeDots = (ArrayList<HashMap>) document.get("dots");
                            int db_city = Integer.parseInt(document.get("city").toString());
                            String db_routename = document.getString("routename");
                            String db_nickname = document.getString("nickname");

                            makeNewContents(anothercityll,db_routename,db_nickname,db_city,db_routeDots,false);
                        }
                    }
                }else{
                    Log.e("ANOTHER_CITY_ROUTE", "onComplete: 다른도시의 산책로 DB접근 에러");
                }
            }
        });
    }

    private void setMyInfoTab(){
        nicknameTV.setText(currUser.GetNickname());
        cityTV.setText(CITY[currUser.GetCity()]);
        if(countStarted)
            routecountTV.setText(Integer.toString(myRouteCount)+" 개");
        else
            routecountTV.setText("불러 오는중..");
    }

    private void makeNewContents(LinearLayout layout,String routeName,String nickname,int city,ArrayList<HashMap> dots,boolean removeable){
        LinearLayout templl = new LinearLayout(getApplicationContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        TextView usertv = new TextView(getApplicationContext());
        TextView routetv = new TextView(getApplicationContext());
        params.topMargin = 10;

        usertv.setText(CITY[city]+"에 사는 "+nickname+"님의");
        usertv.setTextSize(20);
        usertv.setGravity(Gravity.LEFT);

        routetv.setText(routeName);
        routetv.setTextSize(40);
        routetv.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.black));
        routetv.setGravity(Gravity.RIGHT);

        templl.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        templl.setOrientation(LinearLayout.VERTICAL);
        templl.setClickable(true);
        templl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapViewIntent = new Intent(getApplicationContext(),MapShowActivity.class);
                mapViewIntent.putExtra("dots",convertDots(dots));
                mapViewIntent.putExtra("nickname",nickname);
                mapViewIntent.putExtra("routename",routeName);
                mapViewIntent.putExtra("cityString",CITY[city]);
                mapViewIntent.putExtra("removeable",layout.isLongClickable());
                startActivity(mapViewIntent);
            }
        });
        myroutell.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });

        templl.addView(usertv,params);
        templl.addView(routetv,params);

        layout.addView(templl);
    }

    private double[] convertDots(ArrayList<HashMap> dots){
        double[] convertedDots = new double[dots.size()*2];

        for(int i = 0;i<convertedDots.length;i++){
            if(i%2==0)
                convertedDots[i] = (double) ((HashMap)dots.get(i/2)).get("latitude");
            else
                convertedDots[i] = (double) ((HashMap)dots.get((i-1)/2)).get("longitude");
        }

        return convertedDots;
    }
}
