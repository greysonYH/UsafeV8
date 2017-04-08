package com.example.greyson.test1.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.greyson.test1.R;
import com.example.greyson.test1.ui.base.BaseFragment;

/**
 * Created by greyson on 28/3/17.
 */

public class SafetyButtonFragment extends BaseFragment implements View.OnClickListener{
    private LinearLayout mLLStartButton;
    private LinearLayout mLLCancelButton;
    private LinearLayout mLLSettingButton;

    @Override
    protected View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_safetybutton, container, false);
        mLLStartButton = (LinearLayout) view.findViewById(R.id.ll_startbutton);
        mLLCancelButton = (LinearLayout) view.findViewById(R.id.ll_cancelbutton);
        mLLSettingButton = (LinearLayout) view.findViewById(R.id.ll_settingbutton);
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

    }

    @Override
    protected void destroyView() {

    }
}
