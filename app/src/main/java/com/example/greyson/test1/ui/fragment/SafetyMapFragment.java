package com.example.greyson.test1.ui.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;


import com.example.greyson.test1.R;
import com.example.greyson.test1.entity.SafePlaceRes;
import com.example.greyson.test1.net.WSNetService;
import com.example.greyson.test1.ui.activity.MapSettingActivity;
import com.example.greyson.test1.ui.base.BaseFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;


/**
 * Created by greyson on 28/3/17.
 */

public class SafetyMapFragment extends BaseFragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener, View.OnClickListener, OnMapReadyCallback {
    private static final int REQUEST_FINE_LOCATION = 1;
    private GoogleMap googleMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    MapView mapView;

    private LinearLayout mLLSafePlace;
    private LinearLayout mLLSafePin;
    SharedPreferences prefs = null;//
    Set<String> latSet = new HashSet<>();
    Set<String> lngSet = new HashSet<>();

    @Override
    protected View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_safetymap, container, false);

        mLLSafePlace = (LinearLayout) view.findViewById(R.id.ll_safetyplace);
        mLLSafePin = (LinearLayout) view.findViewById(R.id.ll_safetypin);

        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        try {
            MapsInitializer.initialize(mContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mapView.getMapAsync(this);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(3000)        // 30 seconds, in milliseconds
                .setFastestInterval(3000) // 30 second, in milliseconds
                .setSmallestDisplacement(3); // 1 meter
        return view;
    }

    @Override
    public void onMapReady(GoogleMap mMap) {
        googleMap = mMap;
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);}
        }
        googleMap.getUiSettings().setZoomControlsEnabled(true);  //
        googleMap.getUiSettings().setCompassEnabled(true);       //
        googleMap.getUiSettings().setMapToolbarEnabled(true);
        //googleMap.animateCamera(CameraUpdateFactory.zoomTo(14));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            if (mLLSafePin.isSelected() == true) {initPinMap();}
            else {initPlaceMap();}
        }
    }

    private void handleNewLocation() {
        LatLng latLng = getCurrentLocation();
        if (mLLSafePlace.isSelected() == true || mLLSafePlace.isSelected() != false) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(14));
        }
        Map<String, String> params = new HashMap<>();
        params.put("lat", String.valueOf(latLng.latitude));
        params.put("lng", String.valueOf(latLng.longitude));
        mRetrofit.create(WSNetService.class)
                .getSafePlaceData(params)
                .subscribeOn(Schedulers.io())
                .compose(this.<SafePlaceRes>bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<SafePlaceRes>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(SafePlaceRes safePlaceRes) {
                        showMarker(safePlaceRes);
                    }
                });
    }

    private void showMarker(SafePlaceRes safePlaceRes) {
        googleMap.clear();
        for (SafePlaceRes.ResultsBean sfRes : safePlaceRes.getResults()) {
            Double lat = sfRes.getLatitude();
            Double lng = sfRes.getLongitude();
            String type = sfRes.getType();
            googleMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(type));
        }
    }

    private LatLng getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_FINE_LOCATION);
            }
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
        return latLng;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) {return;}
                    googleMap.setMyLocationEnabled(true);
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                    initPlaceMap();
                } else {
                    getActivity().finish();
                }
                return;
            }
        }
    }

    @Override
    protected void initEvent() {
        mLLSafePlace.setOnClickListener(this);
        mLLSafePlace.setSelected(true);
        mLLSafePin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        mLLSafePlace.setSelected(false);
        mLLSafePin.setSelected(false);
        switch (v.getId()) {
            case R.id.ll_safetyplace:
                mLLSafePlace.setSelected(true);
                mLLSafePin.setSelected(false);
                initPlaceMap();
                break;
            case R.id.ll_safetypin:
                mLLSafePin.setSelected(true);
                mLLSafePlace.setSelected(false);
                initPinMap();
                break;
        }
    }

    private void initPlaceMap() {
        googleMap.clear();
        LatLng latLng = getCurrentLocation();
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(14));
        handleNewLocation();
    }

    private void initPinMap() {
        googleMap.clear();
        LatLng latLng = getCurrentLocation();
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(18));
        prefs = mContext.getSharedPreferences("LatLng",MODE_PRIVATE);
        if((prefs.contains("Lat")) && (prefs.contains("Lng")))
        {
            List<String> latPlist = new ArrayList<>(prefs.getStringSet("Lat",null));
            List<String> lngPlist = new ArrayList<>(prefs.getStringSet("Lng",null));
            latSet = new HashSet<>(latPlist);
            lngSet = new HashSet<>(lngPlist);
            if (latPlist.size() == lngPlist.size()) {
                for (int i = 0; i< latPlist.size();i++) {
                    String lat = latPlist.get(i);
                    String lng = lngPlist.get(i);
                    LatLng l =new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));
                    googleMap.addMarker(new MarkerOptions().position(l)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
                }
            }
        }
        Marker pinMarker = googleMap.addMarker(new MarkerOptions().position(latLng)
                .draggable(true).title("New Incident Pin").snippet("Drag and Drop :)")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
        pinMarker.showInfoWindow();

        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                marker.setSnippet("Drag me");
                marker.showInfoWindow();
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                marker.setSnippet("Drop me");
                marker.showInfoWindow();
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng latLng = marker.getPosition();
                marker.setTitle("Click here for setting");
                marker.setSnippet("Can not drag again");
                marker.setDraggable(false);
                marker.showInfoWindow();
                latSet.add(String.valueOf(latLng.latitude));
                lngSet.add(String.valueOf(latLng.longitude));
                prefs.edit().putStringSet("Lat",latSet).commit();
                prefs.edit().putStringSet("Lng",lngSet).commit();
            }
        });

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (mLLSafePin.isSelected() == true) {
                    marker.setTitle("Hi");
                    marker.setSnippet("Click here for setting");
                    marker.showInfoWindow();
                }else{
                    marker.showInfoWindow();
                }
                return true;
            }
        });

        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (mLLSafePin.isSelected() == true) {
                    Intent intent = new Intent();
                    intent.setClass(mContext, MapSettingActivity.class);
                    intent.putExtra("pincolor", "red");
                    startActivityForResult(intent,0);
                    // marker
                    //startActivity(new Intent(mContext, MapSettingActivity.class));
                    //
                    initPinMap();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (mLLSafePin.isSelected() != true) {handleNewLocation();}///
    }

    @Override
    public void onResume() {
        super.onResume();
        mGoogleApiClient.connect();


    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void initData() {

    }

    private void handleResult(String str) {

    }
    @Override
    protected void destroyView() {

    }
}
