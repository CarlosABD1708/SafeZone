package com.example.myapplication;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ktx.Firebase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity  implements LocationListener {
    String latitud;
    String longitud;
    String Token;
    LocationManager locationManager;
    Firebase firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firestore = Firebase.INSTANCE;
        getPermission();
        isGPSEnable();
        getLocation();
        dataInsert();
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()){
                            System.out.println("Obtencion de token fallo");
                            return;
                        }
                        Token = task.getResult();

                    }
                });
//        FirebaseMessaging.getInstance().subscribeToTopic("Prueba").addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                String msg = "Done";
//                if(!task.isSuccessful()){
//                    msg = "Failed";
//                }
//            }
//        });
    }

//    public String onTokenRefresh() {
//        // Get updated InstanceID token.
//        String refreshedToken = FirebaseInstallations.getInstance().getToken(false).toString();
////        Log.d(TAG, "Refreshed token: " + refreshedToken)
//        return refreshedToken;
//    }
    private void dataInsert() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String claveUnica = "MiAbuelita1"; // Puedes establecer la clave que desees.
//
//        String Token = onTokenRefresh();
// Crear un mapa con los datos que deseas agregar.
        Map<String, Object> datos = new HashMap<>();
        datos.put("Longitud", longitud);
        datos.put("Latitud", latitud);
        datos.put("Token",Token);

// Obtener una referencia a la ubicación donde deseas agregar los datos y establecer la clave única personalizada.
        DatabaseReference ubicacionRef = databaseReference.child("ubicaciones").child(claveUnica);

// Agregar los datos con la clave única personalizada.
        ubicacionRef.setValue(datos)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // La operación se realizó con éxito.
                        Toast.makeText(getApplicationContext(), "Datos agregados correctamente", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Se produjo un error al agregar los datos.
                        Toast.makeText(getApplicationContext(), "Error al agregar los datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
        
    private void isGPSEnable() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean GPSenable = false;
        boolean NetworkEnable = false;
        try {
            GPSenable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            NetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }catch (Exception e){
            e.printStackTrace();
        }

        if(!GPSenable && !NetworkEnable){
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Habilita EL GPS")
                    .setCancelable(false)
                    .setPositiveButton("Habilitar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivities(new Intent[]{new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)});
                        }
                    }).setNegativeButton("Cancel",null).show();
        }
    }

    private void getPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_COARSE_LOCATION)!=
                        PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},100);
        }
    }

    private void getLocation() {
        try {
            locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,500,5,(LocationListener) this);
        }catch (SecurityException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            latitud = String.valueOf(location.getLatitude());
            longitud = String.valueOf(location.getLongitude());

            dataInsert();
        }catch (Exception e){e.printStackTrace();}
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LocationListener.super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }
}