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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog.OnDateSetListener;
import com.mdw.moneyconfig.database.model.fund.FundBuyInfo;
import com.mdw.moneyconfig.utils.Constant;
import com.mdw.moneyconfig.database.DataSource;
import com.mdw.moneyconfig.database.DatabaseHelper;
import com.mdw.moneyconfig.MainActivity;
import com.mdw.moneyconfig.utils.MyApplication;
import com.mdw.moneyconfig.R;
import com.mdw.moneyconfig.utils.Utils;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public class RedeemFundActivity extends FragmentActivity implements OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    public static final String DATEPICKER_TAG = "datepicker";
    public static final String TIMEPICKER_TAG = "timepicker";

    /**
     * 进度条
     */
    private ProgressDialog pd;

    /**
     * 基金名称
     */
    private String fundName;

    /**
     * 基金持仓
     */
    private String position;

    /**
     * 基金代码
     */
    private String fundCode;

    /**
     * 收费模式
     */
    private Integer fundInsuranceType;

    /**
     * 赎回金额
     */
    private String redeemMoney="";

    /**
     * 赎回费率
     */
    private String fundRedeemRate="";

    /**
     * 赎回日期
     */
    private String redeemDate;

    /**
     * 时间
     */
    private String time;

    /**
     * 赎回价格
     */
    private String redeemPrice;

    /**
     * 赎回数量
     */
    private String redeemAmount;

    /**
     * 后端费率
     */
    private String fundBackRate;

    /**
     * 日期按钮
     */
    Button buttonDate;

    // 创建ContentValues对象
    ContentValues values;
	
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
                    redeemPrice = msg.getData().getString("jjjz");
                    // 如果查询不到购买日期的历史净值，则提示用户无法添加基金
                    if(!redeemPrice.equals("")){
                        wrapData();
                        // 创建了一个DatabaseHelper对象，只执行这句话是不会创建或打开连接的
                        DatabaseHelper dbHelper = new DatabaseHelper(RedeemFundActivity.this, "moneyconfig_db");
                        // 只有调用了DatabaseHelper的getWritableDatabase()方法或者getReadableDatabase()方法之后，才会创建或打开一个连接
                        SQLiteDatabase sqliteDatabase = dbHelper.getWritableDatabase();
                        // 在fund_redeem中插入数据
                        sqliteDatabase.insert("fund_redeem", null, values);
                        // 在基金实时数据表查询基金价格及涨幅
                        Cursor cursor = sqliteDatabase.query("fund_base", new String[] { "fundCode",
                                        "price", "updown"},
                                "fundCode='"+fundCode+"'", null, null, null, null);
                        cursor.moveToFirst();
                        String price = cursor.getString(cursor.getColumnIndex("price"));
                        String updown = cursor.getString(cursor.getColumnIndex("updown"));
                        // 计算基金概览数据并封装结果数据
                        ContentValues cv = calcFundSumRedeem(fundCode,price,updown);
                        //更新基金概率表
                        sqliteDatabase.update("fund_sum",cv,"fundCode='"+fundCode+"'",null);
                        //关闭游标
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
                        MainActivityIntent.setClass(RedeemFundActivity.this,MainActivity.class);
                        MainActivityIntent.putExtras(bundle);
                        startActivity(MainActivityIntent);
                        finish();
                    }else {
                        Toast toast=Toast.makeText(RedeemFundActivity.this,
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
        setContentView(R.layout.redeem_fund_layout);

        View addView = getLayoutInflater().inflate(R.layout.titlebar_redeem, null);
        getActionBar().setCustomView(addView);
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setDisplayShowCustomEnabled(true);
        fundCode = getIntent().getExtras().getString("fundCode");
        fundName = getIntent().getExtras().getString("fundName");
        position = getIntent().getExtras().getString("position");
        TextView ttr = (TextView) addView.findViewById(R.id.title_text_redeem);
        // 设置标题内容为基金名字+代码
        ttr.setText(fundName+"["+fundCode+"]");
        fundCode = "of" + fundCode;
        EditText etfra = (EditText) findViewById(R.id.editFundRedeemAmount);
        etfra.setHint("不超过"+position+"份");
        List<FundBuyInfo> fbi = DataSource.queryFundBuyInfoByCode(fundCode);
        //如果是前端收费则隐藏后端申购费用输入框
        fundInsuranceType = fbi.get(0).getFundInsuranceType();
        if(fundInsuranceType==0){
            findViewById(R.id.tableRowBackRate).setVisibility(View.GONE);
        }

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
        this.redeemDate = date;

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
        EditText editFundRedeemAmount = (EditText) findViewById(R.id.editFundRedeemAmount);
        EditText editFundInsuranceRate = (EditText) findViewById(R.id.editFundInsuranceRate);
        EditText editFundRedeemRate = (EditText) findViewById(R.id.editFundRedeemRate);
        redeemAmount = editFundRedeemAmount.getText().toString();
        fundBackRate = editFundInsuranceRate.getText().toString();
        fundRedeemRate = editFundRedeemRate.getText().toString();
    }
	
    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        redeemDate = Integer.toString(year)+"-"+Integer.toString(month+1)+"-"+Integer.toString(day);
        // 设置日期按钮值
        buttonDate.setText(redeemDate);
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.fund_redeem, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.saveFundRedeem:
                getEditValue();
                if(Double.parseDouble(redeemAmount)>Double.parseDouble(position)){
                    //提示赎回份数不能大于持仓份数
                    Toast toast=Toast.makeText(RedeemFundActivity.this,
                            getResources().getString(R.string.errorRedeemAmount), Toast.LENGTH_SHORT);
                    toast.show();
                    break;
                }
                // 用正则表达式判断输入基金代码是否正确
                if(fundCode.matches("^of\\d{6,6}")){
                    pd = ProgressDialog.show(RedeemFundActivity.this, "查询历史净值", "加载中，请稍后……");
                    FundPriceService fs = new FundPriceService(fundCode,redeemDate,handler);
                    new Thread(fs).start();
                }else {
                    Toast toast=Toast.makeText(RedeemFundActivity.this,
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
                    Toast toast=Toast.makeText(RedeemFundActivity.this,
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
        values.put("fundInsuranceType", fundInsuranceType);
        values.put("redeemPrice",redeemPrice);
        values.put("redeemDate", redeemDate);
        if("".equals(redeemAmount)){
            values.put("redeemAmount", "0");
        }else{
            values.put("redeemAmount", redeemAmount);
        }
        if("".equals(fundBackRate)){
            values.put("fundBackRate", "0");
        }else{
            values.put("fundBackRate", fundBackRate);
        }
        if("".equals(fundRedeemRate)){
            values.put("fundRedeemRate", "0");
        }else{
            values.put("fundRedeemRate", fundRedeemRate);
        }
        // 如果是前端收费,则如下公式
        // 赎回总额=赎回份额×赎回当日基金份额净值
        // 赎回费用=赎回总额×赎回费率
        // 赎回金额=赎回总额－赎回费用
        // 如果是后端收费,则如下公式
        // 赎回总额＝赎回份额×赎回当日基金份额净值
        // 后端申购费用＝赎回份额×申购当日基金份额净值×后端申购费率
        // 赎回费用=赎回总额×赎回费率
        // 赎回金额=赎回总额－赎回费用
        Double shze,shfy,shje;
        Double hdsgfy = 0.0;
        shze = Double.parseDouble(redeemAmount)*Double.parseDouble(redeemPrice);
        shfy = shze*Double.parseDouble(fundRedeemRate)/100;
        if((fundInsuranceType == 1) && (!fundBackRate.equals(""))){
            List<FundBuyInfo> fbis = DataSource.queryFundBuyInfoByCode(fundCode);
            //以购买日期排序，购买早的排在前面
            Collections.sort(fbis);
            //遍历列表
            Iterator it = fbis.iterator();
            Double avaliableRedeem = 0.0;
            Double ra = Double.parseDouble(redeemAmount);
            Double fundBuyInfo_RedeemAmount = 0.0;
            FundBuyInfo fbi;
            // 创建了一个DatabaseHelper对象，只执行这句话是不会创建或打开连接的
            DatabaseHelper dbHelper = new DatabaseHelper(RedeemFundActivity.this, "moneyconfig_db");
            // 只有调用了DatabaseHelper的getWritableDatabase()方法或者getReadableDatabase()方法之后，才会创建或打开一个连接
            SQLiteDatabase sqliteDatabase = dbHelper.getWritableDatabase();
            while ((ra>0)&&it.hasNext()){
                ContentValues v = new ContentValues();
                fbi = (FundBuyInfo)it.next();
                avaliableRedeem = Double.parseDouble(fbi.getBuyAmount())
                        -Double.parseDouble(fbi.getRedeemAmount());
                if(avaliableRedeem!=0){
                    if(avaliableRedeem<ra){
                        ra -= avaliableRedeem;
                        fundBuyInfo_RedeemAmount = Double.parseDouble(fbi.getBuyAmount());
                    }else {
                        fundBuyInfo_RedeemAmount = Double.parseDouble(fbi.getRedeemAmount())
                                + ra;
                        ra=0.0;
                    }
                    //后端申购费率
                    hdsgfy += fundBuyInfo_RedeemAmount*Double.parseDouble(fbi.getBuyPrice())*Double.parseDouble(fundBackRate)/100;
                    v.put("redeemAmount",String.valueOf(fundBuyInfo_RedeemAmount));
                    sqliteDatabase.update("fund_buyInfo",v,"_id='"+String.valueOf(fbi.getId())+"'",null);
                }
            }
            //关闭数据库
            sqliteDatabase.close();
            shje = shze - shfy - hdsgfy;
            values.put("poundage",String.format("%.2f", shfy+hdsgfy));
            values.put("redeemMoney",String.format("%.2f", shje));
        }else if(fundInsuranceType == 0){
            shje = shze -shfy;
            values.put("poundage",shfy);
            values.put("redeemMoney",String.format("%.2f", shje));
        }
    }

    /**
     * 解析url,获取网页并使用Jsoup解析
     * @param url
     * @return
     */
    public void queryHtml(String url){
        pd = ProgressDialog.show(RedeemFundActivity.this, "查询数据", "加载中，请稍后……");

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
    public ContentValues calcFundSumRedeem(String fundCode,String price,String updown){
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
        Cursor fundSumCursor = sqliteDatabase.query("fund_sum", new String[]{"fund_Position",
                        "buyMoneySum"},
                "fundCode='"+fundCode+"'", null, null, null, null);
        fundSumCursor.moveToFirst();
        //计算基金本金、手续费、持仓
        buyMoneySum = Double.parseDouble(fundSumCursor.getString(
                fundSumCursor.getColumnIndex("buyMoneySum")));
        buyAmountSum = Double.parseDouble(fundSumCursor.getString(
                fundSumCursor.getColumnIndex("fund_Position"))) - Double.parseDouble(redeemAmount);

        Cursor fundRedeemCursor = sqliteDatabase.query("fund_redeem", null,
                "fundCode='"+fundCode+"'", null, null, null, null);

        //计算基金累计赎回资金
        while (fundRedeemCursor.moveToNext()){
            fund_RedeemMoneySum += Double.parseDouble(fundRedeemCursor.getString(
                    fundRedeemCursor.getColumnIndex("redeemMoney")));
        }

        Cursor fundBonusCursor = sqliteDatabase.query("fund_bonus", null,
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
        //封装返回数据
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
