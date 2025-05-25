package com.example.workout;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import android.Manifest;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import Model.Repository.GeneralRepository;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback
{

    private MapView mapView;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    private EditText mapViewSearchTextBox;
    private RecyclerView citiesRv;
    private CityAdapter adapter;
    private GeneralRepository generalRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_page);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null)
        {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        // Create a location service instance
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Make sure we have permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST
            );
        }

        mapViewSearchTextBox = findViewById(R.id.mapViewSearchTextBox);
        citiesRv = findViewById(R.id.citiesRecyclerView);
        generalRepository = new GeneralRepository();
        adapter = new CityAdapter(city -> // What happens when you press on a row
        {

        });

        citiesRv.setLayoutManager(new LinearLayoutManager(this));
        citiesRv.setAdapter(adapter);

        mapViewSearchTextBox.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int st, int c, int a)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c)
            {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                String q = s.toString().trim();
                adapter.setQuery(q);
                if (q.isEmpty() || s.toString().isEmpty())
                {
                    adapter.setCities(new ArrayList<>());
                    citiesRv.setVisibility(View.GONE);
                }
                else
                {
                    generalRepository.getCityByNamePartially(q)
                            .addOnSuccessListener(cities ->
                            {
                                adapter.setCities(cities);
                                // show or hide based on results
                                citiesRv.setVisibility(cities.isEmpty()
                                        ? View.GONE
                                        : View.VISIBLE);
                            });
                }
            }
        });



    }

    @Override
    public void onMapReady(GoogleMap map)
    {
        this.googleMap = map;
        enableMyLocation();
    }

    private void enableMyLocation()
    {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1001);
        }
        else
        {
            // Disable google's UI button because you can't align it where you want
            // And because it's stupid
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            // Show the blue dot
            googleMap.setMyLocationEnabled(true);

            // Move camera to last known location
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location ->
                    {
                        if (location != null)
                        {
                            LatLng ll = new LatLng(
                                    location.getLatitude(),
                                    location.getLongitude()
                            );
                            googleMap.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(ll, 15f)
                            );
                        }
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            enableMyLocation();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onPause()
    {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null)
        {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }
        mapView.onSaveInstanceState(mapViewBundle);
    }
}
