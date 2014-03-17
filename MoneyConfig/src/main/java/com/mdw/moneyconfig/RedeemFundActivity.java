package com.mdw.moneyconfig;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
                        // 在fund_buyInfo中插入数据
                        sqliteDatabase.insert("fund_buyInfo", null, values);
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

}
