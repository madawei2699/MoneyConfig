package com.mdw.moneyconfig;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class buyFundActivity extends Activity {

    Cursor cursor;
    MyAdapter myAdapter;
    LinearLayout mHead;
//    SwipeDismissListView swipeDismissListView;
    ListView lv;
    int dbCount;
    // 基金代码
    String fc;
    // 基金名称
    String fn;
    // 基金持仓
    String position;

    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fund_buyinfo_layout);

        mHead = (LinearLayout) findViewById(R.id.fund_buyinfo_head);
        mHead.setFocusable(true);
        mHead.setBackgroundColor(Color.parseColor("#b2d235"));

        View addView = getLayoutInflater().inflate(R.layout.titlebar_fundbuyinfo, null);
        getActionBar().setCustomView(addView);
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setDisplayShowCustomEnabled(true);
        fc = getIntent().getExtras().getString("fundCode");
        fn = getIntent().getExtras().getString("fundName");
        position = getIntent().getExtras().getString("position");
        TextView ttfbi = (TextView) addView.findViewById(R.id.title_text_fundbuyinfo);
        // 设置标题内容为基金名字+代码
        ttfbi.setText(fn+"["+fc+"]");

        lv = (ListView) findViewById(R.id.fund_buyinfo_listView);

        //从数据库取基金数据
        final DatabaseHelper dbHelper = new DatabaseHelper(MyApplication.getInstance(),
                "moneyconfig_db", 2);
        // 得到一个只读的SQLiteDatabase对象
        SQLiteDatabase sqliteDatabase = dbHelper.getReadableDatabase();
        // 调用SQLiteDatabase对象的query方法进行查询，返回一个Cursor对象：由数据库查询返回的结果集对象
        cursor = sqliteDatabase.query("fund_buyInfo", new String[] { "_id","buyMoney",
                "buyAmount", "buyPrice", "poundage", "buyDate"},
                "fundCode='of"+fc+"'", null, null, null, null);

        this.dbCount = cursor.getCount();

//        SimpleCursorAdapter cursorAdapter=new SimpleCursorAdapter(this,R.layout.item_fund_buyinfo,
//                cursor,new String[]{"buyMoney","buyAmount","buyPrice","poundage","buyDate"},
//                new int []{R.id.fundBuyInfoBuyMoneyLV,R.id.fundBuyInfoBuyAmountLV,
//                R.id.fundBuyInfoBuyPriceLV,R.id.fundBuyInfoPoundageLV,R.id.fundBuyInfoDateLV});
//        swipeDismissListView.setAdapter(cursorAdapter);

        myAdapter = new MyAdapter(buyFundActivity.this, R.layout.item_fund_buyinfo);
//        swipeDismissListView.setAdapter(myAdapter);
        lv.setAdapter(myAdapter);

