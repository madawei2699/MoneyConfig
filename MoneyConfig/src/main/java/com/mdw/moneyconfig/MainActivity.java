package com.mdw.moneyconfig;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mdw.moneyconfig.config.ConfigFragment;
import com.mdw.moneyconfig.database.DataService;
import com.mdw.moneyconfig.fund.FundAddActivity;
import com.mdw.moneyconfig.fund.FundFragment;
import com.mdw.moneyconfig.proportion.ProportionFragment;
import com.mdw.moneyconfig.stock.StockFragment;
import com.mdw.moneyconfig.utils.Constant;

public class MainActivity extends Activity {
	
	//默认基金icon为蓝色，0-基金，1-股票，2-资产比例，3-设置
	private int changeMenuIcon = 0;
	
	/**
	 * 用于展示基金的Fragment
	 */
	private FundFragment fundFragment;

	/**
	 * 用于展示股票的Fragment
	 */
	private StockFragment stockFragment;

	/**
	 * 用于展示资产比例的Fragment
	 */
	private ProportionFragment proportionFragment;

	/**
	 * 用于展示配置的Fragment
	 */
	private ConfigFragment configFragment;

	/**
	 * 用于对Fragment进行管理
	 */
	private FragmentManager fragmentManager;
	
	/**
	 * 用于对title_text进行管理
	 */
	private TextView titleText;
	
	/**
	 * 用于对title_add进行管理
	 */
	private ImageView addImage;

    /**
     * 进度条
     */
    private ProgressDialog pd;

