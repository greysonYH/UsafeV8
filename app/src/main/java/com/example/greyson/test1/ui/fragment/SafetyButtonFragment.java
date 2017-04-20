package com.example.greyson.test1.ui.fragment;

import android.app.NotificationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.greyson.test1.R;
import com.example.greyson.test1.core.TimerListener;
import com.example.greyson.test1.ui.base.BaseFragment;
import com.example.greyson.test1.widget.CountDownView;

/**
 * Created by greyson on 28/3/17.
 */

public class SafetyButtonFragment extends BaseFragment implements View.OnClickListener{
    private LinearLayout mLLStartButton;
    private LinearLayout mLLCancelButton;
    private LinearLayout mLLSettingButton;
    private CountDownView cdv;

    @Override
    protected View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_safetybutton, container, false);
        mLLStartButton = (LinearLayout) view.findViewById(R.id.ll_startbutton);
        mLLCancelButton = (LinearLayout) view.findViewById(R.id.ll_cancelbutton);
        mLLSettingButton = (LinearLayout) view.findViewById(R.id.ll_settingbutton);
        cdv = (CountDownView) view.findViewById(R.id.countdownview);
        cdv.setInitialTime(10000); // Initial time of 30 seconds.
        cdv.setListener(new TimerListener() {
            @Override
            public void timerElapsed() {
                cdv.stop();
            }
        });
        //cdv.start();
        //cdv.stop();
        //cdv.reset();
        return view;
    }

    @Override
    protected void initData() {

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
        cdv.start();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
    }

    private void settingButton() {

    }

    @Override
    protected void destroyView() {

    }
}
