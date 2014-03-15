package com.mdw.moneyconfig;

import java.util.ArrayList;
import java.util.List;

import com.mdw.moneyconfig.MyHScrollView.OnScrollChangedListener;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import static android.widget.AdapterView.OnItemClickListener;

public class FundFragment extends Fragment {
	
	ListView mListView1;
	MyAdapter myAdapter;
	RelativeLayout mHead;
	LinearLayout main;
    Cursor cursor;
    int dbCount;

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View fundLayout = inflater.inflate(R.layout.fund_layout,
				container, false);
		mHead = (RelativeLayout) fundLayout.findViewById(R.id.fund_head);
		mHead.setFocusable(true);
		mHead.setClickable(true);
		mHead.setBackgroundColor(Color.parseColor("#b2d235"));
		mHead.setOnTouchListener(new ListViewAndHeadViewTouchLinstener());

		mListView1 = (ListView) fundLayout.findViewById(R.id.fund_listView1);
		mListView1.setOnTouchListener(new ListViewAndHeadViewTouchLinstener());
        mListView1.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView t = (TextView) view.findViewById(R.id.fund_textView1);
                // 获取被点击Item
                String fc = t.getText().toString().split("\n")[1];
                // 只显示五个汉字名称
                String fn = t.getText().toString().split("\n")[0];
                if(!(fn.equals(""))&&fn.length()>5){
                    fn = fn.substring(0,5);
                }
                // 使用bundle传递基金代码和名字
                Bundle bundle = new Bundle();
                bundle.putString("fundCode", fc);
                bundle.putString("fundName",fn);
                Intent fundBuyInfo = new Intent(FundFragment.this.getActivity(),
                        buyFundActivity.class);
                fundBuyInfo.putExtras(bundle);
                FundFragment.this.startActivity(fundBuyInfo);
                FundFragment.this.getActivity().finish();
            }
        });
        //从数据库取基金数据
        DatabaseHelper dbHelper = new DatabaseHelper(MyApplication.getInstance(),
                "moneyconfig_db", 2);
        // 得到一个只读的SQLiteDatabase对象
        SQLiteDatabase sqliteDatabase = dbHelper.getReadableDatabase();
        // 调用SQLiteDatabase对象的query方法进行查询，返回一个Cursor对象：由数据库查询返回的结果集对象
        // 第一个参数String：表名
        // 第二个参数String[]:要查询的列名
        // 第三个参数String：查询条件
        // 第四个参数String[]：查询条件的参数
        // 第五个参数String:对查询的结果进行分组
        // 第六个参数String：对分组的结果进行限制
        // 第七个参数String：对查询的结果进行排序
        cursor = sqliteDatabase.query("fund_base", new String[] { "fundCode",
                "name", "price", "updown", "scope", "date"},
                null, null, null, null, null);

        this.dbCount = cursor.getCount();

        // 关闭数据库
        sqliteDatabase.close();

        myAdapter = new MyAdapter(this.getActivity(), R.layout.item_fund);
		mListView1.setAdapter(myAdapter);

		return fundLayout;
	}

	class ListViewAndHeadViewTouchLinstener implements View.OnTouchListener {

		@Override
		public boolean onTouch(View arg0, MotionEvent arg1) {
			//当在列头 和 listView控件上touch时，将这个touch的事件分发给 ScrollView
			HorizontalScrollView headSrcrollView = (HorizontalScrollView) mHead
					.findViewById(R.id.fund_horizontalScrollView1);
			headSrcrollView.onTouchEvent(arg1);
			return false;
		}
	}
	
	public class MyAdapter extends BaseAdapter {
		public List<ViewHolder> mHolderList = new ArrayList<ViewHolder>();

		int id_row_layout;
		LayoutInflater mInflater;
		public MyAdapter(Context fundFragment, int id_row_layout) {
			super();
			this.id_row_layout = id_row_layout;
			mInflater = LayoutInflater.from(fundFragment);
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
				synchronized (FundFragment.this) {
					convertView = mInflater.inflate(id_row_layout, null);
					holder = new ViewHolder();

					MyHScrollView scrollView1 = (MyHScrollView) convertView
							.findViewById(R.id.fund_horizontalScrollView1);

					holder.scrollView = scrollView1;
					holder.fundCode = (TextView) convertView
							.findViewById(R.id.fund_textView1);
					holder.price = (TextView) convertView
							.findViewById(R.id.fund_textView3);
					holder.updown = (TextView) convertView
							.findViewById(R.id.fund_textView4);
					holder.scope = (TextView) convertView
							.findViewById(R.id.fund_textView5);
                    holder.fund_ProfitOrLossToday = (TextView) convertView
                            .findViewById(R.id.fund_textView_ProfitOrLossToday);
                    holder.fund_ProfitOrLossSum = (TextView) convertView
                            .findViewById(R.id.fund_textView_ProfitOrLossSum);
                    holder.fund_ProfitOrLossRate = (TextView) convertView
                            .findViewById(R.id.fund_textView_ProfitOrLossRate);
                    holder.fund_MarketValue = (TextView) convertView
                            .findViewById(R.id.fund_textView_MarketValue);
                    holder.fund_Position = (TextView) convertView
                            .findViewById(R.id.fund_textView_Position);
					holder.date = (TextView) convertView
							.findViewById(R.id.fund_textView6);
					MyHScrollView headSrcrollView = (MyHScrollView) mHead
							.findViewById(R.id.fund_horizontalScrollView1);
					headSrcrollView
							.AddOnScrollChangedListener(new OnScrollChangedListenerImp(
									scrollView1));

					convertView.setTag(holder);
					mHolderList.add(holder);
				}
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
            // 将光标移动指定位置
            cursor.moveToPosition(position);
            // 不显示"of"
            String fundCode = cursor.getString(cursor.getColumnIndex("fundCode")).replaceAll("of", "");
            // 只显示五个汉字名称
            String fundName = cursor.getString(cursor.getColumnIndex("name"));
            if(!(fundName.equals(""))&&fundName.length()>5){
                fundName = fundName.substring(0,5);
            }
            String price = cursor.getString(cursor.getColumnIndex("price"));
            String updown = cursor.getString(cursor.getColumnIndex("updown"));
            // 基金名称和代码显示为一列
            holder.fundCode.setText(fundName +"\n" + fundCode);
            holder.price.setText(price);
            holder.updown.setText(updown);
            holder.scope.setText(cursor.getString(cursor.getColumnIndex("scope")));
            holder.date.setText(cursor.getString(cursor.getColumnIndex("date")));
            ContentValues cv = calcFundSum("of"+fundCode,price,updown);
            // 设置今日盈亏
            holder.fund_ProfitOrLossToday.setText(cv.getAsString("fund_ProfitOrLossToday"));
            // 设置累计盈亏
            holder.fund_ProfitOrLossSum.setText(cv.getAsString("fund_ProfitOrLossSum"));
            // 设置盈亏幅度
            holder.fund_ProfitOrLossRate.setText(cv.getAsString("fund_ProfitOrLossRate"));
            // 设置市值
            holder.fund_MarketValue.setText(cv.getAsString("fund_MarketValue"));
            // 设置持仓
            holder.fund_Position.setText(cv.getAsString("fund_Position"));
            // 设置字体大小
            holder.fundCode.setTextSize(15);
            holder.price.setTextSize(20);
            holder.updown.setTextSize(20);
            holder.scope.setTextSize(20);
            holder.fund_ProfitOrLossToday.setTextSize(20);
            holder.fund_ProfitOrLossSum.setTextSize(20);
            holder.fund_ProfitOrLossRate.setTextSize(20);
            holder.fund_MarketValue.setTextSize(20);
            holder.fund_Position.setTextSize(20);
            holder.date.setTextSize(15);
            // 基金涨幅大于0，则颜色设置为红色，否则为绿色
            if(Double.parseDouble(cursor.getString(cursor.getColumnIndex("updown")))>0){
                holder.price.setTextColor(getResources().getColor(R.color.red));
                holder.updown.setTextColor(getResources().getColor(R.color.red));
                holder.scope.setTextColor(getResources().getColor(R.color.red));
            }else {
                holder.price.setTextColor(getResources().getColor(R.color.green));
                holder.updown.setTextColor(getResources().getColor(R.color.green));
                holder.scope.setTextColor(getResources().getColor(R.color.green));
            }
            if(Double.parseDouble(cv.getAsString("fund_ProfitOrLossToday"))>0){
                holder.fund_ProfitOrLossToday.setTextColor(getResources().getColor(R.color.red));
            }else {
                holder.fund_ProfitOrLossToday.setTextColor(getResources().getColor(R.color.green));
            }
            if(Double.parseDouble(cv.getAsString("fund_ProfitOrLossSum"))>0){
                holder.fund_ProfitOrLossSum.setTextColor(getResources().getColor(R.color.red));
            }else {
                holder.fund_ProfitOrLossSum.setTextColor(getResources().getColor(R.color.green));
            }
            if(Double.parseDouble(cv.getAsString("fund_ProfitOrLossRate"))>0){
                holder.fund_ProfitOrLossRate.setTextColor(getResources().getColor(R.color.red));
            }else {
                holder.fund_ProfitOrLossRate.setTextColor(getResources().getColor(R.color.green));
            }
			return convertView;
		}

		class OnScrollChangedListenerImp implements OnScrollChangedListener {
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
			TextView fundCode;
			TextView price;
			TextView updown;
			TextView scope;
			TextView fund_ProfitOrLossToday;
			TextView fund_ProfitOrLossSum;
			TextView fund_ProfitOrLossRate;
			TextView fund_MarketValue;
			TextView fund_Position;
			TextView date;
			HorizontalScrollView scrollView;
		}
	}

    /**
     * 计算基金今日盈亏、累计盈亏、盈亏幅度、市值、持仓
     * @return
     */
    public ContentValues calcFundSum(String fundCode,String price,String updown){
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

        //从数据库取基金数据
        DatabaseHelper dbHelper = new DatabaseHelper(MyApplication.getInstance(),
                "moneyconfig_db", 2);
        // 得到一个只读的SQLiteDatabase对象
        SQLiteDatabase sqliteDatabase = dbHelper.getReadableDatabase();
        Cursor fundBuyCursor = sqliteDatabase.query("fund_buyInfo", new String[]{"buyAmount",
                "poundage", "buyMoney"},
                "fundCode='"+fundCode+"'", null, null, null, null);

        while (fundBuyCursor.moveToNext()){
            buyAmountSum += Double.parseDouble(fundBuyCursor.getString(
                    fundBuyCursor.getColumnIndex("buyAmount")));
            poundageSum += Double.parseDouble(fundBuyCursor.getString(
                    fundBuyCursor.getColumnIndex("poundage")));
            buyMoneySum += Double.parseDouble(fundBuyCursor.getString(
                    fundBuyCursor.getColumnIndex("buyMoney")));
        }
        fund_Position = buyAmountSum;
        fund_ProfitOrLossToday = doubleUpdown*fund_Position;
        fund_MarketValue = doublePrice*fund_Position;
        fund_ProfitOrLossSum = fund_MarketValue-(buyMoneySum-poundageSum);
        fund_ProfitOrLossRate = fund_ProfitOrLossSum/(buyMoneySum-poundageSum);
        // 封装返回数据
        ContentValues value = new ContentValues();
        value.put("fund_Position",String.format("%.2f",fund_Position));
        value.put("fund_ProfitOrLossToday",String.format("%.2f",fund_ProfitOrLossToday));
        value.put("fund_MarketValue",String.format("%.2f",fund_MarketValue));
        value.put("fund_ProfitOrLossSum",String.format("%.2f",fund_ProfitOrLossSum));
        value.put("fund_ProfitOrLossRate",String.format("%.2f",fund_ProfitOrLossRate));
        // 关闭数据库
        sqliteDatabase.close();
        // 关闭游标
        fundBuyCursor.close();
        return value;
    }

}
