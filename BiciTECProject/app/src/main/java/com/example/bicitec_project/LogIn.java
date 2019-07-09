package com.example.bicitec_project;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bicitec_project.Classes.LogInResponse;
import com.example.bicitec_project.Classes.TimeResponse;
import com.example.bicitec_project.api.Api;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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

    private static Api api; //This is the call to the api
    private static String base_url = "http://tecdigital.tec.ac.cr:8082/tds-tdapi/api/";

    //Pop ups
    private Dialog incorrectUsrPopUp;
    private Dialog forgotPasswPopUp;

    // User
    private static User us;
    private String userLoanDate;

    private String mDeviceAddress;

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
                        verifyUserLoan(us.getUserName());

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

    private boolean verifyUserLoan(final String usr){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myUserRef = database.getReference("User").child(usr).child("PrestamoActivo");
        myUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String loanValue = (String)dataSnapshot.getValue();
                if(loanValue == null || loanValue.equals("")){
                    Intent qrIntent = new Intent(LogIn.this,QrScanner.class);
                    qrIntent.putExtra("userId",usr);
                    qrIntent.putExtra(LoanConfirmed.EXTRAS_DEVICE_NAME, "asd");
                    qrIntent.putExtra(LoanConfirmed.EXTRAS_DEVICE_ADDRESS,"asd");
                    startActivity(qrIntent);
                }
                else{
                    LoanConfirmed.setLoanKey(loanValue);
                    HoursDifference hoursDifference = new HoursDifference();
                    String[] params = new String[2];
                    params[0] = loanValue;
                    params[1] = usr;
                    hoursDifference.execute(params);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        return true;
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

    public static Api getApi() {
        return api;
    }

    private class UserLoanHour extends AsyncTask<String, Integer, Integer>{

        @Override
        protected Integer doInBackground(String... voids) {
            Call<TimeResponse>call = api.time(voids[0]);
            String date = "";

            Response<TimeResponse> time = null;
            try {
                time = call.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(time.isSuccessful()){
                    TimeResponse times = time.body();
                    date = times.getServer_time();
                try {
                    Date dateUser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(voids[1]);
                    Date dateNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
                    long milliseconds = dateNow.getTime() - dateUser.getTime();
                    return (int)(long) milliseconds;
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                    //return "Hola";
                }
                //TimeResponse time = call.execute().body();
                //Date nowDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time.getServer_time());
                //return nowDate;


            return 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if(integer != 0){
                int seconds = (int) (integer / 1000) % 60;
                int minutes = (int) ((integer / (1000 * 60)) % 60);
                int hours = (int) ((integer / (1000 * 60 * 60)) % 24);
                if(hours == 0){
                    Intent activeLoan = new Intent(LogIn.this,ActiveLoan.class);
                    activeLoan.putExtra("LongTime",(long)integer);
                    activeLoan.putExtra(LoanConfirmed.EXTRAS_DEVICE_NAME, "asd");
                    activeLoan.putExtra(LoanConfirmed.EXTRAS_DEVICE_ADDRESS,mDeviceAddress);
                    startActivity(activeLoan);
                }
            }
        }
    }

    private class HoursDifference extends AsyncTask<String, Integer, Void>{
        @Override
        protected Void doInBackground(final String... voids) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference myUserRef = database.getReference("Historial").child(voids[0]);

            myUserRef.child("horaInicio").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String nowDate = (String) dataSnapshot.getValue();//new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse((String) dataSnapshot.getValue());
                        final String[] params = new String[2];
                        params[0] = "2017103300";
                        params[1] = nowDate;

                        myUserRef.child("adressFeather").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                mDeviceAddress = (String) dataSnapshot.getValue();
                                UserLoanHour userLoanHour = new UserLoanHour();
                                userLoanHour.execute(params);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        //Date dateUser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(userLoanDate);
                       /* long milliseconds = nowDate.getTime() - date.getTime();
                        int seconds = (int) (milliseconds / 1000) % 60;
                        int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
                        int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);*/
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
}
