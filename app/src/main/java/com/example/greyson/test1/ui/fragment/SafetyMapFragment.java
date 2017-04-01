package com.example.greyson.test1.ui.fragment;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;


import com.example.greyson.test1.R;
import com.example.greyson.test1.entity.SafePlaceRes;
import com.example.greyson.test1.net.WSNetService;
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
        com.google.android.gms.location.LocationListener,View.OnClickListener,OnMapReadyCallback {
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
            MapsInitializer.initialize(getActivity().getApplicationContext());
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
                .setInterval(2000)        // 1 seconds, in milliseconds
                .setFastestInterval(1000); // 1 second, in milliseconds
        return view;
    }

    @Override
    public void onMapReady(GoogleMap mMap) {
        googleMap = mMap;
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);  //
        googleMap.getUiSettings().setCompassEnabled(true);       //
        googleMap.getUiSettings().setMapToolbarEnabled(true);
        googleMap.animateCamera(CameraUpdateFactory.zoomBy(13));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        handleNewLocation();
    }

    private void handleNewLocation() {
        LatLng latLng = getCurrentLocation();
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        if (getDistance(latLng.latitude, latLng.longitude) < 1500) {}
        Map<String, String> params = new HashMap<>();
        params.put("", "");
        mRetrofit.create(WSNetService.class)
                .getSafePlaceData(params)
                .subscribeOn(Schedulers.io())
                .compose(this.<List<SafePlaceRes>>bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<SafePlaceRes>>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(List<SafePlaceRes> safePlaceRes) {
                        showMarker(safePlaceRes);
                    }
                });
    }

    private void showMarker(List<SafePlaceRes> safePlaceResList) {
        for (SafePlaceRes sfRes:safePlaceResList) {
            Double lat = sfRes.getLatitude();
            Double lng = sfRes.getLongtitude();
            String type = sfRes.getType();
            googleMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(type));
        }
    }

    public double getDistance(double a, double b) {
        float[] results = new float[1];
        Location.distanceBetween(a, b, -37.881035, 145.023311, results);
        return results[0];
    }

    private LatLng getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
        return latLng;
    }

    @Override
    protected void initEvent() {
        mLLSafePlace.setOnClickListener(this);
        mLLSafePin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        mLLSafePlace.setSelected(false);
        mLLSafePin.setSelected(false);
        switch (v.getId()) {
            case R.id.ll_safetyplace:
                mLLSafePlace.setSelected(true);
                initPlaceMap();
                break;
            case R.id.ll_safetypin:
                mLLSafePin.setSelected(true);
                initPinMap();
                break;
        }
    }

    private void initPlaceMap() {
        googleMap.clear();
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
        Marker pinMarker = googleMap.addMarker(new MarkerOptions().position(latLng).draggable(true)
                .title("New Event Pin").snippet("Drag abd Drop :)")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
        pinMarker.showInfoWindow();
        GoogleMap.OnMarkerDragListener mkDragListener = new GoogleMap.OnMarkerDragListener() {
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
                marker.setTitle("Pin Saved");
                marker.setSnippet("Sorry, can not drag again");
                marker.setDraggable(false);
                marker.showInfoWindow();
                latSet.add(String.valueOf(latLng.latitude));
                lngSet.add(String.valueOf(latLng.longitude));
                prefs.edit().putStringSet("Lat",latSet).commit();
                prefs.edit().putStringSet("Lng",lngSet).commit();
            }

        };
        googleMap.setOnMarkerDragListener(mkDragListener);

    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        googleMap.clear();
        handleNewLocation();
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
        //String requestPlace = "df";
        Map<String, String> params = new HashMap<>();
        params.put("", "");
        mRetrofit.create(WSNetService.class)
                .getSafePlaceData(params)
                .subscribeOn(Schedulers.io())
                .compose(this.<List<SafePlaceRes>>bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<SafePlaceRes>>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(List<SafePlaceRes> safePlaceRes) {
                        showMarker(safePlaceRes);
                    }

        });
    }

    private void handleResult(String str) {

    }
    @Override
    protected void destroyView() {

    }
}
