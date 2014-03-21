package com.mdw.moneyconfig.fund;

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
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog.OnDateSetListener;
import com.mdw.moneyconfig.MainActivity;
import com.mdw.moneyconfig.R;
import com.mdw.moneyconfig.database.DatabaseHelper;
import com.mdw.moneyconfig.utils.Constant;
import com.mdw.moneyconfig.utils.MyApplication;
import com.mdw.moneyconfig.utils.Utils;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import java.nio.DoubleBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class FundBonusActivity extends FragmentActivity implements OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    public static final String DATEPICKER_TAG = "datepicker";
    public static final String TIMEPICKER_TAG = "timepicker";

    /**
     * 进度条
     */
    private ProgressDialog pd;

    /**
	 * 基金分红模式：红利转投、现金分红
	 */
	private Spinner fundBonusSpinner;
	
	/**
	 * 基金分红适配器
	 */
	private ArrayAdapter<String> fundBonusAdapter;
	
	/**
	 * 默认选择红利转投
	 */
	static int fundBonusPosition = 0;

    /**
     * 基金代码
     */
    private String fundCode;

    /**
     * 基金名称
     */
    private String fundName;

    /**
     * 基金持仓
     */
    private String position;

    /**
     * 分红模式,0-红利转投,1-现金分红
     */
    private int fundBonusType = 0;

    /**
     * 每10份基金分红金额
     */
    private String bonusTenPerPrice="";

    /**
     * 分红日期
     */
    private String fundBonusDate;

    /**
     * 分红时间
     */
    private String buyTime;

    /**
     * 分红价格
     */
    private String fundBonusPrice;

    /**
     * 现金分红金额
     */
    private Double fundBonusMoney = 0.0;

    /**
     * 基金分红份额
     */
    private Double fundBonusAmount = 0.0;

    /**
     * 日期按钮
     */
    Button buttonDate;

    // 创建ContentValues对象
    ContentValues values;

	//基金收费模式
    private String[] fundInsuranceRate = new String[] {"红利转投","现金分红"};

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
                    fundBonusPrice = msg.getData().getString("jjjz");
                    // 如果查询不到购买日期的历史净值，则提示用户无法添加基金
                    if(!fundBonusPrice.equals("")){
                        //封装要添加的基金数据
                        wrapData();
                        // 创建了一个DatabaseHelper对象，只执行这句话是不会创建或打开连接的
                        DatabaseHelper dbHelper = new DatabaseHelper(FundBonusActivity.this, "moneyconfig_db");
                        // 只有调用了DatabaseHelper的getWritableDatabase()方法或者getReadableDatabase()方法之后，才会创建或打开一个连接
                        SQLiteDatabase sqliteDatabase = dbHelper.getWritableDatabase();
                        // 在fund_buyInfo中插入数据
                        sqliteDatabase.insert("fund_bonus", null, values);
                        // 在基金实时数据表查询基金价格及涨幅
                        Cursor cursor = sqliteDatabase.query("fund_base", new String[] { "fundCode",
                                        "price", "updown"},
                                "fundCode='"+fundCode+"'", null, null, null, null);
                        cursor.moveToFirst();
                        String price = cursor.getString(cursor.getColumnIndex("price"));
                        String updown = cursor.getString(cursor.getColumnIndex("updown"));
                        // 计算基金概览数据并封装结果数据
                        ContentValues cv = calcFundSumBonus(fundCode,price,updown);
                        sqliteDatabase.update("fund_sum",cv,"fundCode='"+fundCode+"'",null);
                        cursor.close();
                        // 关闭数据库
                        sqliteDatabase.close();
                        // 使用bundle更新基金界面
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("updateFund", true);
                        // 打开主界面
                        Intent MainActivityIntent = new Intent();
                        //MainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        MainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        MainActivityIntent.setClass(FundBonusActivity.this,MainActivity.class);
                        MainActivityIntent.putExtras(bundle);
                        startActivity(MainActivityIntent);
                        finish();
                    }else {
                        Toast toast=Toast.makeText(FundBonusActivity.this,
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
        setContentView(R.layout.fund_bonus_layout);

        View addView = getLayoutInflater().inflate(R.layout.titlebar_bonus, null);
        getActionBar().setCustomView(addView);
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setDisplayShowCustomEnabled(true);
        fundCode = getIntent().getExtras().getString("fundCode");
        fundName = getIntent().getExtras().getString("fundName");
        position = getIntent().getExtras().getString("position");
        TextView ttr = (TextView) addView.findViewById(R.id.title_text_fundBonus);
        // 设置标题内容为基金名字+代码
        ttr.setText(fundName+"["+fundCode+"]"+getResources().getString(R.string.titleFundBonus));
        fundCode = "of" + fundCode;

        fundBonusSpinner = (Spinner)findViewById(R.id.spinnerFundBonus);

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
        this.fundBonusDate = date;

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
        EditText editFundBonus = (EditText) findViewById(R.id.editFundBonus);
        bonusTenPerPrice = editFundBonus.getText().toString();

    }
	
	/*
     * 设置下拉框
     */
    private void setSpinner()
    {        
        //绑定适配器和值
    	fundBonusAdapter = new ArrayAdapter<String>(MyApplication.getInstance(),
                R.layout.myspinner,fundInsuranceRate);
    	fundBonusSpinner.setAdapter(fundBonusAdapter);
        fundBonusSpinner.setSelection(fundBonusPosition,true);  //设置默认选中项，此处为默认选中前端

        fundBonusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {

            // 表示选项被改变的时候触发此方法
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3)
            {
            	 TextView tv = (TextView)arg1;
                 //tv.setTextSize(15);
                 tv.setTextColor(getResources().getColor(R.color.black));    //设置颜色
                 tv.setGravity(android.view.Gravity.CENTER_HORIZONTAL);   //设置居中
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
                
            }
            
        });
        
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        fundBonusDate = Integer.toString(year)+"-"+Integer.toString(month+1)+"-"+Integer.toString(day);
        // 设置日期按钮值
        buttonDate.setText(fundBonusDate);
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        buyTime = Integer.toString(hourOfDay) + ":" + Integer.toString(minute);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.fund_bonus, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.saveFundBonus:
                getEditValue();
                if(bonusTenPerPrice.equals("")){
                    //提示分红金额不能为空
                    Toast toast=Toast.makeText(FundBonusActivity.this,
                            getResources().getString(R.string.errorBonusMoney), Toast.LENGTH_SHORT);
                    toast.show();
                    break;
                }
                // 用正则表达式判断输入基金代码是否正确
                if(fundCode.matches("^of\\d{6,6}")){
                    pd = ProgressDialog.show(FundBonusActivity.this, "查询历史净值", "加载中，请稍后……");
                    FundPriceService fs = new FundPriceService(fundCode,fundBonusDate,handler);
                    new Thread(fs).start();
                }else {
                    Toast toast=Toast.makeText(FundBonusActivity.this,
                            getResources().getString(R.string.errorFundCode), Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;
            case R.id.searchBonus:
                getEditValue();
                // 用正则表达式判断输入基金代码是否正确
                if(fundCode.matches("^of\\d{6,6}")){
                    // 给线程传递请求URL
                    queryHtml(Utils.getPropertiesURL("fundRateWeb")
                            +fundCode.replaceAll("of",""));
                }else {
                    Toast toast=Toast.makeText(FundBonusActivity.this,
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
        //Intent MainActivityIntent = new Intent();
        //MainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        //MainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //MainActivityIntent.setClass(AddFundActivity.this,MainActivity.class);
        //startActivity(MainActivityIntent);
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
        values.put("bonusType", fundBonusType);
        values.put("buyPrice",fundBonusPrice);
        values.put("bonusDate", fundBonusDate);
        if("".equals(bonusTenPerPrice)){
            values.put("bonusTenPerPrice", "0");
        }else{
            values.put("bonusTenPerPrice", bonusTenPerPrice);
        }
        //分红金额
        Double fhje = Double.parseDouble(position)*Double.parseDouble(bonusTenPerPrice)/10;
        Double fhjjfe = 0.0;
        Double xjfhje = 0.0;
        if(fundBonusType == 0){
            //分红基金份额
            fhjjfe = fhje/Double.parseDouble(fundBonusPrice);
            //现金分红金额
            xjfhje = 0.0;
        }else {
            //分红基金份额
            fhjjfe = 0.0;
            //现金分红金额
            xjfhje = fhje;
        }
        //初始化分红基金份额、现金分红金额
        fundBonusAmount = fhjjfe;
        fundBonusMoney = xjfhje;
        values.put("bonusMoney",String.valueOf(xjfhje));
        values.put("bonusAmount",String.valueOf(fhjjfe));
    }

    /**
     * 解析url,获取网页并使用Jsoup解析
     * @param url
     * @return
     */
    public void queryHtml(String url){
        pd = ProgressDialog.show(FundBonusActivity.this, "查询数据", "加载中，请稍后……");

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

    /**
     * 计算基金今日盈亏、累计盈亏、盈亏幅度、市值、持仓
     * 累计盈亏=基金累计赎回资金-累计购买资金+当前净值*持仓份额+累计现金分红
     * @return
     */
    public ContentValues calcFundSumBonus(String fundCode,String price,String updown){
        Double doublePrice = Double.parseDouble(price); //现价
        Double doubleUpdown = Double.parseDouble(updown); //涨跌
        Double buyAmountSum = 0.0; //基金持仓
        Double poundageSum = 0.0;  //手续费总和
        Double buyMoneySum = 0.0;  //初始金额总和
        Double fund_ProfitOrLossToday = 0.0; //今日盈亏
        Double fund_ProfitOrLossSum = 0.0; //累计盈亏
        Double fund_ProfitOrLossRate = 0.0; //盈亏幅度
        Double fund_MarketValue = 0.0; //市值
        Double fund_Position = 0.0; //持仓
        Double fund_RedeemMoneySum = 0.0; //基金累计赎回资金
        Double fund_BonusMoneySum = 0.0; //累计现金分红

        //从数据库取基金数据
        DatabaseHelper dbHelper = new DatabaseHelper(MyApplication.getInstance(),
                "moneyconfig_db", 2);
        // 得到一个只读的SQLiteDatabase对象
        SQLiteDatabase sqliteDatabase = dbHelper.getReadableDatabase();

        //查询基金概览表是否有此基金
        Cursor fundSumCursor = sqliteDatabase.query("fund_sum", null,
                "fundCode='"+fundCode+"'", null, null, null, null);
        fundSumCursor.moveToFirst();
        //计算基金本金、手续费、持仓
        buyMoneySum = Double.parseDouble(fundSumCursor.getString(
                fundSumCursor.getColumnIndex("buyMoneySum")));
        //基金持仓=原持仓+红利转投份额
        buyAmountSum = Double.parseDouble(fundSumCursor.getString(
                fundSumCursor.getColumnIndex("fund_Position"))) + fundBonusAmount;

        Cursor fundRedeemCursor = sqliteDatabase.query("fund_redeem", new String[]{"redeemMoney"},
                "fundCode='"+fundCode+"'", null, null, null, null);
        //计算基金累计赎回资金
        while (fundRedeemCursor.moveToNext()){
            fund_RedeemMoneySum += Double.parseDouble(fundRedeemCursor.getString(
                    fundRedeemCursor.getColumnIndex("redeemMoney")));
        }

        Cursor fundBonusCursor = sqliteDatabase.query("fund_bonus", new String[]{"bonusMoney"},
                "fundCode='"+fundCode+"'", null, null, null, null);
        //计算累计现金分红
        while (fundBonusCursor.moveToNext()){
            fund_BonusMoneySum += Double.parseDouble(fundBonusCursor.getString(
                    fundBonusCursor.getColumnIndex("bonusMoney")));
        }

        fund_Position = buyAmountSum;
        fund_ProfitOrLossToday = doubleUpdown*fund_Position;
        fund_MarketValue = doublePrice*fund_Position;
        //累计盈亏=基金累计赎回资金-累计购买资金+当前净值*持仓份额+累计现金分红
        fund_ProfitOrLossSum = fund_MarketValue-buyMoneySum+fund_RedeemMoneySum+fund_BonusMoneySum;
        fund_ProfitOrLossRate = fund_ProfitOrLossSum/buyMoneySum;
        // 封装返回数据
        ContentValues value = new ContentValues();
        value.put("fund_Position",String.format("%.2f",fund_Position));
        value.put("fund_ProfitOrLossToday",String.format("%.2f",fund_ProfitOrLossToday));
        value.put("fund_MarketValue",String.format("%.2f",fund_MarketValue));
        value.put("fund_ProfitOrLossSum",String.format("%.2f",fund_ProfitOrLossSum));
        value.put("fund_ProfitOrLossRate",String.format("%.2f",fund_ProfitOrLossRate));
        value.put("buyMoneySum",String.format("%.2f",buyMoneySum));
        value.put("cashBonusSum",String.format("%.2f",fund_BonusMoneySum));
        value.put("redeemMoneySum",String.format("%.2f",fund_RedeemMoneySum));
        // 关闭数据库
        sqliteDatabase.close();
        // 关闭游标
        fundRedeemCursor.close();
        fundBonusCursor.close();
        fundSumCursor.close();
        return value;
    }

}
