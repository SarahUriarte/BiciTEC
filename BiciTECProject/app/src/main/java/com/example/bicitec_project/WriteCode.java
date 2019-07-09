package com.example.bicitec_project;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class WriteCode extends AppCompatActivity {
    private EditText textCode;
    private Button btnAccept;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_code);
        textCode = (EditText)findViewById(R.id.txtCode);
        btnAccept = (Button)findViewById(R.id.btnGo);
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!QrScannerFinish.isVerifyActivity()){
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("Bicycle");
                    ValueEventListener postListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            boolean direccionEncontrada = false;
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                                Bicycle bici = postSnapshot.getValue(Bicycle.class);

                                if (bici.getAdress().equals(textCode.getText().toString()) && bici.getState().equals("available")) {
                                    String deviceName = "Adafruit Bluefruit LE";
                                    Intent loanConfirmed = new Intent(WriteCode.this,BtScanner.class);
                                    loanConfirmed.putExtra(BtScanner.EXTRAS_DEVICE_NAME, deviceName);
                                    loanConfirmed.putExtra(BtScanner.EXTRAS_DEVICE_ADDRESS,bici.getAdress());
                                    finish();
                                    startActivity(loanConfirmed);
                                    direccionEncontrada = true;
                                    break;
                                }
                            }
                            if(!direccionEncontrada){
                                if(!isFinishing()){
                                    //showErrorReadingPopUp();
                                }
                                //
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    };
                    myRef.addValueEventListener(postListener);
                }
                else{
                    if(QrScannerFinish.getmDeviceAddress().equals(textCode.getText().toString())){
                        Intent loanFinished = new Intent(WriteCode.this,LoanFinished.class);
                        loanFinished.putExtra("DeviceAdress",QrScannerFinish.getmDeviceAddress());
                        startActivity(loanFinished);
                    }
                }

            }
        });
    }
}
