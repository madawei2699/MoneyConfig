package com.mdw.moneyconfig;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

/**
 * SQLiteOpenHelper是一个辅助类，用来管理数据库的创建和版本他，它提供两个方面的功能
 * 第一，getReadableDatabase()、getWritableDatabase()可以获得SQLiteDatabase对象，通过该对象可以对数据库进行操作
 * 第二，提供了onCreate()、onUpgrade()两个回调函数，允许我们再创建和升级数据库时，进行自己的操作
 */
public class DatabaseHelper extends SQLiteOpenHelper {
	private static final int VERSION = 2;

	/**
	 * 在SQLiteOpenHelper的子类当中，必须有该构造函数
	 * @param context	上下文对象
	 * @param name		数据库名称
	 * @param factory
	 * @param version	当前数据库的版本，值必须是整数并且是递增的状态
	 */
	public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
		//必须通过super调用父类当中的构造函数
		super(context, name, factory, version);
	}
	
	public DatabaseHelper(Context context, String name, int version){
		this(context,name,null,version);
	}

	public DatabaseHelper(Context context, String name){
		this(context,name,VERSION);
	}

	//该函数是在第一次创建的时候执行，实际上是第一次得到SQLiteDatabase对象的时候才会调用这个方法
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		//创建基金数据表
		//fund_base表存储基金频繁更新的数据，包括基金名称、现价、涨跌、涨跌幅、日期;
        //fundCode-基金代码 name-基金名称 price-基金现价 updown-涨跌 scope-涨跌幅 date-日期
		db.execSQL("create table fund_base(_id integer primary key autoincrement,fundCode varchar(10) not null unique," +
                "name varchar(10),price varchar(10),updown varchar(10),scope varchar(10)," +
				"date varchar(10))");
        //fund_buyInfo表存储基金购买每次购买信息，包括购买金额、购买净值、购买数量、收费模式、收费费率、购买日期、手续费
        //fundCode-基金代码 buyPrice-购买价格 buyAmount-购买数量 fundRate-费率 buyDate-购买日期 fundInsuranceType-收费模式
        //poundage-手续费 buyMoney-购买金额
        db.execSQL("create table fund_buyInfo(_id integer primary key autoincrement,fundCode varchar(10) not null," +
                "buyPrice varchar(10),buyAmount varchar(10),fundRate varchar(10),buyDate varchar(10)," +
                "fundInsuranceType INTEGER,poundage varchar(10),buyMoney varchar(10))");
        //fund_sum表存储某个基金的概览信息，包括本金、数量、盈亏、盈亏比列、止损、止盈、赎回费率、赎回费
        //fundCode-基金代码 fundCapital-该基金本金 fundAmount-该基金数量 profitOrLoss-盈亏 PLPercent-盈亏比列 stopLoss-止损 stopProfit-止盈
        //redeem-赎回费 redeemRate-赎回费率
        db.execSQL("create table fund_sum(_id integer primary key autoincrement,fundCode varchar(10) not null unique," +
                "fundCapital varchar(10),fundAmount varchar(10),profitOrLoss varchar(10),PLPercent varchar(10)," +
                "stopLoss varchar(10),stopProfit varchar(10),redeem varchar(10),redeemRate varchar(10))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
        arg0.execSQL("DROP TABLE IF EXISTS fund_base");
        arg0.execSQL("DROP TABLE IF EXISTS fund_buyInfo");
        arg0.execSQL("DROP TABLE IF EXISTS fund_sum");

        // create new tables
        onCreate(arg0);
	}
}

