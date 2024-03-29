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

                //updateConnectionState(R.string.connected);

                //Toast.makeText(getApplicationContext(),"Conectado",Toast.LENGTH_SHORT).show();
                //invalidateOptionsMenu();
                Log.d("BroadcastReceiver", "Esta conectado");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                //updateConnectionState(R.string.disconnected);
                //Toast.makeText(getApplicationContext(),"Desconectado",Toast.LENGTH_SHORT).show();
                //invalidateOptionsMenu();
                //clearUI();
                Log.d("BroadcastReceiver", "No esta conectado");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.

                /**** ABEL ****/
                //Paso 2.2 Conectarse a la device address del Adafruit Bluefruit LE (Se acaba de hacer aquí)
                // y esperar la confirmación (Se debe realizar un ciclo para esperar la respuesta)

                /*(BluetoothLeService.cont == 0){
                    Log.d("Abel","cont = "+BluetoothLeService.cont);

                    //onCharacteristicChanged debe cambiar a 1 cont cuando se lea la respuesta
                    mBluetoothLeService.enableTXNotification();
                    //Log.d("Abel","response 2.2 not received");
                }*/
                if(BluetoothLeService.cont == 1){
                    Log.d("Abel","cont = "+BluetoothLeService.cont);

                    //onCharacteristicChanged debe cambiar a 1 cont cuando se lea la respuesta
                    /*mBluetoothLeService.enableTXNotification();
                    //Log.d("Abel","response 2.2 not received");
                    layoutCofirm = (RelativeLayout) findViewById(R.id.finishView);
                    layoutLoading = (RelativeLayout) findViewById(R.id.progressView);
                    layoutCofirm.setVisibility(View.VISIBLE);
                    layoutLoading.setVisibility(View.INVISIBLE);*/
                }
                /*while(BluetoothLeService.cont2 == 0){
                    Log.d("Sarah","cont2 = "+BluetoothLeService.cont2);

                    //onCharacteristicChanged debe cambiar a 1 cont cuando se lea la respuesta
                    mBluetoothLeService.enableTXNotification();
                    //Log.d("Abel","response 2.2 not received");
                }

                //Paso 3. Enviar la señal de abrir candado al Adafruit Bluefruit LE y esperar confirmación
                /*while (BluetoothLeService.cont2 != 1) {
                    Log.d("Abel","cont2 = "+BluetoothLeService.cont2);
                    boolean tryWrite = false;
                    while (!tryWrite) {
                        byte[] value = new byte[]{79, 112, 101, 110, 71, 97, 83, 69, 83, 76, 97, 98, 33};
                        tryWrite = mBluetoothLeService.writeRXCharacteristic(value);
                    }
                    mBluetoothLeService.enableTXNotification();
                    //Log.d("Abel","response_3 not received");
                }*/
                if(BluetoothLeService.cont2 == 1){
                    Log.d("Sarah","Intentó conectar");
                    //btnConfirmado = (Button) findViewById(R.id.confirmadoBtn);
                    //btnConfirmado.setVisibility(View.VISIBLE);
                    //crearInstanciaFirebase("Prestada");
                    //Intent activeLoan = new Intent(LoanConfirmed.this,ActiveLoan.class);
                    //startActivity(activeLoan);

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
    public void crearInstanciaFirebase(String procedimiento){
        /*Historial historial = new Historial();
        historial.setDate(getDateTime());
        historial.setAdressFeather(mDeviceAddress);
        historial.setProcedimiento(procedimiento);
        String fecha = getDateTime();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Historial");
        myRef.child(String.valueOf(mDeviceAddress+" "+fecha)).setValue(historial);*/

    }

    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_confirmed);

        /*******************************************************************/

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
                while (true){
                    boolean tryWrite = false;
                    byte[] value = new byte[]{79, 112, 101, 110, 71, 97, 83, 69, 83, 76, 97, 98, 33};
                    tryWrite = mBluetoothLeService.writeRXCharacteristic(value);
                    if(tryWrite){
                        break;
                    }
                }
                while (BluetoothLeService.cont2 == 0) {
                    Log.d("Abel","cont2 = "+BluetoothLeService.cont2);
                    mBluetoothLeService.enableTXNotification();
                    //Log.d("Abel","response_3 not received");
                }
                if(BluetoothLeService.cont2 == 1){
                    Intent activeLoan = new Intent(LoanConfirmed.this,ActiveLoan.class);
                    activeLoan.putExtra(ActiveLoan.EXTRAS_DEVICE_NAME, mDeviceName);
                    activeLoan.putExtra(ActiveLoan.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
                    startActivity(activeLoan);
                }
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    public static BluetoothLeService getBluethothLeService(){
        return mBluetoothLeService;
    }
}
