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
import android.support.v4.util.ArraySet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;


import com.example.greyson.test1.R;
import com.example.greyson.test1.entity.MyMarker;
import com.example.greyson.test1.entity.SafePlaceRes;
import com.example.greyson.test1.entity.UserPinHistory;
import com.example.greyson.test1.net.WSNetService;
import com.example.greyson.test1.ui.activity.MapSettingActivity;
import com.example.greyson.test1.ui.base.BaseFragment;
import com.example.greyson.test1.utils.StringUtils;
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
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private SharedPreferences preferences;

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
        String message = safePlaceRes.getMessage();
        if (message.equalsIgnoreCase("5 KM"))
        {
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(12));
            Toast.makeText(mContext, "There are no Safe Place in 2KM, Change to 5KM",Toast.LENGTH_LONG).show();
        }
        else if (message.equalsIgnoreCase("Nothing found"))
        {Toast.makeText(mContext, "There are no Safe Place in 5KM",Toast.LENGTH_LONG).show();}
        for (SafePlaceRes.ResultsBean sfRes : safePlaceRes.getResults()) {
            Double lat = sfRes.getLatitude();
            Double lng = sfRes.getLongitude();
            String type = sfRes.getType();
            switch (type) {
                case "Firestation":
                    googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(lat, lng))
                            .title(type)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.safeplace_f)));
                    break;
                case "Convenience Shop":
                    googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(lat, lng))
                            .title(type)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.safeplace_7)));
                    break;
                case "Petrol Station":
                    googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(lat, lng))
                            .title(type)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.safeplace_g)));
                    break;
                case "Restaurant":
                    googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(lat, lng))
                            .title(type)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.safeplace_m)));
                    break;
                case "Police Station":
                    googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(lat,lng))
                            .title(type)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.safeplace_p)));
                    break;
                case "Hospital":
                    googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(lat,lng))
                            .title(type)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.safeplace_h)));
                    break;
                case "Supermarket":
                    googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(lat,lng))
                            .title(type)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.safeplace_s)));
                    break;
                default:
                    googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(lat,lng))
                            .title(type));
            }
            //googleMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(type));

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
        saveLastLocationToSharedPreference(latLng);
        return latLng;
    }

    private void saveLastLocationToSharedPreference(LatLng latLng) {
            String lat = String.valueOf(latLng.latitude);
            String lng = String.valueOf(latLng.longitude);
            SharedPreferences preferences1 = mContext.getSharedPreferences("LastLocation",MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences1.edit();
            editor.putString("last location", lat + "," + lng);
            editor.commit();

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
        preferences = mContext.getSharedPreferences("LocalUser",MODE_PRIVATE);
        showMarkerFromSharedPreference(getObjectFromSharedPreference("admin"));

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
                marker.setSnippet("Click here for setting");
                marker.showInfoWindow();
            }
        });

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (mLLSafePin.isSelected() == true) {
                    //marker.setSnippet("Click here for setting");//
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
                    sendPinStatus(marker);
                }
            }
        });
    }

    public void sendPinStatus (Marker marker) {
        Intent intent = new Intent();
        intent.setClass(mContext, MapSettingActivity.class);
        String markerTag;
        String markerStatus;
        markerTag = (String) marker.getTag();
        markerStatus = "old";
        if (markerTag == null){
            markerStatus = "new";
            int listSize = getObjectFromSharedPreference("admin").getMmk().size();
            String pinIndex = String.valueOf(listSize);
            markerTag = pinIndex;
            marker.setSnippet("");
        }
        intent.putExtra("status", markerStatus);
        intent.putExtra("tag", markerTag);
        intent.putExtra("lat", marker.getPosition().latitude);
        intent.putExtra("lng", marker.getPosition().longitude);
        intent.putExtra("note", marker.getSnippet());
        startActivityForResult(intent, 1);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 0) {

        } else if (resultCode == 1) {
            handleDeletePin(data);
        } else if (resultCode == 2) {
            handleSavePin(data);
        }
    }

    private void handleDeletePin(Intent data) {
        Bundle b = data.getExtras();
        String tag = b.getString("tag");
        UserPinHistory userPinHistory = getObjectFromSharedPreference("admin");
        ArrayList<MyMarker> myMarkerList = userPinHistory.getMmk();
        Iterator<MyMarker> iterator = myMarkerList.iterator();
        while(iterator.hasNext()){
            MyMarker mk = iterator.next();
            String mkTag = mk.getMkTag();
            if(mkTag.equals(tag)){
                iterator.remove();
                Toast.makeText(mContext, "Pin Removed",Toast.LENGTH_SHORT).show();
            }
        }
        saveObjectToSharedPreference("admin",userPinHistory);
    }

    private void handleSavePin (Intent data) {
        Bundle b = data.getExtras();
        String color = b.getString("color");
        String note = b.getString("note");
        Double lat = b.getDouble("lat");
        Double lng = b.getDouble("lng");
        String tag = b.getString("tag");
        String pinStatus = b.getString("status");
        UserPinHistory userPinHistory = getObjectFromSharedPreference("admin");
        //Toast.makeText(mContext, tag + lat + lng + color + note,Toast.LENGTH_SHORT).show();
        if (pinStatus.equals("old")) {
            MyMarker updateMarker = userPinHistory.getMmk().get(Integer.valueOf(tag));
            updateMarker.setMkLat(lat);
            updateMarker.setMkLnt(lng);
            updateMarker.setMkColor(color);
            updateMarker.setMkDescription(note);
        }else{
            MyMarker myMarker = new MyMarker(tag, lat, lng, color, note);
            userPinHistory.getMmk().add(myMarker);
        }
        saveObjectToSharedPreference("admin",userPinHistory);
    }

    public void saveObjectToSharedPreference(String key, Object obj) {
        Gson gson = new Gson();
        String jsonObj = gson.toJson(obj);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, jsonObj);
        editor.commit();
    }

    public UserPinHistory getObjectFromSharedPreference(String key) {

        Gson gson = new Gson();
        String str = preferences.getString(key, null);
        if (str == null) {//first empty
            return new UserPinHistory();
        } else {
            try {
                return gson.fromJson(str, UserPinHistory.class);
            } catch (Exception e) {
                throw new IllegalArgumentException("Can not get object with key " + key);
            }
        }
    }

    private void showMarkerFromSharedPreference(UserPinHistory pinHistory) {
        ArrayList<MyMarker> myMarkerList = pinHistory.getMmk();
        Iterator<MyMarker> iterator = myMarkerList.iterator();
        int count = 0;
        while(iterator.hasNext()){
            MyMarker mk = iterator.next();
            LatLng l =new LatLng(mk.getMkLat(),mk.getMkLnt());
            Marker marker = googleMap.addMarker(new MarkerOptions().position(l));
            setMarkerColor(marker, mk.getMkColor());
            marker.setSnippet(mk.getMkDescription());
            marker.setTag(String.valueOf(count));
            mk.setMkTag(String.valueOf(count));//
            marker.showInfoWindow();
            count ++;
        }
        UserPinHistory latestPinHistory = new UserPinHistory();
        latestPinHistory.setMmk(myMarkerList);
        saveObjectToSharedPreference("admin",latestPinHistory);
    }


    private void setMarkerColor(Marker marker, String color) {
        switch (color) {
            case "Assault(Orange)":
                marker.setTitle("Assault");
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                break;
            case "Theft(Yellow)":
                marker.setTitle("Theft");
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                break;
            case "Robbery(Green)":
                marker.setTitle("Robbery");
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                break;
            case "Rape(Cyan)":
                marker.setTitle("Rape");
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                break;
            case "Harassment(Azure)":
                marker.setTitle("Harassment");
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                break;
            case "Discrimination(Blue)":
                marker.setTitle("Discrimination");
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                break;
            case "Abduction(Magenta)":
                marker.setTitle("Abduction");
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                break;
            default:
                marker.setTitle("Others");
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
                break;
        }
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

    @Override
    protected void destroyView() {

    }
}
