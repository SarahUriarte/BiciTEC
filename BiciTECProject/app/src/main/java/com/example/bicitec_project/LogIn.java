package com.example.bicitec_project;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
    private Button forgotCredential;

    private Api api; //This is the call to the api
    private String base_url = "http://tecdigital.tec.ac.cr:8082/tds-tdapi/api/";

    //Pop ups
    private Dialog incorrectUsrPopUp;
    private Dialog forgotPasswPopUp;

    // User
    private static User us;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        mAuth = FirebaseAuth.getInstance();



        txtUser = (TextView) findViewById(R.id.txtUser);
        txtPassword = (TextView)findViewById(R.id.txtPassword);
        btnEnter = (Button)findViewById(R.id.btnEntrar);
        txtRegister = (TextView)findViewById(R.id.txtRegister);
        forgotCredential = (Button) findViewById(R.id.btnForgotPassw);
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
        forgotCredential.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showforgotPawwPopUp();
            }
        });
        /*txtRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signIn = new Intent(LogIn.this,SignIn.class);
                startActivity(signIn);
            }
        });*/
        incorrectUsrPopUp = new Dialog(this);
        forgotPasswPopUp = new Dialog(this);
    }
    private void existingUser(final String userName, String password){
        Call<LogInResponse> call = api.authentication(userName,password);
        call.enqueue(new Callback<LogInResponse>() {
            @Override
            public void onResponse(Call<LogInResponse> call, Response<LogInResponse> response) {
                if(response.code() == 200){
                    LogInResponse loginData = response.body();
                    if(loginData.getAuthentication().equals("ok")){
                        us = new User(userName);
                        Intent qrIntent = new Intent(LogIn.this,QrScanner.class);
                        qrIntent.putExtra("userId",loginData.getUser_id());
                        startActivity(qrIntent);
                    }
                }
                else{
                    //Toast.makeText(getApplicationContext(), "Usuario o contraseña incorrecta",Toast.LENGTH_SHORT).show();
                    showIncorrectUserPopUp();
                }
            }

            @Override
            public void onFailure(Call<LogInResponse> call, Throwable t) {

            }
        });
    }

    //Pop up incorrect user
    public void showIncorrectUserPopUp(){
        Button accept;
        incorrectUsrPopUp.setContentView(R.layout.incorrect_user_pop_up);

        accept = (Button) incorrectUsrPopUp.findViewById(R.id.btnAccept);

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incorrectUsrPopUp.dismiss();
            }
        });
        incorrectUsrPopUp.show();
    }

    public void showforgotPawwPopUp(){
        Button goToTec;
        Button back;
        final String tecDigitalURL = "https://tecdigital.tec.ac.cr/register/?return_url=%2fdotlrn%2findex#/";
        forgotPasswPopUp.setContentView(R.layout.tec_digital_pop_up);

        back = (Button) forgotPasswPopUp.findViewById(R.id.btnBack);
        goToTec = (Button)forgotPasswPopUp.findViewById(R.id.btnGotoTec);
        goToTec.setText(Html.fromHtml(getString(R.string.ir_tec_digital)));
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgotPasswPopUp.dismiss();
            }
        });
        goToTec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebURL(tecDigitalURL);
            }
        });
        forgotPasswPopUp.show();
    }
    public void openWebURL( String inURL ) {
        Intent browse = new Intent( Intent.ACTION_VIEW , Uri.parse( inURL ) );

        startActivity( browse );
    }

    public static User getUs() {
        return us;
    }
}
