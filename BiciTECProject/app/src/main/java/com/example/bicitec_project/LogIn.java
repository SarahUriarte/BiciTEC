package com.example.bicitec_project;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bicitec_project.Classes.LogInResponse;
import com.example.bicitec_project.api.Api;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LogIn extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView txtUser;
    private TextView txtPassword;
    private TextView txtRegister;
    private Button btnEnter;

    private Api api; //This is the call to the api
    private String base_url = "http://tecdigital.tec.ac.cr:8082/tds-tdapi/api/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        mAuth = FirebaseAuth.getInstance();
        txtUser = (TextView) findViewById(R.id.txtUser);
        txtPassword = (TextView)findViewById(R.id.txtPassword);
        btnEnter = (Button)findViewById(R.id.btnEntrar);
        txtRegister = (TextView)findViewById(R.id.txtRegister);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(base_url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(Api.class);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usr = txtUser.getText().toString();
                String pasw = txtPassword.getText().toString();
                existingUser(usr,pasw);
            }
        });
        /*txtRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signIn = new Intent(LogIn.this,SignIn.class);
                startActivity(signIn);
            }
        });*/
    }
    private void existingUser(String userName, String password){
        Call<LogInResponse> call = api.authentication(userName,password);
        call.enqueue(new Callback<LogInResponse>() {
            @Override
            public void onResponse(Call<LogInResponse> call, Response<LogInResponse> response) {
                if(response.code() == 200){
                    LogInResponse loginData = response.body();
                    if(loginData.getAuthentication().equals("ok")){
                        Intent qrIntent = new Intent(LogIn.this,QrScanner.class);
                        qrIntent.putExtra("userId",loginData.getUser_id());
                        startActivity(qrIntent);
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(), "Usuario o contraseña incorrecta",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LogInResponse> call, Throwable t) {

            }
        });
    }

}
