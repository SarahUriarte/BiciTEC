package com.example.bicitec_project;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class ReportBicycle extends AppCompatActivity {
    private List<String> spinnerParking;
    private Spinner sItemsParking;
    private List<String> spinnerDamage;
    private Spinner sItemsDamage;
    private Button btnAttac;
    private ImageView imgAttached;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_bicycle);
        spinnerParking =  new ArrayList<String>();
        spinnerParking.add("Principal");
        spinnerParking.add("Gimnasio");
        spinnerParking.add("Piscina");
        spinnerParking.add("D3");
        spinnerParking.add("K1");
        spinnerParking.add("Learning commons");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, spinnerParking);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sItemsParking = (Spinner) findViewById(R.id.spnParqueos);
        sItemsParking.setAdapter(adapter);

        spinnerDamage =  new ArrayList<String>();
        spinnerDamage.add("Pedal");
        spinnerDamage.add("Canasta");
        spinnerDamage.add("Llanta");
        spinnerDamage.add("Aro");
        spinnerDamage.add("Timbre");
        spinnerDamage.add("Otro");

        ArrayAdapter<String> adapterDamage = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, spinnerDamage);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sItemsDamage = (Spinner) findViewById(R.id.spnDamage);
        sItemsDamage.setAdapter(adapterDamage);

        btnAttac = (Button) findViewById(R.id.btnAttach);
        imgAttached = (ImageView)findViewById(R.id.imgAttached);
        isReadStoragePermissionGranted();
        btnAttac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if(isReadStoragePermissionGranted()){

                }*/
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                i.setType("image/");
                startActivityForResult(i.createChooser(i,"Seleccione la aplicación"),10);
            }
        });
    }
    public  boolean isReadStoragePermissionGranted(){
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                //Log.v(TAG,"Permission is granted1");
                return true;
            } else {

                //Log.v(TAG,"Permission is revoked1");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            //Log.v(TAG,"Permission is granted1");
            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            Uri path = data.getData();
            Bitmap bmp = null;
           // bmp.compress(Bitmap.CompressFormat.PNG, 100, path);
            try {
                FileOutputStream out = new FileOutputStream(String.valueOf(path));
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out); //100-best quality
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            imgAttached.setImageBitmap(bmp);
        }
    }
    private static Bitmap codec(Bitmap src, Bitmap.CompressFormat format,
                                int quality) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        src.compress(format, quality, os);

        byte[] array = os.toByteArray();
        return BitmapFactory.decodeByteArray(array, 0, array.length);
    }
}
