package com.example.bicitec_project;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.bicitec_project.Classes.LogInResponse;
import com.example.bicitec_project.Classes.Record;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LoanConfirmed extends AppCompatActivity {
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    /*UI Elements*/
    private Button btnAccept;
    private Button btnCancelar;

    private RelativeLayout layoutCofirm;
    private RelativeLayout layoutLoading;
    /*End of the IU Elements*/
    /*Control variables*/
    private int solicitarBici;

    private String mDeviceAddress;
    private String mDeviceName;
    private static BluetoothLeService mBluetoothLeService;
    private BluetoothGatt bluetoothGatt;
    private boolean mConnected = false;
    private final static String TAG = LoanConfirmed.class.getSimpleName();
    /*    ------- variables for the scanner -------  */
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

    /*    ------- end for the scanner -------  */
    /*  ---------- Variables for the database ------------------*/
    private DatabaseReference mDatabase;

    /*----------------------------------------------------------*/
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.w("Sarah","reading bluethoot");
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            if(mBluetoothLeService.connect(mDeviceAddress)){
                Log.d("Sarah K", "Se conecto bien");
            }else{
                Log.d("Sarah K", "No se conecto");
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {

                mConnected = true;
                /*int a = BluetoothLeService.cont2;
                layoutCofirm = (RelativeLayout) findViewById(R.id.finishView);
                layoutLoading = (RelativeLayout) findViewById(R.id.progressView);
                layoutCofirm.setVisibility(View.VISIBLE);
                layoutLoading.setVisibility(View.INVISIBLE);
                Log.d("BroadcastReceiver", "Esta conectado");*/
                mBluetoothLeService.enableTXNotification();
                while (true){
                    boolean tryWrite = false;
                    byte[] value = new byte[]{79, 112, 101, 110, 71, 97, 83, 69, 83, 76, 97, 98, 33};
                    tryWrite = mBluetoothLeService.writeRXCharacteristic(value);
                    if(tryWrite){
                        break;
                    }
                }
                mBluetoothLeService.enableTXNotification();
                int cont = 0;
                while (BluetoothLeService.cont2 == 0) {
                    Log.d("Abel","cont2 = "+BluetoothLeService.cont2);
                    mBluetoothLeService.enableTXNotification();
                    //Log.d("Abel","response_3 not received");
                    cont++;
                    if(cont == 1000){
                        break;
                    }
                }
                int a = BluetoothLeService.cont2;
                if(BluetoothLeService.cont2 != 0){
                    Intent activeLoan = new Intent(LoanConfirmed.this,ActiveLoan.class);
                    crearInstanciaFirebase();
                    activeLoan.putExtra(ActiveLoan.EXTRAS_DEVICE_NAME, mDeviceName);
                    activeLoan.putExtra(ActiveLoan.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
                    startActivity(activeLoan);
                }
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                Log.d("BroadcastReceiver", "No esta conectado");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.

                /**** ABEL ****/
                //Paso 2.2 Conectarse a la device address del Adafruit Bluefruit LE (Se acaba de hacer aquí)
                // y esperar la confirmación (Se debe realizar un ciclo para esperar la respuesta)

                /*while(BluetoothLeService.cont == 0){
                    Log.d("Abel","cont = "+BluetoothLeService.cont);

                    //onCharacteristicChanged debe cambiar a 1 cont cuando se lea la respuesta
                    mBluetoothLeService.enableTXNotification();
                    //Log.d("Abel","response 2.2 not received");
                }
                if(BluetoothLeService.cont == 1){
                    Log.d("Abel","cont = "+BluetoothLeService.cont);

                    //onCharacteristicChanged debe cambiar a 1 cont cuando se lea la respuesta
                    mBluetoothLeService.enableTXNotification();
                    //Log.d("Abel","response 2.2 not received");
                    layoutCofirm = (RelativeLayout) findViewById(R.id.finishView);
                    layoutLoading = (RelativeLayout) findViewById(R.id.progressView);
                    layoutCofirm.setVisibility(View.VISIBLE);
                    layoutLoading.setVisibility(View.INVISIBLE);
                }*/
                if(BluetoothLeService.cont2 == 1){
                    Log.d("Sarah","Intentó conectar");

                }
                /***ABEL END */
            }
        }
    };
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    //Método para insertar en firebase
    public void crearInstanciaFirebase(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Historial");
        DatabaseReference myBicycleRef = database.getReference("Bicycle").child(mDeviceAddress).child("state");
        Record record = new Record(LogIn.getUs().getUserName(),"a","a","a",mDeviceAddress,"En curso");
        String key = myRef.push().getKey();
        myRef.child(key).setValue(record);
        //myBicycleRef.setValue("not available");
    }

    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_confirmed);
        /*if(LoanFinished.isVerify()){
            Intent login = new Intent(LoanConfirmed.this, LogIn.class);
            //unbindService(mServiceConnection);
            //mBluetoothLeService = null;
            //unregisterReceiver(mGattUpdateReceiver);
            BtScanner.getmHandler();
            BtScanner.setmHandler(null);
            //mHandler.removeCallbacks(Thread.currentThread());
            //mBluetoothLeService.disconnect();
            startActivity(login);

        }
        /*******************************************************************/
        //mHandler = new Handler();
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        /*if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Ble not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        /*final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        // Build ScanSetting
        ScanSettings.Builder scanSetting = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .setReportDelay(5000);

        settings = scanSetting.build();*/

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        btnAccept = (Button)findViewById(R.id.btnAccept);

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Sarah1", "cont "+BluetoothLeService.cont);
                mBluetoothLeService.enableTXNotification();
                while (true){
                    boolean tryWrite = false;
                    byte[] value = new byte[]{79, 112, 101, 110, 71, 97, 83, 69, 83, 76, 97, 98, 33};
                    tryWrite = mBluetoothLeService.writeRXCharacteristic(value);
                    if(tryWrite){
                        break;
                    }
                }
                /*if(BluetoothLeService.cont == 1){
                    Log.d("Abel","cont = "+BluetoothLeService.cont +" response 2.2 received");
                    //Toast.makeText(getApplicationContext(),"Conectado",Toast.LENGTH_SHORT).show();
                    while (true){
                        boolean tryWrite = false;
                        byte[] value = new byte[]{79, 112, 101, 110, 71, 97, 83, 69, 83, 76, 97, 98, 33};
                        tryWrite = mBluetoothLeService.writeRXCharacteristic(value);
                        if(tryWrite){
                            break;
                        }
                    }
                    //crearInstanciaFirebase("En curso");
                }*/
                while (BluetoothLeService.cont2 == 0) {
                    Log.d("Abel","cont2 = "+BluetoothLeService.cont2);
                    mBluetoothLeService.enableTXNotification();
                    //Log.d("Abel","response_3 not received");
                }
                int a = BluetoothLeService.cont2;
                if(BluetoothLeService.cont2 == 1){
                    Intent activeLoan = new Intent(LoanConfirmed.this,ActiveLoan.class);
                    crearInstanciaFirebase();
                    activeLoan.putExtra(ActiveLoan.EXTRAS_DEVICE_NAME, mDeviceName);
                    activeLoan.putExtra(ActiveLoan.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
                    startActivity(activeLoan);
                }
            }
        });

        mDatabase = FirebaseDatabase.getInstance().getReference();

    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        /*if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }else {

            if (Build.VERSION.SDK_INT >= 21) {
                mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .build();
                filters = new ArrayList<ScanFilter>();
            }

            if (Build.VERSION.SDK_INT >= 23) {
                //checkLocationPermission();
                //requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
                Log.d("Abel", ">= 23");
            }
        }

        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }*/
        //scanLeDevice(true);
    }

    public static BluetoothLeService getBluethothLeService(){
        return mBluetoothLeService;
    }

    public static void setmBluetoothLeService(BluetoothLeService mBluetoothLeService) {
        LoanConfirmed.mBluetoothLeService = mBluetoothLeService;
    }

    /* ------------------------- Code fot the scanner ----------------------------*/
    //@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    /*private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                @Override
                public void run() {
                    mScanning = false;
                    //mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    if (Build.VERSION.SDK_INT < 21) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    } else {
                        //mBluetoothAdapter.stopScan(mScanCallback);

                    }
                    invalidateOptionsMenu();
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
                //mLEScanner.stopScan(mScanCallback);
            }
        }
        invalidateOptionsMenu();
    }*/
    /*private ScanCallback mScanCallback = new ScanCallback() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("callbackType", String.valueOf(callbackType));
            Log.i("Abel - result", result.toString());
            final BluetoothDevice btDevice = result.getDevice();
            String device = result.toString();
            if (btDevice != null) {
                if (device.contains("Adafruit Bluefruit LE")) {
                    //connectToDevice(btDevice);
                    device = "Adafruit Bluefruit LE";
                    Log.d("Ada", device);
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    /*mLeDeviceListAdapter.addDevice(btDevice);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }*/

       /* @Override
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
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.d("Sarah","Device"+device);
        }
    };*/
       @Override
       protected void onPause() {
           super.onPause();
           unregisterReceiver(mGattUpdateReceiver);
       }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }
}
