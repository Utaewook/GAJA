package com.example.gaja_navermap;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login_MainActivity extends AppCompatActivity {

    Button joinButton;
    Button loginButton;
    CheckBox loginAUTOCHECK;

    EditText loginIDEdt;
    EditText loginPWEdt;

    FirebaseFirestore database = FirebaseFirestore.getInstance();
    CurrentLoginedUser curUser = CurrentLoginedUser.GetInstance();

    private final String DB_USER_TABLE = "USERS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_main);

        loginIDEdt = (EditText) findViewById(R.id.login_id);
        loginPWEdt = (EditText) findViewById(R.id.login_pw);
        loginAUTOCHECK = (CheckBox) findViewById(R.id.login_autologin);

        joinButton = (Button) findViewById(R.id.login_joinBtn);
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), com.example.gaja_navermap.Join_MainActivity.class);
                startActivity(intent);
            }
        });

        loginButton = (Button) findViewById(R.id.login_loginBtn);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = loginIDEdt.getText().toString();
                String pw = loginPWEdt.getText().toString();

                if(id.isEmpty()){
                    Toast.makeText(getApplicationContext(),"아이디를 입력해주세요.",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(pw.isEmpty()){
                    Toast.makeText(getApplicationContext(),"비밀번호를 입력해주세요.",Toast.LENGTH_SHORT).show();
                    return;
                }

                database.collection(DB_USER_TABLE).document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if(document.getData()!=null){
                                String dbID = document.getString("id");
                                String dbPW = document.getString("password");
                                String dbNickname = document.getString("nickname");
                                int dbCity = Integer.parseInt(document.get("city").toString());

                                if(dbPW.compareTo(pw)!=0){
                                    Toast.makeText(getApplicationContext(),"비밀번호가 일치하지 않습니다.",Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                curUser.SetUserData(dbID,dbNickname,dbPW,dbCity,loginAUTOCHECK.isChecked());
                                SaveSharedPreference.setUser(Login_MainActivity.this, curUser);

                                Intent mainPage = new Intent(getApplicationContext(), com.example.gaja_navermap.Menu_MainActivity.class);
                                mainPage.putExtra("id",curUser.GetID());
                                mainPage.putExtra("nickname",curUser.GetNickname());
                                mainPage.putExtra("city",curUser.GetCity());
                                startActivity(mainPage);
                                Toast.makeText(getApplicationContext(),"로그인 하였습니다.",Toast.LENGTH_SHORT).show();
                                finish();

                            }else{
                                Toast.makeText(getApplicationContext(), "정보와 일치하는 회원이 없습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }else {
                            Log.e("LOGIN", "로그인 과정에서 진행한 DB 접근 오류");
                        }
                    }
                });
            }
        });

    }
}