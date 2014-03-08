package com.mdw.moneyconfig;

import java.util.ArrayList;
import java.util.List;

import com.mdw.moneyconfig.MyHScrollView.OnScrollChangedListener;
import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FundFragment extends Fragment {
	
	ListView mListView1;
	MyAdapter myAdapter;
	RelativeLayout mHead;
	LinearLayout main;

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
        int count;
		public MyAdapter(Context fundFragment, int id_row_layout) {
			super();
			this.id_row_layout = id_row_layout;
			mInflater = LayoutInflater.from(fundFragment);
			//从数据库取基金数据条数
            DatabaseHelper dbHelper = new DatabaseHelper(MyApplication.getInstance(),  
                    "moneyconfig_db", 2);  
            // 得到一个只读的SQLiteDatabase对象  
            SQLiteDatabase sqliteDatabase = dbHelper.getReadableDatabase();
            //获取基金表记录数
            Cursor mCount = sqliteDatabase.rawQuery("select count(*) from fund", null);
            mCount.moveToFirst();
            this.count = mCount.getInt(0);
            mCount.close();
            sqliteDatabase.close();
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return count;
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
					holder.name = (TextView) convertView
							.findViewById(R.id.fund_textView2);
					holder.price = (TextView) convertView
							.findViewById(R.id.fund_textView3);
					holder.updown = (TextView) convertView
							.findViewById(R.id.fund_textView4);
					holder.scope = (TextView) convertView
							.findViewById(R.id.fund_textView5);
					holder.date = (TextView) convertView
							.findViewById(R.id.fund_textView6);
					holder.poundage = (TextView) convertView
							.findViewById(R.id.fund_textView7);
					holder.capital = (TextView) convertView
							.findViewById(R.id.fund_textView8);

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
            Cursor cursor = sqliteDatabase.query("fund", new String[] { "fundCode",  
                    "name", "price", "updown", "scope", "date", "poundage", "capital" }, 
                    null, null, null, null, null); 
            // 将光标移动到下一行，从而判断该结果集是否还有下一条数据，如果有则返回true，没有则返回false  
            while (cursor.moveToNext()) {  
                holder.fundCode.setText(cursor.getString(cursor.getColumnIndex("fundCode")));
                holder.name.setText(cursor.getString(cursor.getColumnIndex("name")));
                holder.price.setText(cursor.getString(cursor.getColumnIndex("price")));
                holder.updown.setText(cursor.getString(cursor.getColumnIndex("updown")));
                holder.scope.setText(cursor.getString(cursor.getColumnIndex("scope")));
                holder.date.setText(cursor.getString(cursor.getColumnIndex("date")));
                holder.poundage.setText(cursor.getString(cursor.getColumnIndex("poundage")));
                holder.capital.setText(cursor.getString(cursor.getColumnIndex("capital")));
            }
            //关闭游标
            cursor.close();
            sqliteDatabase.close();
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
			TextView name;
			TextView price;
			TextView updown;
			TextView scope;
			TextView date;
			TextView poundage;
			TextView capital;
			HorizontalScrollView scrollView;
		}
	}

}
