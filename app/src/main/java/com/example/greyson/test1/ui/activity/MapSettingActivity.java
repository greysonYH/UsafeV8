package com.example.greyson.test1.ui.activity;

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

/**
 * Created by greyson on 8/4/17.
 */

public class MapSettingActivity extends BaseActivity implements View.OnClickListener,
        AdapterView.OnItemSelectedListener {
    private LinearLayout mLLSavePin;
    //private LinearLayout mLLChangePin;
    private LinearLayout mLLAddNote;

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
        //mLLChangePin = (LinearLayout) findViewById(R.id.ll_changepin);
        mLLAddNote = (LinearLayout) findViewById(R.id.ll_addnote);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initEvent() {
        mLLSavePin.setOnClickListener(this);
        //mLLChangePin.setOnClickListener(this);
        mLLAddNote.setOnClickListener(this);
    }

    @Override
    protected void destroyView() {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        Toast.makeText(this, parent.getSelectedItem().toString(),Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_savepin:
                break;
            //case R.id.ll_changepin:
                //break;
            case R.id.ll_addnote:
                break;
        }
    }
}
