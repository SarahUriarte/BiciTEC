package com.example.bicitec_project;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.bicitec_project.Classes.TimeResponse;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
                Log.d("BroadcastReceiver", "Esta conectado");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                //updateConnectionState(R.string.disconnected);
                //Toast.makeText(getApplicationContext(), "Desconectado", Toast.LENGTH_SHORT).show();
                //clearUI();

                Log.d("BroadcastReceiver", "No esta conectado");

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.

                /**** ABEL ****/
                //Paso 2.2 Conectarse a la device address del Adafruit Bluefruit LE (Se acaba de hacer aquí)
                // y esperar la confirmación (Se debe realizar un ciclo para esperar la respuesta)
                /*if(mConnected){


                    if (BluetoothLeService.cont3 == 1) {
                        mBluetoothLeService.disconnect();
                        finishViewReq.setVisibility(View.INVISIBLE);
                        finishView.setVisibility(View.VISIBLE);
                        /*EndLoanTask endLoanTask = new EndLoanTask();
                        endLoanTask.execute();*/

                        //finishLoanOnDB();



                    /*Intent prestamoFinalizado = new Intent(PrestamoActivo.this, PrestamoFinalizado.class);
                    startActivity(prestamoFinalizado);
                        //
                    }
                }

                /*if(!mConnected){
                    EndLoanTask endLoanTask = new EndLoanTask();
                    endLoanTask.execute();
                }*/
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
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        final Intent intent = getIntent();
        mDeviceAddress = intent.getStringExtra("DeviceAdress");
        finishView = (RelativeLayout)findViewById(R.id.finishView);
        finishViewReq = (RelativeLayout) findViewById(R.id.finishViewReq);
        but = (Button)findViewById(R.id.button);
        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mBluetoothLeService.enableTXNotification();
                /*while (true) {
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
                /*if (BluetoothLeService.cont3 == 1) {
                    Toast.makeText(getApplicationContext(), "Devuelta", Toast.LENGTH_SHORT);
                    mBluetoothLeService.disconnect();
                    //finishLoanOnDB();
                    /*Intent prestamoFinalizado = new Intent(PrestamoActivo.this, PrestamoFinalizado.class);
                    startActivity(prestamoFinalizado);



                }*/
                WriteThread writeThread = new WriteThread();
                writeThread.execute();
            }
        });

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

    }


    private void finishLoanOnDB(String time){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Historial").child(LoanConfirmed.getLoanKey());
        DatabaseReference myUserRef = database.getReference("User").child(LogIn.getUs().getUserName());
        DatabaseReference myBiciRef = database.getReference("Bicycle").child(mDeviceAddress);
        myRef.child("estado").setValue("Terminado");
        myRef.child("horaFin").setValue(time);
        myUserRef.child(LoanConfirmed.getLoanKey()).child("loanState").setValue("Terminado");
        myUserRef.child("PrestamoActivo").setValue("");
        myBiciRef.child("state").setValue("available");
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
        System.gc();
    }

    private class EndLoanTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... voids) {

            //finishLoanOnDB();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //super.onPostExecute(aVoid);
            finishViewReq.setVisibility(View.INVISIBLE);
            finishView.setVisibility(View.VISIBLE);

            //finish();

            Log.d("Sarah", "Salí de aquí ");
        }
    }

    private class  WriteThread extends AsyncTask<Void,Integer,Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            while (true) {
                boolean tryWrite = false;
                byte[] value = new byte[]{67, 108, 111, 115, 101, 71, 97, 83, 69, 83, 76, 97, 98, 33};
                tryWrite = mBluetoothLeService.writeRXCharacteristic(value);
                if (tryWrite) {
                    break;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            WaitAnswerThread waitAnswerThread = new WaitAnswerThread();
            waitAnswerThread.execute();
        }
    }

    private class WaitAnswerThread extends AsyncTask<Void,Integer,Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            while (BluetoothLeService.cont3 == 0) {
                Log.d("Abel", "cont2 = " + BluetoothLeService.cont2);
                mBluetoothLeService.enableTXNotification();
                //Log.d("Abel","response_3 not received");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mBluetoothLeService.disconnect();
            getTime();
            finishViewReq.setVisibility(View.INVISIBLE);
            finishView.setVisibility(View.VISIBLE);

        }
    }
    private void getTime(){
        Call<TimeResponse> call = LogIn.getApi().time(LogIn.getUs().getUserName());
        call.enqueue(new Callback<TimeResponse>() {
            @Override
            public void onResponse(Call<TimeResponse> call, Response<TimeResponse> response) {
                if(response.code() == 200){
                    TimeResponse time = response.body();
                    String[] date = time.getServer_time().split(" ");
                    String hour = date[1];
                    finishLoanOnDB(hour);
                }
            }
            @Override
            public void onFailure(Call<TimeResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(),"Fallé",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
