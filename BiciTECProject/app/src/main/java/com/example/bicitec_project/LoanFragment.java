package com.example.bicitec_project;


import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoanFragment extends Fragment {

    
    public LoanFragment() {
        // Required empty public constructor
    }
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
    private BluetoothLeService mBluetoothLeService;
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
                //finish();
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

                while(BluetoothLeService.cont == 0){
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
                    //layoutCofirm = (RelativeLayout) findViewById(R.id.secondView);
                    //layoutLoading = (RelativeLayout) findViewById(R.id.progressView);
                    layoutCofirm.setVisibility(View.VISIBLE);
                    layoutLoading.setVisibility(View.INVISIBLE);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_loan, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
