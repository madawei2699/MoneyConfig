package com.mdw.moneyconfig;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog.OnDateSetListener;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class AddFundActivity extends FragmentActivity implements OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    public static final String DATEPICKER_TAG = "datepicker";
    public static final String TIMEPICKER_TAG = "timepicker";

    /**
     * 进度条
     */
    private ProgressDialog pd;

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
    private String buyMoney="";

    /**
     * 收费模式,0-前端,1-后端
     */
    private int fundInsuranceType = 0;

    /**
     * 费率
     */
    private String fundRate="";

    /**
     * 购买日期
     */
    private String buyDate;

    /**
     * 购买时间
     */
    private String buyTime;

    /**
     * 购买价格
     */
    private String buyPrice;

    /**
     * 日期按钮
     */
    Button buttonDate;

    // 创建ContentValues对象
    ContentValues values;
	
	//基金收费模式
    private String[] fundInsuranceRate = new String[] {"前端","后端"};

    private Handler handler = new Handler() {
        // 处理子线程给我们发送的消息
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what){
                case Constant.SEARCHSERVICEOK:
                    pd.dismiss();// 关闭ProgressDialog
                    // 给webview展示内容
                    showFundRateDialog(msg.getData().getString("html"));
                    break;
                case Constant.FUNDPRICEOK:
                    pd.dismiss();// 关闭ProgressDialog
                    buyPrice = msg.getData().getString("jjjz");
                    // 如果查询不到购买日期的历史净值，则提示用户无法添加基金
                    if(!buyPrice.equals("")){
                        wrapData();
                        // 创建了一个DatabaseHelper对象，只执行这句话是不会创建或打开连接的
                        DatabaseHelper dbHelper = new DatabaseHelper(AddFundActivity.this, "moneyconfig_db");
                        // 只有调用了DatabaseHelper的getWritableDatabase()方法或者getReadableDatabase()方法之后，才会创建或打开一个连接
                        SQLiteDatabase sqliteDatabase = dbHelper.getWritableDatabase();
                        // 查询fund_base表是否有此基金实时数据
                        Cursor c = sqliteDatabase.rawQuery("select fundCode from fund_base where fundCode ='"
                                + fundCode + "'",null);
                        // 如果无此基金代码，则新增一条记录
                        if(c.getCount()==0){
                            sqliteDatabase.execSQL("insert into fund_base(fundCode) values ('"
                                    +fundCode +"')");
                        }
                        // 关闭游标
                        c.close();
                        // 在fund_buyInfo中插入数据
                        sqliteDatabase.insert("fund_buyInfo", null, values);
                        // 关闭数据库
                        sqliteDatabase.close();
                        // 打开主界面
                        Intent MainActivityIntent = new Intent();
                        //MainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        //MainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        MainActivityIntent.setClass(AddFundActivity.this,MainActivity.class);
                        startActivity(MainActivityIntent);
                        finish();
                    }else {
                        Toast toast=Toast.makeText(AddFundActivity.this,
                                getResources().getString(R.string.errorFindFundRate), Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    break;
                default:
                    break;
            }
        };
    };

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
        // 初始化日期按钮的值
        buttonDate = (Button)findViewById(R.id.buttonDate);
        // 设置日期格式
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = sDateFormat.format(new java.util.Date());
        buttonDate.setText(date);
        // 初始化购买日期值
        this.buyDate = date;

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
        EditText editFundInsuranceRate = (EditText) findViewById(R.id.editFundInsuranceRate);
        // 开放式基金前缀为"of"
        fundCode = "of" + editFundCode.getText().toString();
        buyMoney = editFundMoney.getText().toString();
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
        // 设置日期按钮值
        buttonDate.setText(buyDate);
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
                // 用正则表达式判断输入基金代码是否正确
                if(fundCode.matches("^of\\d{6,6}")){
                    //开启线程，获取json，开始进行解析
                    if(fundCode.matches("^of\\d{6,6}")){
                        pd = ProgressDialog.show(AddFundActivity.this, "查询历史净值", "加载中，请稍后……");
                        FundPriceService fs = new FundPriceService(fundCode,buyDate,handler);
                        new Thread(fs).start();
                    }
                }else {
                    Toast toast=Toast.makeText(AddFundActivity.this,
                            getResources().getString(R.string.errorFundCode), Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;
            case R.id.searchRate:
                getEditValue();
                // 用正则表达式判断输入基金代码是否正确
                if(fundCode.matches("^of\\d{6,6}")){
                    // 给线程传递请求URL
                    queryHtml(Utils.getPropertiesURL("fundRateWeb")
                            +fundCode.replaceAll("of",""));
                }else {
                    Toast toast=Toast.makeText(AddFundActivity.this,
                            getResources().getString(R.string.errorFundCode), Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;
            default:
                break;
        }
        return true;
    }
    @Override
    public void onBackPressed()
    {
        //do whatever you want the 'Back' button to do
        //as an example the 'Back' button is set to start a new Activity named 'NewActivity'
        // 打开主界面
        Intent MainActivityIntent = new Intent();
        //MainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        //MainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MainActivityIntent.setClass(AddFundActivity.this,MainActivity.class);
        startActivity(MainActivityIntent);
        finish();

        return;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // TO DO

        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * 将要保存数据封装
     * @return
     */
    public void wrapData(){
        values = new ContentValues();
        values.put("fundCode", fundCode);
        values.put("fundInsuranceType", fundInsuranceType);
        values.put("buyPrice",buyPrice);
        values.put("buyDate", buyDate);
        if("".equals(fundRate)){
            values.put("fundRate", "0");
        }else{
            values.put("fundRate", fundRate);
        }
        if("".equals(buyMoney)){
            values.put("buyMoney", "0");
        }else{
            values.put("buyMoney", buyMoney);
        }
        // 如果是前端收费,则计算购买数量
        // 净申购金额＝申购金额/（1＋申购费率）
        // 申购费用＝申购金额－净申购金额
        // 申购份额＝净申购金额/T日申购价格
        // 注：净申购金额及申购份额的计算结果以四舍五入的方法保留小数点后两位。
        if(fundInsuranceType == 0 && (!fundRate.equals(""))){
            Double jsgje = Double.parseDouble(buyMoney)/(1+Double.parseDouble(fundRate)/100);
            Double sgfy = Double.parseDouble(buyMoney) - jsgje;
            Double sgfe = jsgje/Double.parseDouble(buyPrice);
            values.put("poundage",String.format("%.2f", sgfy));
            values.put("buyAmount",String.format("%.2f", sgfe));
        }else if(fundInsuranceType == 1){
            Double hdsgfe = Double.parseDouble(buyMoney)/Double.parseDouble(buyPrice);
            values.put("poundage","0");
            values.put("buyAmount",String.format("%.2f", hdsgfe));
        }
    }

    /**
     * 解析url,获取网页并使用Jsoup解析
     * @param url
     * @return
     */
    public void queryHtml(String url){
        pd = ProgressDialog.show(AddFundActivity.this, "查询数据", "加载中，请稍后……");

        //开启线程，连接主页，获取html，开始进行解析
        SearchService ss = new SearchService(url,handler);
        new Thread(ss).start();
    }

    /**
     * 显示基金费率对话框
     * @param html
     */
    public void showFundRateDialog(String html){
        AlertDialog.Builder fundRateDialog = new AlertDialog.Builder(this);
        fundRateDialog.setTitle("基金费率");
        WebView wv = new WebView(this);
        // 加载费率页面
        wv.loadDataWithBaseURL(Utils.getPropertiesURL("baseAssetsURL"),html,
                Utils.getPropertiesURL("mime"),
                Utils.getPropertiesURL("encodeFundRate"),null);
        fundRateDialog.setView(wv);
        fundRateDialog.setNegativeButton("关闭", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        fundRateDialog.show();
    }

}