//        swipeDismissListView.setOnDismissCallback(new SwipeDismissListView.OnDismissCallback() {
//            @Override
//            public void onDismiss(int dismissPosition) {
//                // 打开数据库
//                SQLiteDatabase s = dbHelper.getReadableDatabase();
//                // 游标移到要删除的对象
//                cursor.moveToPosition(dismissPosition);
//                // 删除基金购买记录
//                s.execSQL("delete from fund_buyInfo where _id='" +
//                        String.valueOf(cursor.getInt(cursor.getColumnIndex("_id"))) + "'");
//                // 重新查询游标
//                cursor = s.query("fund_buyInfo", new String[] { "_id","buyMoney",
//                        "buyAmount", "buyPrice", "poundage", "buyDate"},
//                        "fundCode='of"+fc+"'", null, null, null, null);
//                dbCount = cursor.getCount();
//                // 关闭数据库
//                s.close();
//                // 通知数据更新
//                myAdapter.notifyDataSetChanged();
//            }
//        });
        // 关闭数据库
        sqliteDatabase.close();
    } 

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.fundbuyinfo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.fund_redeem:
                // 使用bundle传递基金代码和名字
                Bundle bundle = new Bundle();
                bundle.putString("fundCode", fc);
                bundle.putString("fundName",fn);
                bundle.putString("position",position);
                Intent redeemFund = new Intent(buyFundActivity.this,
                        RedeemFundActivity.class);
                redeemFund.putExtras(bundle);
                this.startActivity(redeemFund);
                finish();
                break;
            case R.id.fund_deleteAll:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder//给builder set各种属性值
                        .setMessage(getString(R.string.alert_dialog_message))
                        .setPositiveButton("确定删除", new DialogInterface.OnClickListener() {//确定按钮
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //从数据库取基金数据
                                DatabaseHelper dbHelper = new DatabaseHelper(MyApplication.getInstance(),
                                        "moneyconfig_db", 2);
                                // 打开数据库
                                SQLiteDatabase s = dbHelper.getReadableDatabase();
                                // 以指定fundCode删除三张基金表中全部的相关基金
                                s.execSQL("delete from fund_buyInfo where fundCode='of" + fc + "'");
                                s.execSQL("delete from fund_base where fundCode='of" + fc + "'");
                                s.execSQL("delete from fund_sum where fundCode='of" + fc + "'");
                                // 关闭数据库
                                s.close();

                                // 使用bundle更新基金界面
                                Bundle bundle = new Bundle();
                                bundle.putBoolean("updateFund", true);
                                // 打开主界面
                                Intent MainActivityIntent = new Intent();
                                //MainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                                MainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                MainActivityIntent.setClass(buyFundActivity.this,MainActivity.class);
                                MainActivityIntent.putExtras(bundle);
                                startActivity(MainActivityIntent);
                                finish();
                            }
                        })
                        .setNegativeButton("我按错了", new DialogInterface.OnClickListener() {//取消按钮
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();//显示AlertDialog
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

    @Override
    public void onBackPressed()
    {
        //do whatever you want the 'Back' button to do
        //as an example the 'Back' button is set to start a new Activity named 'NewActivity'
        // 打开主界面
        //Intent MainActivityIntent = new Intent();
        //MainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        //MainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //MainActivityIntent.setClass(buyFundActivity.this,MainActivity.class);
        //startActivity(MainActivityIntent);
        finish();

        return;
    }

    public class MyAdapter extends BaseAdapter {
        public List<ViewHolder> mHolderList = new ArrayList<ViewHolder>();

        int id_row_layout;
        LayoutInflater mInflater;
        public MyAdapter(Context buyFundActivity, int id_row_layout) {
            super();
            this.id_row_layout = id_row_layout;
            mInflater = LayoutInflater.from(buyFundActivity);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return dbCount;
        }

        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parentView) {
            ViewHolder holder = null;
            if (convertView == null) {
                synchronized (buyFundActivity.this) {
                    convertView = mInflater.inflate(id_row_layout, null);
                    holder = new ViewHolder();

                    holder.buyMoney = (TextView) convertView
                            .findViewById(R.id.fundBuyInfoBuyMoneyLV);
                    holder.buyAmount = (TextView) convertView
                            .findViewById(R.id.fundBuyInfoBuyAmountLV);
                    holder.buyPrice = (TextView) convertView
                            .findViewById(R.id.fundBuyInfoBuyPriceLV);
                    holder.poundage = (TextView) convertView
                            .findViewById(R.id.fundBuyInfoPoundageLV);
                    holder.buyDate = (TextView) convertView
                            .findViewById(R.id.fundBuyInfoDateLV);
                    convertView.setTag(holder);
                    mHolderList.add(holder);
                }
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            // 将光标移动指定位置
            cursor.moveToPosition(position);

            holder.buyMoney.setText(cursor.getString(cursor.getColumnIndex("buyMoney")));
            holder.buyAmount.setText(cursor.getString(cursor.getColumnIndex("buyAmount")));
            holder.buyPrice.setText(cursor.getString(cursor.getColumnIndex("buyPrice")));
            holder.poundage.setText(cursor.getString(cursor.getColumnIndex("poundage")));
            holder.buyDate.setText(cursor.getString(cursor.getColumnIndex("buyDate")));
            // 设置字体大小
            holder.buyMoney.setTextSize(15);
            holder.buyAmount.setTextSize(15);
            holder.buyPrice.setTextSize(15);
            holder.poundage.setTextSize(15);
            holder.buyDate.setTextSize(12);
            return convertView;
        }

        class OnScrollChangedListenerImp implements MyHScrollView.OnScrollChangedListener {
            MyHScrollView mScrollViewArg;

            public OnScrollChangedListenerImp(MyHScrollView scrollViewar) {
                mScrollViewArg = scrollViewar;
            }

            @Override
            public void onScrollChanged(int l, int t, int oldl, int oldt) {
                mScrollViewArg.smoothScrollTo(l, t);
            }
        };

        class ViewHolder {
            TextView buyMoney;
            TextView buyAmount;
            TextView buyPrice;
            TextView poundage;
            TextView buyDate;
        }
    }
}
