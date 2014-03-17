package com.mdw.moneyconfig;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.List;

/**
 * 数据库操作类
 * @author zFish
 *
 */

public class DataSource {

    // 创建了一个DatabaseHelper对象，只执行这句话是不会创建或打开连接的
    private static DatabaseHelper dbHelper = new DatabaseHelper(MyApplication.getInstance(), "moneyconfig_db");
    private static SQLiteDatabase sqliteDatabase;
    // 游标
    private static Cursor cursor;
    // 封装数据
    private static ContentValues cv;
    // 基金实时数据实体类
    private static FundBase fundBase;
    // 基金购买记录实体类
    private static List<FundBuyInfo> fundBuyInfo;
    // 基金赎回记录实体类
    private static List<FundRedeem> fundRedeem;
    // 基金概览实体类
    private static FundSum fundSum;

    /**
     * 通过基金代码，返回基金购买记录列表
     * @param fundCode
     * @return
     */
    public static List<FundBuyInfo> queryFundBuyInfoByCode(String fundCode){
        sqliteDatabase = dbHelper.getWritableDatabase();
        FundBuyInfo fb = new FundBuyInfo();
        cursor = sqliteDatabase.query("fund_buyInfo", null,
                "fundCode='of"+fundCode+"'", null, null, null, null);
        while (cursor.moveToNext()){
            fb.setBuyAmount(cursor.getString(cursor.getColumnIndex("buyAmount")));
            fb.setBuyDate(cursor.getString(cursor.getColumnIndex("buyDate")));
            fb.setBuyMoney(cursor.getString(cursor.getColumnIndex("buyMoney")));
            fb.setBuyPrice(cursor.getString(cursor.getColumnIndex("buyPrice")));
            fb.setFundCode(cursor.getString(cursor.getColumnIndex("fundCode")));
            fb.setFundInsuranceType(cursor.getInt(cursor.getColumnIndex("fundInsuranceType")));
            fb.setFundRate(cursor.getString(cursor.getColumnIndex("fundRate")));
            fb.setPoundage(cursor.getString(cursor.getColumnIndex("poundage")));
            fundBuyInfo.add(fb);
        }
        cursor.close();
        sqliteDatabase.close();
        return fundBuyInfo;
    }

    /**
     * 通过基金代码查询基金概览并返回封装数据
     * @param fundCode
     * @return
     */
    public static ContentValues queryFundSumByCode(String fundCode){
        cv = new ContentValues();
        cursor = sqliteDatabase.query("fund_sum", null,
                "fundCode='of"+fundCode+"'", null, null, null, null);
        cv.put("fund_ProfitOrLossToday",cursor.getString(cursor.getColumnIndex("fund_ProfitOrLossToday")));
        cv.put("fund_ProfitOrLossSum",cursor.getString(cursor.getColumnIndex("fund_ProfitOrLossSum")));
        cv.put("fund_ProfitOrLossRate",cursor.getString(cursor.getColumnIndex("fund_ProfitOrLossRate")));
        cv.put("fund_MarketValue",cursor.getString(cursor.getColumnIndex("fund_MarketValue")));
        cv.put("fund_Position",cursor.getString(cursor.getColumnIndex("fund_Position")));
        cv.put("buyMoneySum",cursor.getString(cursor.getColumnIndex("buyMoneySum")));
        cursor.close();
        return cv;
    }

}
