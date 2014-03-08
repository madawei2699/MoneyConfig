package com.mdw.moneyconfig;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.view.View.OnClickListener;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog.OnDateSetListener;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import java.util.Calendar;

public class AddFundActivity extends FragmentActivity implements OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    public static final String DATEPICKER_TAG = "datepicker";
    public static final String TIMEPICKER_TAG = "timepicker";
	
	/**
	 * 基金收费模式：前端、后端
	 */
	private Spinner fundInsuranceRateSpinner; 
	
	/**
	 * 基金收费模式适配器
	 */
	private ArrayAdapter<String> fundInsuranceRateAdapter;
	
	/**
	 * 默认选择前端收费
	 */
	static int fundInsuranceRatePosition = 0;

    /**
     * 基金代码
     */
    private String fundCode;

    /**
     * 购买金额
     */
    private String buyMoney;

    /**
     * 购买数量
     */
    private String buyAmount;

    /**
     * 收费模式,0-前端,1-后端
     */
    private int fundInsuranceType = 0;

    /**
     * 费率
     */
    private String fundRate;

    /**
     * 购买日期
     */
    private String buyDate;

    /**
     * 购买时间
     */
    private String buyTime;
	
	//基金收费模式
    private String[] fundInsuranceRate = new String[] {"前端","后端"};

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_fund_layout);

        View addView = getLayoutInflater().inflate(R.layout.titlebar_add, null);
        getActionBar().setCustomView(addView);
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setDisplayShowCustomEnabled(true);

		fundInsuranceRateSpinner = (Spinner)findViewById(R.id.spinnerFrontBack);

		setSpinner();

        final Calendar calendar = Calendar.getInstance();

        final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);
        final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(this, calendar.get(Calendar.HOUR_OF_DAY) ,
                calendar.get(Calendar.MINUTE), false, false);

        findViewById(R.id.buttonDate).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                datePickerDialog.setYearRange(1985, 2028);
                datePickerDialog.show(getSupportFragmentManager(), DATEPICKER_TAG);
            }
        });

        findViewById(R.id.buttonTime).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                timePickerDialog.show(getSupportFragmentManager(), TIMEPICKER_TAG);
            }
        });

        if (savedInstanceState != null) {
            DatePickerDialog dpd = (DatePickerDialog) getSupportFragmentManager().findFragmentByTag(DATEPICKER_TAG);
            if (dpd != null) {
                dpd.setOnDateSetListener(this);
            }

            TimePickerDialog tpd = (TimePickerDialog) getSupportFragmentManager().findFragmentByTag(TIMEPICKER_TAG);
            if (tpd != null) {
                tpd.setOnTimeSetListener(this);
            }
        }

	}

    private void getEditValue(){
        EditText editFundCode = (EditText) findViewById(R.id.editFundCode);
        EditText editFundMoney = (EditText) findViewById(R.id.editFundMoney);
        EditText editFundAmount = (EditText) findViewById(R.id.editFundAmount);
        EditText editFundInsuranceRate = (EditText) findViewById(R.id.editFundInsuranceRate);
        fundCode = editFundCode.getText().toString();
        buyMoney = editFundMoney.getText().toString();
        buyAmount = editFundAmount.getText().toString();
        fundRate = editFundInsuranceRate.getText().toString();

    }
	
	/*
     * 设置下拉框
     */
    private void setSpinner()
    {        
        //绑定适配器和值
    	fundInsuranceRateAdapter = new ArrayAdapter<String>(MyApplication.getInstance(),
                R.layout.myspinner,fundInsuranceRate);
    	fundInsuranceRateSpinner.setAdapter(fundInsuranceRateAdapter);
    	fundInsuranceRateSpinner.setSelection(fundInsuranceRatePosition,true);  //设置默认选中项，此处为默认选中前端
        
    	fundInsuranceRateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {

            // 表示选项被改变的时候触发此方法
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3)
            {
            	 TextView tv = (TextView)arg1;
                 //tv.setTextSize(20);
                 tv.setTextColor(getResources().getColor(R.color.black));    //设置颜色
                 tv.setGravity(android.view.Gravity.CENTER_HORIZONTAL);   //设置居中
                 if(1 == position){
                     fundInsuranceType = 1;
                 }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
                
            }
            
        });
        
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        buyDate = Integer.toString(year)+"-"+Integer.toString(month+1)+"-"+Integer.toString(day);
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        buyTime = Integer.toString(hourOfDay) + ":" + Integer.toString(minute);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.addfund, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.saveFund:
                getEditValue();
                this.finish();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // TO DO

        return super.onPrepareOptionsMenu(menu);
    }

}
