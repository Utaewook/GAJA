package com.example.gaja_navermap;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

public class Join_MainActivity extends AppCompatActivity {

    private final String DB_USER_TABLE = "USERS";
    private final String[] CITY = {"서울","부산","대구","인천","광주","대전","울산"};
    boolean hasDupID = false;
    boolean hasDupNickname = false;
    boolean checkDupID = false;
    boolean checkDupNickname = false;
    boolean citySelected = false;

    int myCity;

    Button loginButton;
    Button joinButton;
    Button nicknameDupCheckButton;
    Button idDupCheckButton;

    EditText editTxtID;
    EditText editTxtNickname;
    EditText editTxtPassword;
    EditText editTxtPasswordCheck;
    Spinner citySelectSpinner;

    FirebaseFirestore database = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.join_main);
        setTitle("회원가입");

        editTxtID = (EditText)findViewById(R.id.join_id);
        editTxtNickname = (EditText)findViewById(R.id.join_nickname);
        editTxtPassword = (EditText)findViewById(R.id.join_pw);
        editTxtPasswordCheck = (EditText)findViewById(R.id.join_pwChk);

        citySelectSpinner = (Spinner) findViewById(R.id.join_cityselect);

        citySelectSpinner.setAdapter(new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,CITY));
        citySelectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                citySelected = true;
                myCity = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        loginButton = (Button) findViewById(R.id.join_loginBtn);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        idDupCheckButton = (Button) findViewById(R.id.overlapChkBtn1);
        idDupCheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDupID = true;
                String id = editTxtID.getText().toString();

                if(id.isEmpty()){
                    Toast.makeText(getApplicationContext(),"아이디를 입력해주세요.",Toast.LENGTH_SHORT).show();
                    return;
                }

                database.collection(DB_USER_TABLE).whereEqualTo("id",id).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            boolean isDupID = false;
                            for(QueryDocumentSnapshot document : task.getResult()){
                                Toast.makeText(getApplicationContext(), "중복된 아이디입니다.", Toast.LENGTH_SHORT).show();
                                hasDupID = true;
                                isDupID = true;
                                break;
                            }

                            if (!hasDupID || !isDupID) {
                                Toast.makeText(getApplicationContext(), "사용 가능한 아이디입니다.", Toast.LENGTH_SHORT).show();
                                hasDupID = false;
                            }
                        }
                    }
                });
            }
        });

        nicknameDupCheckButton = (Button) findViewById(R.id.overlapChkBtn2);
        nicknameDupCheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDupNickname = true;
                String nickname = editTxtNickname.getText().toString();

                if (nickname.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "닉네임을 작성하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                database.collection(DB_USER_TABLE).whereEqualTo("nickname", nickname)
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            boolean isDupNickname = false;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Toast.makeText(getApplicationContext(), "중복된 닉네임입니다.", Toast.LENGTH_SHORT).show();
                                hasDupNickname = true;
                                isDupNickname = true;
                                break;
                            }

                            if (!hasDupNickname || !isDupNickname) {
                                Toast.makeText(getApplicationContext(), "사용 가능한 닉네임입니다.", Toast.LENGTH_SHORT).show();
                                hasDupNickname = false;
                            }
                        }
                    }
                });
            }
        });

        joinButton = (Button) findViewById(R.id.join_joinBtn);
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = editTxtID.getText().toString();
                String nickname = editTxtNickname.getText().toString();
                String pw = editTxtPassword.getText().toString();
                String pwchk = editTxtPasswordCheck.getText().toString();

                if(!checkDupID || !checkDupNickname){
                    Toast.makeText(getApplicationContext(), "중복확인을 해주세요.",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(hasDupID||hasDupNickname){
                    Toast.makeText(getApplicationContext(),"중복된 아이디 혹은 닉네임 입니다.",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (id.isEmpty() || nickname.isEmpty() || pw.isEmpty() || pwchk.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "모든 항목을 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(pw.compareTo(pwchk)!=0){
                    Toast.makeText(getApplicationContext(), "비밀번호와 비밀번호 확인이 맞지 않습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!citySelected){
                    Toast.makeText(getApplicationContext(), "도시를 선택 해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                HashMap<String,Object> newUser = new HashMap<String,Object>();

                newUser.put("id",id);
                newUser.put("password",pw);
                newUser.put("nickname",nickname);
                newUser.put("city",myCity);

                database.collection(DB_USER_TABLE).document(id).set(newUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getApplicationContext(), "회원가입을 성공했습니다.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "회원가입에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });

                if (!id.isEmpty() && !nickname.isEmpty() && !pw.isEmpty() && !pwchk.isEmpty()) {
                    if (pw.compareTo(pwchk)==0) {
                        finish();
                    }
                }
            }
        });
    }
}
