package com.mdw.moneyconfig;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class buyFundActivity extends Activity {

    Cursor cursor;
    MyAdapter myAdapter;
    LinearLayout mHead;
    SwipeDismissListView swipeDismissListView;
    int dbCount;
    // 基金代码
    String fc;

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
        String fn = getIntent().getExtras().getString("fundName");
        TextView ttfbi = (TextView) addView.findViewById(R.id.title_text_fundbuyinfo);
        // 设置标题内容为基金名字+代码
        ttfbi.setText(fn+"["+fc+"]");

        swipeDismissListView = (SwipeDismissListView) findViewById(R.id.fund_buyinfo_listView);

        //从数据库取基金数据
        final DatabaseHelper dbHelper = new DatabaseHelper(MyApplication.getInstance(),
                "moneyconfig_db", 2);
        // 得到一个只读的SQLiteDatabase对象
        SQLiteDatabase sqliteDatabase = dbHelper.getReadableDatabase();
        // 调用SQLiteDatabase对象的query方法进行查询，返回一个Cursor对象：由数据库查询返回的结果集对象
        cursor = sqliteDatabase.query("fund_buyInfo", new String[] { "_id","buyMoney",
                "buyAmount", "buyPrice", "poundage", "buyDate"},
                null, null, null, null, null);

        this.dbCount = cursor.getCount();

//        SimpleCursorAdapter cursorAdapter=new SimpleCursorAdapter(this,R.layout.item_fund_buyinfo,
//                cursor,new String[]{"buyMoney","buyAmount","buyPrice","poundage","buyDate"},
//                new int []{R.id.fundBuyInfoBuyMoneyLV,R.id.fundBuyInfoBuyAmountLV,
//                R.id.fundBuyInfoBuyPriceLV,R.id.fundBuyInfoPoundageLV,R.id.fundBuyInfoDateLV});
//        swipeDismissListView.setAdapter(cursorAdapter);

        myAdapter = new MyAdapter(buyFundActivity.this, R.layout.item_fund_buyinfo);
        swipeDismissListView.setAdapter(myAdapter);

        swipeDismissListView.setOnDismissCallback(new SwipeDismissListView.OnDismissCallback() {
            @Override
            public void onDismiss(int dismissPosition) {
                // 打开数据库
                SQLiteDatabase s = dbHelper.getReadableDatabase();
                // 游标移到要删除的对象
                cursor.moveToPosition(dismissPosition);
                // 删除基金购买记录
                s.execSQL("delete from fund_buyInfo where _id='" +
                        String.valueOf(cursor.getInt(cursor.getColumnIndex("_id"))) + "'");
                // 重新查询游标
                cursor = s.query("fund_buyInfo", new String[] { "_id","buyMoney",
                        "buyAmount", "buyPrice", "poundage", "buyDate"},
                        null, null, null, null, null);
                dbCount = cursor.getCount();
                // 关闭数据库
                s.close();
                // 通知数据更新
                myAdapter.notifyDataSetChanged();
            }
        });
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
            case R.id.fund_deleteAll:
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

                // 打开主界面
                Intent MainActivityIntent = new Intent();
                //MainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                //MainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MainActivityIntent.setClass(buyFundActivity.this,MainActivity.class);
                startActivity(MainActivityIntent);
                finish();

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
        Intent MainActivityIntent = new Intent();
        //MainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        //MainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MainActivityIntent.setClass(buyFundActivity.this,MainActivity.class);
        startActivity(MainActivityIntent);
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
