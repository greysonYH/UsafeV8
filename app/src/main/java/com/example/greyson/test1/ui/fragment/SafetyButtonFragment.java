package com.example.greyson.test1.ui.fragment;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.greyson.test1.R;
import com.example.greyson.test1.core.TimerListener;
import com.example.greyson.test1.ui.activity.MainActivity;
import com.example.greyson.test1.ui.base.BaseFragment;
import com.example.greyson.test1.widget.CountDownView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by greyson on 28/3/17.
 */

public class SafetyButtonFragment extends BaseFragment implements View.OnClickListener{
    private static final int RESULT_PICK_CONTACT = 111;
    private static final int REQUEST_SEND_SMS = 222;
    private LinearLayout mLLStartButton;
    private LinearLayout mLLCancelButton;
    private LinearLayout mLLSettingButton;
    private TextView mTVContactName;
    private TextView mTVContactNumber;
    private CountDownView cdv;

    private String phoneNumber;

    @Override
    protected View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_safetybutton, container, false);
        mLLStartButton = (LinearLayout) view.findViewById(R.id.ll_startbutton);
        mLLCancelButton = (LinearLayout) view.findViewById(R.id.ll_cancelbutton);
        mLLSettingButton = (LinearLayout) view.findViewById(R.id.ll_settingbutton);
        mTVContactName = (TextView) view.findViewById(R.id.tv_contactName);
        mTVContactNumber = (TextView) view.findViewById(R.id.tv_contactPhone);
        cdv = (CountDownView) view.findViewById(R.id.countdownview);
        cdv.setInitialTime(5000); // Initial time of 5 seconds.
        cdv.setListener(new TimerListener() {
            @Override
            public void timerElapsed() {

                cdv.stop();
                checkSMSPermission();
                //sendMessageToContact();
            }
        });
        return view;
    }
    private void checkSMSPermission() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.SEND_SMS)) {

            } else {
                requestPermissions(new String[]{Manifest.permission.SEND_SMS}, REQUEST_SEND_SMS);
            }
        }else {sendMessageToContact();}
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_SEND_SMS:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sendMessageToContact();}
            }break;
        }
    }
    private boolean checkContactNotEmpty() {
        String[] ePhoneList = mTVContactNumber.getText().toString().split(":");
        String[] eNameList = mTVContactName.getText().toString().split(":");
        if (ePhoneList.length == 1){
            Toast.makeText(mContext, "You need choose contact first" , Toast.LENGTH_LONG).show();
            return false;
        }
        saveLastContact(eNameList[1],ePhoneList[1]);
        return true;
    }
    private boolean saveLastContact(String name, String phone) {
        SharedPreferences preferences = mContext.getSharedPreferences("LastContact",MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("contact", name + "," + phone);
        editor.commit();
        return true;
    }
    private void sendMessageToContact() {
        //http://maps.google.com/maps?q=-37.886256,145.0543715
        String[] ePhoneList = mTVContactNumber.getText().toString().split(":");
        if (checkContactNotEmpty()){
            SharedPreferences preferences = mContext.getSharedPreferences("LastLocation",MODE_PRIVATE);
            String lastLocation = preferences.getString("last location",null);
            String baseMapUrl = "http://maps.google.com/maps?q=";
            String eMessage = "This is a emergency message, please call me first, press this link to see my last location: "
                    + baseMapUrl + lastLocation;
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(ePhoneList[1], null, eMessage, null, null);
            Toast.makeText(mContext, "SMS sent.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void initData() {
        SharedPreferences preferences = mContext.getSharedPreferences("LastContact",MODE_PRIVATE);
        String lastContact = preferences.getString("contact",null);
        if (lastContact == null) {
            Toast.makeText(mContext, "Emergency Contact is Empty.", Toast.LENGTH_LONG).show();
            return;
        }
        mTVContactName.setText("Contact Name:" + lastContact.split(",")[0]);
        mTVContactNumber.setText("Contact Number:" + lastContact.split(",")[1]);
    }

    @Override
    protected void initEvent() {
        mLLStartButton.setOnClickListener(this);
        mLLCancelButton.setOnClickListener(this);
        mLLSettingButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        mLLStartButton.setSelected(false);
        mLLCancelButton.setSelected(false);
        mLLSettingButton.setSelected(false);
        switch (v.getId()) {
            case R.id.ll_startbutton:
                mLLStartButton.setSelected(true);
                startTimer();
                break;
            case R.id.ll_cancelbutton:
                mLLCancelButton.setSelected(true);
                resetTimer();
                break;
            case R.id.ll_settingbutton:
                mLLSettingButton.setSelected(true);
                settingButton();
                break;
        }
    }

    private void resetTimer() {
        cdv.reset();
    }

    private void startTimer() {
        if(checkContactNotEmpty()) {
            cdv.start();
            startNotification();
        }
    }

    private void startNotification() {
        if(checkNotificaionExsist())
            return;
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.putExtra("notification",0);
        PendingIntent pIntent = PendingIntent.getActivity(mContext, (int) System.currentTimeMillis(), intent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intent1 = new Intent(mContext, MainActivity.class);
        intent1.putExtra("notification",1);
        PendingIntent pIntent1 = PendingIntent.getActivity(mContext, (int) System.currentTimeMillis(), intent1, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intent2 = new Intent(mContext, MainActivity.class);
        intent2.putExtra("notification",2);
        PendingIntent pIntent2 = PendingIntent.getActivity(mContext, (int) System.currentTimeMillis(), intent2, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action action1 = new NotificationCompat.Action.Builder(R.mipmap.ic_launcher,"Start Timer",pIntent1).build();
        NotificationCompat.Action action2 = new NotificationCompat.Action.Builder(R.mipmap.ic_launcher,"Reset Timer",pIntent2).build();

        Notification n  = new NotificationCompat.Builder(mContext)
                .setContentTitle("Welcome to use U-Safe")
                .setContentText("Click to enter application")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .setOngoing(true)
                .setTicker("Start Countdown")
                .setWhen(System.currentTimeMillis())
                //.setUsesChronometer(true)
                .setAutoCancel(false)
                .addAction(action1)
                .addAction(action2)
                .build();
        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify("usafe",666,n);
    }

    private boolean checkNotificaionExsist() {
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        StatusBarNotification[] s = notificationManager.getActiveNotifications();///Min 23 API
        int nLength = s.length;
        for (int i = 0;i< nLength; i++) {
            if(s[i].getTag().equals("usafe"))
                return true;
        }
        return false;
    }
    private void settingButton() {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RESULT_PICK_CONTACT:
                    contactPicked(data);
                    break;
            }
        } else {
            Log.e("MainActivity", "Failed to pick contact");
        }
    }

    private void contactPicked(Intent data) {
        Cursor cursor = null;
        try {
            String contactPhone = "" ;
            String contactName = "";
            Uri uri = data.getData();
            cursor = mContext.getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            int  phoneIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            int  nameIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            contactPhone = cursor.getString(phoneIndex);
            contactName = cursor.getString(nameIndex);
            mTVContactName.setText("Contact Name: " + contactName);
            mTVContactNumber.setText("Contact Phone: " + contactPhone);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void destroyView() {

    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle b = getArguments();
        if (b != null) {
            int extra = b.getInt("notification");
            switch (extra) {
                case 1:
                    startTimer();
                    break;
                case 2:
                    resetTimer();
                    break;
            }
        }
    }
}
