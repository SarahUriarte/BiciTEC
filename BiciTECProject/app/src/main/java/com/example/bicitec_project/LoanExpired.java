package com.example.bicitec_project;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class LoanExpired extends AppCompatActivity {
    private CountDownTimer timer;
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private String mDeviceAddress;
    private String mDeviceName;
    private BluetoothLeService mBluetoothLeService = LoanConfirmed.getBluethothLeService();
    long loanTime = 60000; //= 1800000;//1800000; //Estos deben ser 25 minutos
    private Dialog confirmFinish;
    private Button btnFinish;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_expired);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        loanTime = intent.getLongExtra("LongTime",0);

        if(loanTime == 0){
            loanTime = 1800000;
        }
        else {
            loanTime = 1800000 - loanTime;
        }
        confirmFinish = new Dialog(this);
        btnFinish = (Button)findViewById(R.id.btnFinishL);
        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmFinishPopUp();
            }
        });
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        startTimer();
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
        int minutes = (int) loanTime / 60000;
        int seconds = (int) loanTime % 60000 / 1000;

        /*String timeLeft = "" + minutes;
        timeLeft += ": ";
        if (seconds < 10) timeLeft += "0";
        timeLeft += seconds;*/
        if (minutes == 0 && seconds == 1) {
            //stopTimer();
            //Toast.makeText(getApplicationContext(), "Time over", Toast.LENGTH_SHORT).show();
           /*Cambia a la pantalla de robado*/

        }
    }
    private void showConfirmFinishPopUp(){
        Button accept;
        Button cancel;
        confirmFinish.setContentView(R.layout.loan_finished_pop_up);

        accept = (Button)confirmFinish.findViewById(R.id.btnAcceptPopUp);
        cancel = (Button)confirmFinish.findViewById(R.id.btnCancelPopUp);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmFinish.dismiss();
            }
        });
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent finishLoan = new Intent(LoanExpired.this,QrScannerFinish.class);
                finishLoan.putExtra("DeviceName",mDeviceName);
                finishLoan.putExtra("DeviceAdress",mDeviceAddress);
                startActivity(finishLoan);
            }
        });
        confirmFinish.show();
    }
    @Override
    public void onBackPressed() {
    }
}