    private Handler handler = new Handler() {
        // 处理子线程给我们发送的消息
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what){
                case Constant.NETWORKINVALID:
                    pd.dismiss();
                    setTabSelection(0);
                    Toast toast=Toast.makeText(MainActivity.this,
                            getResources().getString(R.string.errorNetworkInvaild), Toast.LENGTH_SHORT);
                    toast.show();
                    break;
                case Constant.INPUTISNULL:
                    pd.dismiss();
                    Toast toast2=Toast.makeText(MainActivity.this,
                            getResources().getString(R.string.errorInputIsNull), Toast.LENGTH_SHORT);
                    toast2.show();
                    break;
                case Constant.DATASERVICEOK:
                    pd.dismiss();// 关闭ProgressDialog
                    // 第一次启动时选中基金tab
                    setTabSelection(0);
                    break;
                default:
                    break;
            }
        };
    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		View addView = getLayoutInflater().inflate(R.layout.titlebar_main, null);
		getActionBar().setCustomView(addView);
		getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		// 初始化布局元素
		initViews();
		fragmentManager = getFragmentManager();
        // 判断是否需要更新基金数据
        if(true == getIntent().getExtras().getBoolean("updateFund")){
            // 重新刷新基金界面
            fundFragment = null;
            // 选中基金界面
            setTabSelection(0);
        }else if(true == getIntent().getExtras().getBoolean("updateAllData")){
            /* 显示ProgressDialog */
            pd = ProgressDialog.show(MainActivity.this, "资产配置", "刷新数据，请稍后……");
            // 后台数据更新
            new Thread(new DataService(handler)).start();
        }

	}

    private void initViews() {
		titleText = (TextView) findViewById(R.id.title_text);
		addImage = (ImageView) findViewById(R.id.title_add);
		addImage.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//通过获取titleText的值来判断addImage所处基金Fragment还是股票Fragment
				if(titleText.getText().equals(getResources().getString(R.string.fund))){
					Intent addFundIntent = new Intent();
                    addFundIntent.setClass(MainActivity.this,FundAddActivity.class);
                    startActivity(addFundIntent);
                    //finish();
				}else if(titleText.getText().equals(getResources().getString(R.string.stock))){
					
				}
			}
		});
	}
	
	private void setTabSelection(int index) {
		// 每次选中之前先清楚掉上次的选中状态
		clearSelection();
		// 开启一个Fragment事务
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		// 先隐藏掉所有的Fragment，以防止有多个Fragment显示在界面上的情况
		hideFragments(transaction);
		//0-基金 1-股票 2-资产比例 3-配置
		switch (index) {
		case 0:
			// set title_add visible
			addImage.setVisibility(View.VISIBLE);
			// set title_text to fund
			titleText.setText(R.string.fund);
			if (fundFragment == null) {
				// 如果fundFragment为空，则创建一个并添加到界面上
				fundFragment = new FundFragment();
				transaction.add(R.id.content, fundFragment);
			} else {
				// 如果fundFragment不为空，则直接将它显示出来
				transaction.show(fundFragment);
			}
			break;
		case 1:
			// set title_add visible
			addImage.setVisibility(View.VISIBLE);
			// set title_text to stock
			titleText.setText(R.string.stock);
			if (stockFragment == null) {
				// 如果stockFragment为空，则创建一个并添加到界面上
				stockFragment = new StockFragment();
				transaction.add(R.id.content, stockFragment);
			} else {
				// 如果stockFragment不为空，则直接将它显示出来
				transaction.show(stockFragment);
			}
			break;
		case 2:
			// set title_add invisible
			addImage.setVisibility(View.INVISIBLE);
			// set title_text to proportion
			titleText.setText(R.string.proportion);
			if (proportionFragment == null) {
				// 如果proportionFragment为空，则创建一个并添加到界面上
				proportionFragment = new ProportionFragment();
				transaction.add(R.id.content, proportionFragment);
			} else {
				// 如果proportionFragment不为空，则直接将它显示出来
				transaction.show(proportionFragment);
			}
			break;
		case 3:
			// set title_add invisible
			addImage.setVisibility(View.INVISIBLE);
			// set title_text to config
			titleText.setText(R.string.config);
			if (configFragment == null) {
				// 如果configFragment为空，则创建一个并添加到界面上
				configFragment = new ConfigFragment();
				transaction.add(R.id.content, configFragment);
			} else {
				// 如果SettingFragment不为空，则直接将它显示出来
				transaction.show(configFragment);
			}
			break;
		}
		transaction.commit();
		
	}

	private void hideFragments(FragmentTransaction transaction) {
		// TODO Auto-generated method stub
		if (fundFragment != null) {
			transaction.hide(fundFragment);
		}
		if (stockFragment != null) {
			transaction.hide(stockFragment);
		}
		if (proportionFragment != null) {
			transaction.hide(proportionFragment);
		}
		if (configFragment != null) {
			transaction.hide(configFragment);
		}
	}


	private void clearSelection() {
		// TODO Auto-generated method stub
		invalidateOptionsMenu();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override 
	public boolean onOptionsItemSelected(MenuItem item) { 
	    switch (item.getItemId()) { 
	        case R.id.fund:
	        	setTabSelection(0);
	        	changeMenuIcon = 0;
	        	clearSelection();
	        	item.setCheckable(true);
	        	break;
	        case R.id.stock:
	        	setTabSelection(1);
	        	changeMenuIcon = 1;
	        	clearSelection();
	        	item.setCheckable(false);
	        	break;
	        case R.id.proportion:
	        	setTabSelection(2);
	        	changeMenuIcon = 2;
	        	clearSelection();
	        	item.setCheckable(false);
	        	break;
	        case R.id.config:
	        	setTabSelection(3);
	        	changeMenuIcon = 3;
	        	clearSelection();
	        	item.setCheckable(false);
	        	break;
	        default:
				break;
	    }
	    return true;
	}
	
	@Override  
    public boolean onPrepareOptionsMenu(Menu menu) {  
		// TO DO 
		if(changeMenuIcon == 0){
			MenuItem item = (MenuItem)menu.findItem(R.id.fund);
			item.setIcon(R.drawable.fund_pressed);
		}
		else if(changeMenuIcon == 1){
			MenuItem item = (MenuItem)menu.findItem(R.id.stock);
			item.setIcon(R.drawable.stock_pressed);
		}
		else if(changeMenuIcon == 2){
			MenuItem item = (MenuItem)menu.findItem(R.id.proportion);
			item.setIcon(R.drawable.proportion_pressed);
		}
		else if(changeMenuIcon == 3){
			MenuItem item = (MenuItem)menu.findItem(R.id.config);
			item.setIcon(R.drawable.config_pressed);
		}
        return super.onPrepareOptionsMenu(menu);  
    }

}
