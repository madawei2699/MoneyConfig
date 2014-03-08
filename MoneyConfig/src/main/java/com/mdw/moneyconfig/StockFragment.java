package com.mdw.moneyconfig;

import java.util.ArrayList;
import java.util.List;

import com.mdw.moneyconfig.MyHScrollView.OnScrollChangedListener;

import android.app.Fragment;
import android.content.Context;
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

public class StockFragment extends Fragment {
	
	ListView mListView1;
	MyAdapter myAdapter;
	RelativeLayout mHead;
	LinearLayout main;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View stockLayout = inflater.inflate(R.layout.stock_layout,
				container, false);
		mHead = (RelativeLayout) stockLayout.findViewById(R.id.stock_head);
		mHead.setFocusable(true);
		mHead.setClickable(true);
		mHead.setBackgroundColor(Color.parseColor("#b2d235"));
		mHead.setOnTouchListener(new ListViewAndHeadViewTouchLinstener());

		mListView1 = (ListView) stockLayout.findViewById(R.id.stock_listView1);
		mListView1.setOnTouchListener(new ListViewAndHeadViewTouchLinstener());

		myAdapter = new MyAdapter(this.getActivity(), R.layout.item_stock);
		mListView1.setAdapter(myAdapter);
		return stockLayout;
	}
	
	class ListViewAndHeadViewTouchLinstener implements View.OnTouchListener {

		@Override
		public boolean onTouch(View arg0, MotionEvent arg1) {
			//当在列头 和 listView控件上touch时，将这个touch的事件分发给 ScrollView
			HorizontalScrollView headSrcrollView = (HorizontalScrollView) mHead
					.findViewById(R.id.stock_horizontalScrollView1);
			headSrcrollView.onTouchEvent(arg1);
			return false;
		}
	}
	
	public class MyAdapter extends BaseAdapter {
		public List<ViewHolder> mHolderList = new ArrayList<ViewHolder>();

		int id_row_layout;
		LayoutInflater mInflater;

		public MyAdapter(Context stockFragment, int id_row_layout) {
			super();
			this.id_row_layout = id_row_layout;
			mInflater = LayoutInflater.from(stockFragment);

		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 250;
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
				synchronized (StockFragment.this) {
					convertView = mInflater.inflate(id_row_layout, null);
					holder = new ViewHolder();

					MyHScrollView scrollView1 = (MyHScrollView) convertView
							.findViewById(R.id.stock_horizontalScrollView1);

					holder.scrollView = scrollView1;
					holder.txt1 = (TextView) convertView
							.findViewById(R.id.stock_textView1);
					holder.txt2 = (TextView) convertView
							.findViewById(R.id.stock_textView2);
					holder.txt3 = (TextView) convertView
							.findViewById(R.id.stock_textView3);
					holder.txt4 = (TextView) convertView
							.findViewById(R.id.stock_textView4);
					holder.txt5 = (TextView) convertView
							.findViewById(R.id.stock_textView5);

					MyHScrollView headSrcrollView = (MyHScrollView) mHead
							.findViewById(R.id.stock_horizontalScrollView1);
					headSrcrollView
							.AddOnScrollChangedListener(new OnScrollChangedListenerImp(
									scrollView1));

					convertView.setTag(holder);
					mHolderList.add(holder);
				}
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.txt1.setText(position + "" + 1);
			holder.txt2.setText(position + "" + 2);
			holder.txt3.setText(position + "" + 3);
			holder.txt4.setText(position + "" + 4);
			holder.txt5.setText(position + "" + 5);

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
			TextView txt1;
			TextView txt2;
			TextView txt3;
			TextView txt4;
			TextView txt5;
			HorizontalScrollView scrollView;
		}
	}

}
