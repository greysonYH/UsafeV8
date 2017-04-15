package com.example.greyson.test1.ui.activity;

import android.content.Intent;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.greyson.test1.R;
import com.example.greyson.test1.ui.base.BaseActivity;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

/**
 * Created by greyson on 8/4/17.
 */

public class MapSettingActivity extends BaseActivity implements View.OnClickListener,
        AdapterView.OnItemSelectedListener {
    private LinearLayout mLLSavePin;
    private LinearLayout mLLAddNote;
    private LinearLayout mLLDeletePin;
    private Intent fromIntent;
    private String toStr;
    @Override
    protected int getLayoutRes() {
        return R.layout.act_mapsetting;
    }

    @Override
    protected void initView() {
        Toolbar toolbar = findView(R.id.toolbar);
        AppCompatTextView tvTitle = findView(R.id.tv_title);
        tvTitle.setText("Map Setting");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        Spinner spinner = (Spinner) findViewById(R.id.spin_changepin);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.changepin_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        mLLSavePin = (LinearLayout) findViewById(R.id.ll_savepin);
        mLLDeletePin = (LinearLayout) findViewById(R.id.ll_deletepin);
        mLLAddNote = (LinearLayout) findViewById(R.id.ll_addnote);

        fromIntent = getIntent();
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initEvent() {
        mLLSavePin.setOnClickListener(this);
        mLLDeletePin.setOnClickListener(this);
        mLLAddNote.setOnClickListener(this);
    }

    @Override
    protected void destroyView() {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        toStr = parent.getSelectedItem().toString();
        //Toast.makeText(this, parent.getSelectedItem().toString(),Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            fromIntent.putExtra("color", toStr);
            setResult( 2 , fromIntent);
            finish();
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_savepin:
                savepin();
                break;
            case R.id.ll_deletepin:
                break;
            case R.id.ll_addnote:
                break;
        }
    }

    private void savepin() {

    }
}
