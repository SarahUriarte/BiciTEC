package com.example.bicitec_project;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.bicitec_project.Classes.Station;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QrScannerFinish extends AppCompatActivity implements ZXingScannerView.ResultHandler{
    private static final int REQUEST_CAMERA = 1;
    private ZXingScannerView scannerView;
    private FirebaseAuth mAuth;
    private static String mDeviceAddress;
    private String mDeviceName;
    Dialog readingErrorPopUp;
    private static boolean verifyActivity = false;

    //For firebase
    FirebaseDatabase database;
    DatabaseReference myStationRef;
    ValueEventListener postListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner_finish);
        mAuth = FirebaseAuth.getInstance();
        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra("DeviceName");
        mDeviceAddress = intent.getStringExtra("DeviceAdress");
        database = FirebaseDatabase.getInstance();
        readingErrorPopUp = new Dialog(this);
        int currentApiVersion = Build.VERSION.SDK_INT;

        if (currentApiVersion >= Build.VERSION_CODES.M) {
            if (checkPermission()) {
                Toast.makeText(getApplicationContext(), "Permission already granted!", Toast.LENGTH_LONG).show();
            } else {
                requestPermission();
            }
        }
    }
    @Override
    public void onResume() {
        super.onResume();

        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.M) {
            if (checkPermission()) {
                if (scannerView == null) {
                    scannerView = new ZXingScannerView(this);
                    setContentView(scannerView);
                }
                scannerView.setResultHandler(this);
                scannerView.startCamera();
            } else {
                requestPermission();
            }
        } else {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_DENIED) {
                requestPermission();
            } else {
                if (scannerView == null) {
                    scannerView = new ZXingScannerView(this);
                    setContentView(scannerView);
                }
                scannerView.setResultHandler(this);
                scannerView.startCamera();
            }
        }
    }
    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(QrScannerFinish.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0) {

                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted) {
                        //Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access camera", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access and camera", Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{Manifest.permission.CAMERA},
                                                            REQUEST_CAMERA);
                                                }
                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(QrScannerFinish.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
    @Override
    public void handleResult(Result result) {
        scannerView.stopCamera();
        final String myResult = result.getText();
        Log.d("QRCodeScanner", result.getText());
        Log.d("QRCodeScanner", result.getBarcodeFormat().toString());

        //Toast.makeText(getApplicationContext(), myResult, Toast.LENGTH_LONG).show();
        scannerView.resumeCameraPreview(QrScannerFinish.this);
        myStationRef = database.getReference("Station");
        postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean find = false;
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                    if(postSnapshot.getKey().equals(myResult)){
                        Intent loanFinished = new Intent(QrScannerFinish.this,LoanFinished.class);
                        loanFinished.putExtra("DeviceAdress",mDeviceAddress);
                        loanFinished.putExtra(LoanFinished.EXTRAS_FINISH_STATION, myResult);
                        startActivity(loanFinished);
                        find = true;
                        finish();
                        myStationRef.removeEventListener(postListener);
                    }
                }
                if(!isFinishing() && !find){
                    Toast.makeText(getApplicationContext(),
                            "El código que está escaneando no coincide con el de ninguna estación",
                            Toast.LENGTH_SHORT).show();
                            scannerView.startCamera();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Falta código para todos los onCancelled
            }
        };
        myStationRef.addValueEventListener(postListener);

    }
    public void showErrorReadingPopUp(){
        Button retry;
        Button digitCode;
        readingErrorPopUp.setContentView(R.layout.reading_error_pop_up);
        retry = (Button) readingErrorPopUp.findViewById(R.id.btnreTry);
        digitCode = (Button)readingErrorPopUp.findViewById(R.id.btnDigit);
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readingErrorPopUp.dismiss();
                scannerView.startCamera();
            }
        });
        digitCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyActivity = true;
                Intent writeCode = new Intent(QrScannerFinish.this,WriteCode.class);
                startActivity(writeCode);
            }
        });
        readingErrorPopUp.show();
    }

    public static boolean isVerifyActivity() {
        return verifyActivity;
    }

    public static String getmDeviceAddress() {
        return mDeviceAddress;
    }

    @Override
    public void onBackPressed() {

    }
}
