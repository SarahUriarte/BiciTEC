package com.example.bicitec_project;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.bicitec_project.Classes.Bicycle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;

import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
public class QrScanner extends AppCompatActivity implements ZXingScannerView.ResultHandler{

    private static final int REQUEST_CAMERA = 1;
    private ZXingScannerView scannerView;
    private FirebaseAuth mAuth;

    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private ScanSettings settings;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private int permissionCheck;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    //private DeviceScanActivity.LeDeviceListAdapter mLeDeviceListAdapter;

    private BluetoothLeScanner mLEScanner;
    private List<ScanFilter> filters;

    private Dialog intructionsPopUp;
    private Dialog readingErrorPopUp;

    // Database
    DatabaseReference myRef;
    ValueEventListener postListener;

    //Timer
    private CountDownTimer timer;
    long loanTime = 30000;
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        /*************************************************/
        intructionsPopUp = new Dialog(this);
        readingErrorPopUp = new Dialog(this);
        showInstructionsPopUp();
        //mHandler = new Handler();
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        /************************************************/

        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);
        int currentApiVersion = Build.VERSION.SDK_INT;

        if (currentApiVersion >= Build.VERSION_CODES.M) {
            if (checkPermission()) {
                //Toast.makeText(getApplicationContext(), "Permission already granted!", Toast.LENGTH_LONG).show();
            } else {
                requestPermission();
            }
        }else{

        }

        startTimer();

    }

    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(QrScanner.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
    }
    public void checkLocationPermission() {
        permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);

        switch (permissionCheck) {
            case PackageManager.PERMISSION_GRANTED:
                break;

            case PackageManager.PERMISSION_DENIED:

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    //Show an explanation to user *asynchronouselly* -- don't block
                    //this thread waiting for the user's response! After user sees the explanation, try again to request the permission

                    Log.d("Abel", "Location access is required to show Bluetooth devices nearby.");

                } else {
                    //No explanation needed, we can request the permission
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                            PERMISSION_REQUEST_COARSE_LOCATION);
                }
                break;
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        // Initializes list view adapter.
        //mLeDeviceListAdapter = new DeviceScanActivity.LeDeviceListAdapter();
        //setListAdapter(mLeDeviceListAdapter);
        //scanLeDevice(true);
    }
    public void showInstructionsPopUp(){
        Button accept;
        intructionsPopUp.setContentView(R.layout.qr_instruction_pop_up);

        accept = (Button) intructionsPopUp.findViewById(R.id.btnAccept);

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intructionsPopUp.dismiss();
                int currentapiVersion = android.os.Build.VERSION.SDK_INT;
                if (currentapiVersion >= android.os.Build.VERSION_CODES.M) {
                    if (checkPermission()) {
                        if (scannerView == null) {
                            scannerView = new ZXingScannerView(QrScanner.this);
                            setContentView(scannerView);
                        }
                        scannerView.setResultHandler(QrScanner.this);
                        scannerView.startCamera();
                    } else {
                        requestPermission();
                    }
                }
                else {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_DENIED){
                        requestPermission();
                    }
                    else{
                        if (scannerView == null) {
                            scannerView = new ZXingScannerView(QrScanner.this);
                            setContentView(scannerView);
                        }
                        scannerView.setResultHandler(QrScanner.this);
                        scannerView.startCamera();
                    }
                }
                if (!mBluetoothAdapter.isEnabled()) {
                    if (!mBluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    }
                }else {
                    if (Build.VERSION.SDK_INT >= 21) {
                        checkLocationPermission();
                        //requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
                        Log.d("Abel", ">= 23");
                    }
                }
            }
        });
        intructionsPopUp.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scannerView.stopCamera();
        if(postListener != null){
            myRef.removeEventListener(postListener);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0) {

                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted) {
                        Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access camera", Toast.LENGTH_LONG).show();
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
        new android.support.v7.app.AlertDialog.Builder(QrScanner.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void handleResult(Result result) {
        scannerView.stopCamera();
        stopTimer();
        final String myResult = result.getText();
        Log.d("QRCodeScanner", result.getText());
        Log.d("QRCodeScanner", result.getBarcodeFormat().toString());

        //Toast.makeText(getApplicationContext(), myResult, Toast.LENGTH_LONG).show();
        scannerView.resumeCameraPreview(QrScanner.this);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Bicycle");

        postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean direccionEncontrada = false;
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    Bicycle bici = postSnapshot.getValue(Bicycle.class);
                    if (bici.getAdress().equals(myResult)) {
                        if(bici.getState().equals("available")){
                            String deviceName = "Adafruit Bluefruit LE";
                            Intent loanConfirmed = new Intent(QrScanner.this,BtScanner.class);
                            loanConfirmed.putExtra(BtScanner.EXTRAS_DEVICE_NAME, deviceName);
                            loanConfirmed.putExtra(BtScanner.EXTRAS_DEVICE_ADDRESS,bici.getAdress());
                            loanConfirmed.putExtra(BtScanner.EXTRAS_STATION, bici.getStation());
                            finish();
                            startActivity(loanConfirmed);
                            direccionEncontrada = true;
                            break;
                        }else {
                            Toast.makeText(getApplicationContext(),
                                    "Esta bicicleta no está disponible",
                                    Toast.LENGTH_SHORT).show();
                            scannerView.startCamera();
                            loanTime = 30000;
                            startTimer();
                        }

                    }
                }
                if(!direccionEncontrada){
                    if(!isFinishing()){
                        Toast.makeText(getApplicationContext(),
                                "Este código no coincide con ninguna bicicleta",
                                Toast.LENGTH_SHORT).show();
                        scannerView.startCamera();
                        loanTime = 30000;
                        startTimer();
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
                loanTime = 30000;
                startTimer();
            }
        });
        digitCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent writeCode = new Intent(QrScanner.this,WriteCode.class);
                timer.cancel();
                readingErrorPopUp.cancel();
                finish();
                Log.d("Qr", "Estoy en el dimiss");
                startActivity(writeCode);
            }
        });
        readingErrorPopUp.show();
    }

    @Override
    public void onBackPressed() {
    }

    public void startTimer() {
        timer = new CountDownTimer(loanTime, 1000) {
            @Override
            public void onTick(long l) {
                loanTime = l;
                updateTimer();
            }

            @Override
            public void onFinish() {

            }
        };
        timer.start();
    }

    public void stopTimer() {
        timer.cancel();
    }

    public void updateTimer() {
        int seconds = (int) loanTime % 60000 / 1000;
        //Toast.makeText(getApplicationContext(),Integer.toString(seconds),Toast.LENGTH_SHORT).show();
        if (seconds == 1) {
            //stopTimer();
            //Toast.makeText(getApplicationContext(), "Time over", Toast.LENGTH_SHORT).show();
            //Intent prestamoActivo = new Intent(PrestamoActivo.this,PrestamoActivo5Min.class);
            //startActivity(prestamoActivo);
            if(!isFinishing()){
                showErrorReadingPopUp();
            }

        }
    }
}
