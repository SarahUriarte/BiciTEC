package com.example.bicitec_project;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ActiveLoan extends AppCompatActivity implements LocationListener {


    private CountDownTimer timer;
    private TextView txtTimer;
    private TextView txtTimerFinishing;
    private TextView txtDistance;
    private int distance = 0;
    long loanTime = 1800000;//1800000; //Estos deben ser 25 minutos
    private Button cerrarCandado;
    private RelativeLayout activeLoan;
    private RelativeLayout activeLoanFinishing;
    private Button btnFinish;

    private static final int PERMISSION_READ_STATE_LOCATION = 1;
    private LocationManager locationManager;
    /*Services variables*/
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private String mDeviceAddress;
    private String mDeviceName;
    private BluetoothLeService mBluetoothLeService;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mConnected = false;

    /*private final ServiceConnection mServiceConnection = new ServiceConnection() {

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
    };*/

   /* private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
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

                /**** ABEL **
                //Paso 2.2 Conectarse a la device address del Adafruit Bluefruit LE (Se acaba de hacer aquí)
                // y esperar la confirmación (Se debe realizar un ciclo para esperar la respuesta)


                if (BluetoothLeService.cont3 == 1) {
                    Log.d("Sarah", "Intentó conectar");
                    //btnConfirmado = (Button) findViewById(R.id.confirmadoBtn);
                    //btnConfirmado.setVisibility(View.VISIBLE);
                    //crearInstanciaFirebase("Prestada");
                    //Intent prestamoActivo = new Intent(DeviceControlActivity.this,PrestamoActivo.class);
                    //startActivity(prestamoActivo);

                }
                /***ABEL END
            }

        }
    };*/

    @Override
    protected void onPause() {
        super.onPause();
        //unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*unbindService(mServiceConnection);
        mBluetoothLeService = null;*/
    }


    /*private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }*/

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_loan);
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        /*if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "DEvice not supported", Toast.LENGTH_SHORT).show();
            finish();
        }*/

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        /*final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();*/

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        /*Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);*/

        txtTimer = (TextView) findViewById(R.id.txtTimer);
        txtTimerFinishing = (TextView) findViewById(R.id.txtTimer2);
        txtDistance = (TextView)findViewById(R.id.txtDistance);

        cerrarCandado = (Button) findViewById(R.id.btnCerrarCandado);
        /*cerrarCandado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                    startActivity(prestamoFinalizado);
                }
            }
        });*/
        btnFinish = (Button) findViewById(R.id.btnFinishLoan);
        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent finishLoan = new Intent(ActiveLoan.this,QrScannerFinish.class);
                finishLoan.putExtra("DeviceName",mDeviceName);
                finishLoan.putExtra("DeviceAdress",mDeviceAddress);
                startActivity(finishLoan);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.w("Sarah","Entré al onResume");
        //registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        /*try {
            int gpsSignal  = Settings.Secure.getInt(getContentResolver(),Settings.Secure.LOCATION_MODE);
            if(gpsSignal == 0){

                //El gps está desactivado
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }*/
        /*if(statusCheck()){

        }*/
        startTimer();
    }
    public boolean statusCheck() {
        Log.w("Sarah","Vine a checkear el status");
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean flag = !manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        Log.w("Sarah","The flag is "+ flag);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(getApplicationContext(),"You need to give gps access",Toast.LENGTH_SHORT);
            buildAlertMessageNoGps();
        }
        else{
            return true;
        }
        return false;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    /*Inicio de los métodos para iniciar el temporizador de media hora de préstamo
     * de la bicicleta. Este método de CountDownTimer recibe la variable global loantime que es
     * el tiempo del préstamo, además de el intervalo de tiempo en que cambia, que es 1000ms = 1s
     * en el onTick "l" representa al tiempo que falta para acabar la cuenta*/
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
        int minutes = (int) loanTime / 60000;
        int seconds = (int) loanTime % 60000 / 1000;

        String timeLeft = "" + minutes;
        timeLeft += ": ";
        if (seconds < 10) timeLeft += "0";
        timeLeft += seconds;
        if (minutes == 1 && seconds == 1) {
            //stopTimer();
            Toast.makeText(getApplicationContext(), "Time over", Toast.LENGTH_SHORT).show();
            //Intent prestamoActivo = new Intent(PrestamoActivo.this,PrestamoActivo5Min.class);
            //startActivity(prestamoActivo);
            activeLoan = (RelativeLayout) findViewById(R.id.activeLoanView);
            activeLoanFinishing = (RelativeLayout) findViewById(R.id.activeLoanViewFinishing);

            activeLoan.setVisibility(View.INVISIBLE);
            activeLoanFinishing.setVisibility(View.VISIBLE);
        }
        if (minutes == 0 && seconds == 1) {
            //stopTimer();
            Toast.makeText(getApplicationContext(), "Time over", Toast.LENGTH_SHORT).show();
            Intent loanFinished = new Intent(ActiveLoan.this, LoanFinished.class);
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
            }
            loanFinished.putExtra(ActiveLoan.EXTRAS_DEVICE_NAME, mDeviceName);
            loanFinished.putExtra(ActiveLoan.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
            startActivity(loanFinished);

        }

        txtTimer.setText(timeLeft);
        txtTimerFinishing.setText(timeLeft);
    }

    private boolean checkPermissionLocation() {
        return (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermissionLocation() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_READ_STATE_LOCATION);
    }
    /*Fin de los métodos de temporización*/

    private void updateLocation() {
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        Log.w("Sarah","Me vine para el update location");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            //    here to request the missing permissions, and then overriding
            //    public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Log.w("Sarah","Garanticé los permisos");
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 600000, 3,this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 600000, 5,this);
    }
    @Override
    public void onLocationChanged(Location location) {
        Log.w("Sarah","Hola, onLocationChanged");
        Toast.makeText(getApplicationContext(),"Provider " +location.getProvider(),Toast.LENGTH_SHORT).show();
        distance += 5;
        txtDistance.setText(Integer.toString(distance));
        //Log.w("Sarah","Garanticé los permisos");
        /*location.getLatitude();*/
        Toast.makeText(getApplicationContext(),
                "Latitud"+String.valueOf(location.getLatitude())+"Longitud "+String.valueOf(location.getLongitude()),
                Toast.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(), Integer.toString(distance), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
