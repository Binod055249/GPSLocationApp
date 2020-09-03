package com.example.gpslocationapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener,
        LocationListener {

    public static final String TAG = "TAG";
    private static final int REQUEST_CODE = 1000;

    private GoogleApiClient googleApiClient;
    // private Location location;
    // private TextView txtLocation;

    EditText edtAddress, edtMilesPerHour, edtMetersPerMile;
    TextView txtDistanceValue, txtTime;
    Button btnGetTheData;

    private String destinationLocationAddress = "";

    private TaxiManager taxiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //   txtLocation=findViewById(R.id.txtLocation);

        edtAddress = findViewById(R.id.edtAdress);
        edtMetersPerMile = findViewById(R.id.edtMetersPerMile);
        edtMilesPerHour = findViewById(R.id.edtMilesPerHour);

        txtDistanceValue = findViewById(R.id.txtDistanceValue);
        txtTime = findViewById(R.id.txtTime);

        btnGetTheData = findViewById(R.id.btnGetTheData);

        btnGetTheData.setOnClickListener(MainActivity.this);

        taxiManager = new TaxiManager();

        googleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .addConnectionCallbacks(MainActivity.this)
                .addOnConnectionFailedListener(MainActivity.this)
                .addApi(LocationServices.API)
                .build();

    }

    @Override
    public void onLocationChanged(Location location) {

        onClick(null);
    }

    @Override
    protected void onPause() {
        super.onPause();

        FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
        fusedLocationProviderApi.removeLocationUpdates(googleApiClient, MainActivity.this);
    }

    @Override
    public void onClick(View v) {

        String addressValue = edtAddress.getText().toString();
        boolean isGeoCoding = true;

        if (!addressValue.equals(destinationLocationAddress)) {

            //   addressValue = destinationLocationAddress;

            destinationLocationAddress = addressValue;
            Geocoder geocoder = new Geocoder(getApplicationContext());

            try {

                List<Address> myAddress = geocoder.getFromLocationName(destinationLocationAddress, 4);

                if (myAddress != null) {

                    double latitude = myAddress.get(0).getLatitude();
                    double longitude = myAddress.get(0).getLongitude();

                    Location locationAddress = new Location("My Destination");
                    locationAddress.setLatitude(latitude);
                    locationAddress.setLongitude(longitude);
                    taxiManager.setDestinationLocation(locationAddress);


                }
            } catch (Exception e) {

                isGeoCoding = false;
                e.printStackTrace();
            }
        }

        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {


            FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
            Location userCurrentlocation = fusedLocationProviderApi.getLastLocation(googleApiClient);

            if (userCurrentlocation != null && isGeoCoding) {

                txtDistanceValue.setText(taxiManager.returnTheMilesBetweenCurrentLocationAndDestinationLocation
                        (userCurrentlocation, Integer.parseInt(edtMetersPerMile.getText().toString())));

                txtTime.setText(taxiManager.returnTimeLeftToGetToDestinationLocation
                        (userCurrentlocation, Float.parseFloat(edtMilesPerHour.getText().toString()), Integer.parseInt(edtMetersPerMile.getText().toString())));

            }

        } else {

            txtDistanceValue.setText("This app is not allowed to access the location");
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        }


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Log.d(TAG, "we are connected");
        //   showTheUserLocation();

        //FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(5f);

        if (googleApiClient.isConnected()) {


            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            }
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest,new LocationCallback(){

                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                }

                @Override
                public void onLocationAvailability(LocationAvailability locationAvailability) {
                    super.onLocationAvailability(locationAvailability);
                }
            },null);
        }else {

            googleApiClient.connect();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
     Log.d(TAG,"The connection is suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
      Log.d(TAG,"The connection is failed");

      if(connectionResult.hasResolution()){

          try {
              connectionResult.startResolutionForResult(MainActivity.this,
                      REQUEST_CODE);
          } catch (Exception e) {
             Log.d(TAG,e.getStackTrace().toString());
          }
      }else {

          Toast.makeText(this, "Google play services is not working. EXIT!", Toast.LENGTH_LONG).show();
          finish();
      }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==REQUEST_CODE && resultCode==RESULT_OK){

            googleApiClient.connect();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(googleApiClient!=null){

            googleApiClient.connect();
        }
    }

    //custom methods
 /*   private void showTheUserLocation(){

        //Runtime Permission
        int permissionCheck= ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {

            FusedLocationProviderApi fusedLocationProviderApi=LocationServices.FusedLocationApi;
            location=fusedLocationProviderApi.getLastLocation(googleApiClient);

            if(location!=null){

                double latitude=location.getLatitude();
                double longitude=location.getLongitude();

                txtLocation.setText(latitude+ ", "+longitude);
            }else{

                txtLocation.setText("The app is not able to access the location now. Try again later");
            }


        }else{

            txtLocation.setText("This map is not allowed to access the location");
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},1);
        }
    }
  */


}