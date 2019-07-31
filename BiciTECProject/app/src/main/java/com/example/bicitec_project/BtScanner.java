package com.example.bicitec_project;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class BtScanner extends AppCompatActivity {
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mLEScanner;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    private ScanSettings settings;
    private boolean mScanning;
    private Handler mHandler;
    private int mStatus;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private int permissionCheck;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    /*----------- Adress and Feather Name ------------------------*/
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_STATION = "STATION";
    private String mDeviceAddress;
    private String mDeviceName;
    private int estacionSalida;
    /*------------------------------------------------------------*/
    private Button btnAdress;
    private Button btnAccept;
    private Button btnCancel;
    private RelativeLayout layoutCofirm;
    private RelativeLayout layoutLoading;
    /*-- Boolean to verificate if already came to this activity --*/
    //private boolean verify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_scanner);

        mHandler = new Handler();
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        estacionSalida = intent.getIntExtra(EXTRAS_STATION,0);

        layoutCofirm = (RelativeLayout) findViewById(R.id.finishView);
        layoutLoading = (RelativeLayout)findViewById(R.id.progressView);
        btnAdress = (Button)findViewById(R.id.button2);
        btnAdress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //scanLeDevice(true);

            }
        });
        btnAccept = (Button)findViewById(R.id.btnAccept);

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent loanConfirmed = new Intent(BtScanner.this,LoanConfirmed.class);
                loanConfirmed.putExtra(LoanConfirmed.EXTRAS_DEVICE_NAME, mDeviceName);
                loanConfirmed.putExtra(LoanConfirmed.EXTRAS_DEVICE_ADDRESS, btnAdress.getText());
                loanConfirmed.putExtra(LoanConfirmed.EXTRAS_STATION,estacionSalida);
                //mLEScanner.stopScan(mScanCallback);

                if (mScanning) {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mScanning = false;
                }
                startActivity(loanConfirmed);
                finish();
        }});

        btnCancel =  (Button)findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent home = new Intent(BtScanner.this,Home.class);
                finish();
                startActivity(home);
            }
        });
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Bt not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Build ScanSetting
        ScanSettings.Builder scanSetting = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .setReportDelay(5000);

        settings = scanSetting.build();
        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bt not supported",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
       /* if(verify){
            Intent logIn = new Intent(BtScanner.this,LogIn.class);
            startActivity(logIn);
        }*/
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
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }else {

            if (Build.VERSION.SDK_INT >= 21) {
                mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                        .build();
                filters = new ArrayList<ScanFilter>();
            }

            if (Build.VERSION.SDK_INT >= 23) {
                checkLocationPermission();
                //requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
                Log.d("Abel", ">= 23");
            }
        }

        // Initializes list view adapter.
        /*mLeDeviceListAdapter = new LeDeviceListAdapter();
        setListAdapter(mLeDeviceListAdapter);*/
        scanLeDevice(true);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    //mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    if (Build.VERSION.SDK_INT < 21) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    } else {
                        //mBluetoothAdapter.stopScan(mScanCallback);

                    }
                }
            }, SCAN_PERIOD);

            mScanning = true;
            //mBluetoothAdapter.startLeScan(mLeScanCallback);
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                mLEScanner.startScan(filters, settings, mScanCallback);
            }
        } else {
            mScanning = false;
            //mBluetoothAdapter.stopLeScan(mLeScanCallback);
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else {
                mLEScanner.stopScan(mScanCallback);
            }
        }
    }
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("callbackType", String.valueOf(callbackType));
            Log.i("Abel - result", result.toString());
            final BluetoothDevice btDevice = result.getDevice();
            String device = result.toString();
            if (btDevice != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(btDevice.getAddress().equals(mDeviceAddress)){
                            btnAdress.setText(btDevice.getAddress());
                            layoutLoading.setVisibility(View.INVISIBLE);

                            layoutCofirm.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("Abe -ScanResult-Results", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Abel - Scan Failed", "Error Code: " + errorCode);
        }
    };

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });

                }
            };

    @Override
    protected void onPause() {
        super.onPause();
        //mHandler.removeCallbacksAndMessages(null);

    }
    @Override
    public void onBackPressed() {

    }
}
