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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
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
import com.example.bicitec_project.Classes.ReleaseBicycleResponse;
import com.example.bicitec_project.Classes.TimeResponse;
import com.example.bicitec_project.Classes.TimerRequest;
import com.example.bicitec_project.Classes.UserLoan;
import com.example.bicitec_project.api.Api;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoanConfirmed extends AppCompatActivity {
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_STATION = "STATION";

    /*UI Elements*/
    private Button btnAccept;
    private Button btnCancelar;

    private RelativeLayout layoutCofirm;
    private RelativeLayout layoutLoading;
    /*End of the IU Elements*/
    /*Control variables*/
    private int solicitarBici;

    private int estacionSalida;
    private String mDeviceAddress;
    private String mDeviceName;
    private static String loanKey;
    private static String userLoanKey;

    private static BluetoothLeService mBluetoothLeService;
    private BluetoothGatt bluetoothGatt;
    private boolean mConnected = false;
    private final static String TAG = LoanConfirmed.class.getSimpleName();

    /*  ---------- API variables ------------------*/
    private Api api; //This is the call to the api
    private String base_url = "http://localhost:3000/api/";

    //For firebase
    FirebaseDatabase database;
    DatabaseReference myStationRef;
    ValueEventListener postListener;

    //Id to store de user loan at TEC digital
    private static ReleaseBicycleResponse releaseBicycleResponse;

    /*----------------------------------------------------------*/
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    public final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                //Log.e(TAG, "Unable to initialize Bluetooth");
                Log.d("Conexion","Estoy en el mBluetoothLeService.initialize");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            if(mBluetoothLeService.connect(mDeviceAddress)){
                //Log.d("Sarah K", "Se conecto bien");
                Log.d("Conexion","Estoy conectándome con el adress que me dieron");
            }else{
                //Log.d("Sarah K", "No se conecto");
                Log.d("Conexion","No me pude conectar");
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
            Log.d("Conexion","Me desconecté");
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
                Log.d("Conexion","En el broadcast me conecté");
                //Log.d("BroadcastReceiver", "Esta conectado");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                Log.d("Conexion","En el broadcast me desconecté");
                //Log.d("BroadcastReceiver", "No esta conectado");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                Log.d("Conexion","Me encontré otro servicio");
                Log.d("Conexion","El estado de la variable mConnected es " + mConnected);
                /**** ABEL ****/
                //Paso 2.2 Conectarse a la device address del Adafruit Bluefruit LE (Se acaba de hacer aquí)
                // y esperar la confirmación (Se debe realizar un ciclo para esperar la respuesta)

                /*while(BluetoothLeService.cont == 0){
                    Log.d("Abel","cont = "+BluetoothLeService.cont);

                    //onCharacteristicChanged debe cambiar a 1 cont cuando se lea la respuesta
                    mBluetoothLeService.enableTXNotification();
                    //Log.d("Abel","response 2.2 not received");
                }
                /*if(BluetoothLeService.cont == 1){
                    Log.d("Abel","cont = "+BluetoothLeService.cont);

                    //onCharacteristicChanged debe cambiar a 1 cont cuando se lea la respuesta
                    mBluetoothLeService.enableTXNotification();
                    //Log.d("Abel","response 2.2 not received");
                    layoutCofirm = (RelativeLayout) findViewById(R.id.finishView);
                    layoutLoading = (RelativeLayout) findViewById(R.id.progressView);
                    layoutCofirm.setVisibility(View.VISIBLE);
                    layoutLoading.setVisibility(View.INVISIBLE);
                }*/
                if(mConnected){
                    while (true){
                        boolean tryWrite = false;
                        byte[] value = new byte[]{79, 112, 101, 110, 71, 97, 83, 69, 83, 76, 97, 98, 33};
                        tryWrite = mBluetoothLeService.writeRXCharacteristic(value);
                        if(tryWrite){
                            break;
                        }
                    }
                    while (BluetoothLeService.cont2 == 0) {
                        //Log.d("Abel","cont2 = "+BluetoothLeService.cont2);
                        mBluetoothLeService.enableTXNotification();
                        //Log.d("Abel","response_3 not received");
                    }
                    if(BluetoothLeService.cont2 == 1){
                        //Intent activeLoan = new Intent(LoanConfirmed.this,ActiveLoan.class);
                        //crearInstanciaFirebase();
                        //saveUserLoan();
                        /*activeLoan.putExtra(ActiveLoan.EXTRAS_DEVICE_NAME, mDeviceName);
                        activeLoan.putExtra(ActiveLoan.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
                        startActivity(activeLoan);*/
                        ActiveLoanTask activeLoanTask = new ActiveLoanTask();
                        activeLoanTask.execute();
                    }
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
    public void crearInstanciaFirebase(String time){
        DatabaseReference myRef = database.getReference("Record");
        DatabaseReference myBicycleRef = database.getReference("Bicycle").child(mDeviceAddress).child("state");
        Record record = new Record(LogIn.getUs().getUserName(),time,"",mDeviceAddress,estacionSalida,-1);
        loanKey = myRef.push().getKey();
        myRef.child(loanKey).setValue(record);
        myBicycleRef.setValue("not available");

    }
    public void saveUserLoan(String date){
        DatabaseReference myRef = database.getReference("User").child(LogIn.getUs().getUserName());
        //myRef.push().setValue()
        UserLoan userLoan = new UserLoan(mDeviceAddress,date,0,0,"En curso");
        //userLoanKey = myRef.push().getKey();
        myRef.child("PrestamoActivo").setValue(loanKey);
        myRef.child(loanKey).setValue(userLoan);
    }

    private void releaseBycicle(){
        Call<ReleaseBicycleResponse> call = LogIn.getApi().releaseBicycle(
                LogIn.getLoginData().getUser_id(),mDeviceAddress,estacionSalida);
        Toast.makeText(getApplicationContext(),LogIn.getLoginData().getUser_id(),Toast.LENGTH_SHORT).show();
        call.enqueue(new Callback<ReleaseBicycleResponse>() {
            @Override
            public void onResponse(Call<ReleaseBicycleResponse> call, Response<ReleaseBicycleResponse> response) {
                if(response.code() == 200){
                    Toast.makeText(getApplicationContext(),"Prestamo en la BD",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(),"gg",Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ReleaseBicycleResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(),"Fallé",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTime(){
        Call<TimeResponse>call = LogIn.getApi().time(LogIn.getUs().getUserName());
        call.enqueue(new Callback<TimeResponse>() {
            @Override
            public void onResponse(Call<TimeResponse> call, Response<TimeResponse> response) {
                if(response.code() == 200){
                    TimeResponse time = response.body();
                    String[] date = time.getServer_time().split(" ");
                    String hour = date[1];
                    crearInstanciaFirebase(time.getServer_time());
                    saveUserLoan(time.getServer_time());
                    changeStationSpace();
                    //releaseBycicle();
                }
            }
            @Override
            public void onFailure(Call<TimeResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(),"No pude conseguir la hora",Toast.LENGTH_SHORT).show();
            }
        });
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

        /*******************************************************************/

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        estacionSalida = intent.getIntExtra(EXTRAS_STATION,0);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        database = FirebaseDatabase.getInstance();

    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            //Log.d(TAG, "Connect request result=" + result);
        }
    }
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
        myStationRef.removeEventListener(postListener);
    }
    @Override
    public void onBackPressed() {
    }



    private class ActiveLoanTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            //crearInstanciaFirebase();
            getTime();

            //saveUserLoan();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //if(BluetoothLeService.cont2 == 1){
                Intent activeLoan = new Intent(LoanConfirmed.this,ActiveLoan.class);
                activeLoan.putExtra(ActiveLoan.EXTRAS_DEVICE_NAME, mDeviceName);
                activeLoan.putExtra(ActiveLoan.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
                finish();
                startActivity(activeLoan);
           // }
        }
    }

    private void changeStationSpace(){
        myStationRef = database.getReference("Station").child(Integer.toString(estacionSalida)).child("Space");
        postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long spaces = (long) dataSnapshot.getValue();
                int suma = (int) spaces + 1;
                //Toast.makeText(getApplicationContext(),Integer.toString(suma),Toast.LENGTH_SHORT).show();
                myStationRef.setValue(suma);
                database.getReference("Bicycle").child(mDeviceAddress).child("station").setValue("");
                myStationRef.removeEventListener(postListener);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Falta código para todos los onCancelled
            }
        };
        myStationRef.addValueEventListener(postListener);
    }

    public static BluetoothLeService getBluethothLeService(){
        return mBluetoothLeService;
    }

    public static String getLoanKey() {
        return loanKey;
    }

    public static String getUserLoanKey() {
        return userLoanKey;
    }

    public static void setLoanKey(String loanKey) {
        LoanConfirmed.loanKey = loanKey;
    }

    public static void setUserLoanKey(String userLoanKey) {
        LoanConfirmed.userLoanKey = userLoanKey;
    }
}
