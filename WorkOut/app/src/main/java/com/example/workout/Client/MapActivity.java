package com.example.workout.Client;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.workout.MainActivity;
import com.example.workout.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.Manifest;

import android.widget.Toast;

import java.util.ArrayList;

import Model.Business;
import Model.Client;
import Model.Course;
import Model.Location;
import Model.Repository.BusinessRepository;
import Model.SearchStrategies.SearchRadiusStrategy;
import Model.SearchStrategies.SearchStrategyInterface;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback
{

    private MapView mapView;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    private BusinessRepository businessRepository;
    private int defaultRadius = 100;
    private Location modelLocation;
    private Client client;
    private ArrayList<Course> courses;
    private ArrayList<Business> businessesFromCourses;
    private boolean isFromSearch;
    BottomNavigationView bottomNavigationView;

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

        businessRepository = new BusinessRepository();

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

        client = getIntent().getParcelableExtra("client");
        courses = getIntent().getParcelableArrayListExtra("course_list");
        businessesFromCourses = new ArrayList<>();
        if(courses == null)
        {
            isFromSearch = false;
            courses = new ArrayList<>();
        }

        for (Course course : courses)
        {
            businessRepository.getBusinessesById(course.getBusinessId()).addOnSuccessListener(business ->
            {
                businessesFromCourses.add(business);
            });
        }
        modelLocation = null;

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_map);
        setUpBottomNavigationView();
    }

    private void setUpBottomNavigationView()
    {
        bottomNavigationView.setOnItemSelectedListener(item ->
        {
            int id = item.getItemId();
            if (id == R.id.nav_home)
            {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("client", client);
                startActivity(intent);
                startActivity(intent);
                return true;
            }
            else if (id == R.id.nav_profile)
            {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("client", client);
                startActivity(intent);
                startActivity(intent);
                return true;
            }
            else if (id == R.id.nav_search)
            {
                Intent intent = new Intent(this, SearchActivity.class);
                intent.putExtra("client", client);
                startActivity(intent);
                return true;
            }
            else if (id == R.id.nav_map)
            {
                // you’re already here
                return true;
            }
            else if (id == R.id.nav_Calendar)
            {
                Intent intent = new Intent(this, CalendarActivity.class);
                intent.putExtra("client", client);
                startActivity(intent);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    @Override
    public void onMapReady(GoogleMap map)
    {
        this.googleMap = map;
        enableMyLocation();
    }

    private void showNearbyBusinesses()
    {
        if (modelLocation == null)
        {
            Toast.makeText(this, "Cannot find user location", Toast.LENGTH_SHORT).show();
            return;
        }

        SearchStrategyInterface distance = new SearchRadiusStrategy(defaultRadius);
        businessRepository.searchByStrategy(distance, modelLocation).addOnSuccessListener(businesses ->
        {
            for (Business current : businesses)
            {
                if(existInBusinessesFromCourses(current) || !isFromSearch)
                {
                    Location location = current.getLocation();
                    LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(position);
                    markerOptions.title(current.getBusinessName());
                    googleMap.addMarker(markerOptions);
                }
            }
        });
    }

    private boolean existInBusinessesFromCourses(Business business)
    {
        for(Business current : businessesFromCourses)
        {
            if(business.getId().equals(current.getId()))
                return true;
        }

        return false;
    }

    private void enableMyLocation()
    {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        } else
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

                            modelLocation = new Location(location.getLongitude(), location.getLatitude());
                            showNearbyBusinesses();
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
