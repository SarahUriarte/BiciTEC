package com.example.bicitec_project;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class LoanFinished extends AppCompatActivity{
    /*UI Elements*/
    private RelativeLayout finishView;
    private RelativeLayout finishViewReq;

    private static final int PERMISSION_READ_STATE_LOCATION = 1;
    private LocationManager locationManager;
    /*Services variables*/
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private String mDeviceAddress;
    private String mDeviceName;
    private BluetoothLeService mBluetoothLeService = LoanConfirmed.getBluethothLeService();
    private boolean mConnected = false;
    private Button but;
    private static boolean verify;
    private Button btnAcept;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();

            if (!mBluetoothLeService.initialize()) {
                Log.e("Sarah", "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            if (mBluetoothLeService.connect(mDeviceAddress)) {
                Log.d("SarahK", "Se conecto bien");
            } else {
                Log.d("SarahK", "No se conecto");
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                //updateConnectionState(R.string.connected);

                Toast.makeText(getApplicationContext(), "Conectado", Toast.LENGTH_SHORT).show();
                invalidateOptionsMenu();
                Log.d("BroadcastReceiver", "Esta conectado");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                //updateConnectionState(R.string.disconnected);
                Toast.makeText(getApplicationContext(), "Desconectado", Toast.LENGTH_SHORT).show();
                invalidateOptionsMenu();
                //clearUI();
                Log.d("BroadcastReceiver", "No esta conectado");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.

                /**** ABEL ****/
                //Paso 2.2 Conectarse a la device address del Adafruit Bluefruit LE (Se acaba de hacer aquí)
                // y esperar la confirmación (Se debe realizar un ciclo para esperar la respuesta)


                if (BluetoothLeService.cont3 == 1) {
                    Log.d("Sarah", "Intentó conectar");
                    //btnConfirmado = (Button) findViewById(R.id.confirmadoBtn);
                    //btnConfirmado.setVisibility(View.VISIBLE);
                    //saveLoanRequest("Prestada");
                    //Intent prestamoActivo = new Intent(DeviceControlActivity.this,PrestamoActivo.class);
                    //startActivity(prestamoActivo);

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_finished);
        final Intent intent = getIntent();
        mDeviceAddress = intent.getStringExtra("DeviceAdress");
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        finishView = (RelativeLayout)findViewById(R.id.finishView);
        but = (Button)findViewById(R.id.buttonFinish);
        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothLeService.enableTXNotification();
                while (true) {
                    boolean tryWrite = false;
                    byte[] value = new byte[]{67, 108, 111, 115, 101, 71, 97, 83, 69, 83, 76, 97, 98, 33};
                    tryWrite = mBluetoothLeService.writeRXCharacteristic(value);
                    if (tryWrite) {
                        break;
                    }
                }
                while (BluetoothLeService.cont3 == 0) {
                    Log.d("Abel", "cont2 = " + BluetoothLeService.cont2);
                    mBluetoothLeService.enableTXNotification();
                    //Log.d("Abel","response_3 not received");
                }
                if (BluetoothLeService.cont3 == 1) {
                    Toast.makeText(getApplicationContext(), "Devuelta", Toast.LENGTH_SHORT);
                    mBluetoothLeService.disconnect();
                    /*Intent prestamoFinalizado = new Intent(PrestamoActivo.this, PrestamoFinalizado.class);
                    startActivity(prestamoFinalizado);*/
                    finishViewReq.setVisibility(View.INVISIBLE);
                    finishView.setVisibility(View.VISIBLE);
                    finishLoanOnDB();
                }
            }
        });
        btnAcept = (Button)findViewById(R.id.btnAccept);
        btnAcept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent qrScanner = new Intent(LoanFinished.this,QrScanner.class);
                startActivity(qrScanner);
            }
        });
        finishViewReq = (RelativeLayout) findViewById(R.id.finishViewReq);
        //observable.subscribe(observer);

    }
    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }
    @Override
    protected void onResume() {
        super.onResume();
        //casa.start();
    }

    private void finishLoanRequest(){
        mBluetoothLeService.enableTXNotification();
        while (true) {
            boolean tryWrite = false;
            byte[] value = new byte[]{67, 108, 111, 115, 101, 71, 97, 83, 69, 83, 76, 97, 98, 33};
            tryWrite = mBluetoothLeService.writeRXCharacteristic(value);
            if (tryWrite) {
                break;
            }
        }
        //Could I put this instruction in a thread?
        while (BluetoothLeService.cont3 == 0) {
            Log.d("Abel", "cont2 = " + BluetoothLeService.cont2);
            mBluetoothLeService.enableTXNotification();
            //Log.d("Abel","response_3 not received");
        }
        


    }
    /*final Observable operationObservable = Observable.create(new Observable.OnSubscribe() {
        @Override
        public void call(Object o) {

        }
    });*/
    Observable observable = Observable.create(new ObservableOnSubscribe() {
        @Override
        public void subscribe(ObservableEmitter emitter){
            try {
                //Thread.sleep(2000);
                finishLoanRequest();
                emitter.onComplete();
            }catch (Exception e){
                emitter.onError(e);
            }
        }
    }).subscribeOn(Schedulers.io()) // subscribeOn the I/O thread
            .observeOn(AndroidSchedulers.mainThread()); // observeOn the UI Thread;

    Observer observer = new Observer() {
        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onNext(Object o) {

        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onComplete() {
            if (BluetoothLeService.cont3 == 1) {
                Toast.makeText(getApplicationContext(), "Devuelta", Toast.LENGTH_SHORT);
                mBluetoothLeService.disconnect();
            }
            finishViewReq.setVisibility(View.INVISIBLE);
            finishView.setVisibility(View.VISIBLE);
        }
    };

    private void finishLoanOnDB(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Historial").child(LoanConfirmed.getLoanKey());
        DatabaseReference myUserRef = database.getReference("User").child(LoanConfirmed.getUserLoanKey());
        DatabaseReference myBiciRef = database.getReference("Bicycle").child(mDeviceAddress);
        myRef.child("estado").setValue("Terminado");
        myUserRef.child("loanState").setValue("Terminado");
        myBiciRef.child("state").setValue("available");

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
