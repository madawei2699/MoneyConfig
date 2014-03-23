package com.mdw.moneyconfig.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mdw.moneyconfig.utils.MyApplication;
import com.mdw.moneyconfig.database.model.fund.FundBase;
import com.mdw.moneyconfig.database.model.fund.FundBuyInfo;
import com.mdw.moneyconfig.database.model.fund.FundRedeem;
import com.mdw.moneyconfig.database.model.fund.FundSum;

import java.util.ArrayList;
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
        sqliteDatabase = dbHelper.getReadableDatabase();
        FundBuyInfo fb;
        //初始化列表
        fundBuyInfo = new ArrayList<FundBuyInfo>();
        cursor = sqliteDatabase.query("fund_buyInfo", null,
                "fundCode='"+fundCode+"'", null, null, null, null);
        while (cursor.moveToNext()){
            fb = new FundBuyInfo();
            fb.setId(cursor.getInt(cursor.getColumnIndex("_id")));
            fb.setBuyAmount(cursor.getString(cursor.getColumnIndex("buyAmount")));
            fb.setBuyDate(cursor.getString(cursor.getColumnIndex("buyDate")));
            fb.setBuyMoney(cursor.getString(cursor.getColumnIndex("buyMoney")));
            fb.setBuyPrice(cursor.getString(cursor.getColumnIndex("buyPrice")));
            fb.setFundCode(cursor.getString(cursor.getColumnIndex("fundCode")));
            fb.setFundInsuranceType(cursor.getInt(cursor.getColumnIndex("fundInsuranceType")));
            fb.setFundRate(cursor.getString(cursor.getColumnIndex("fundRate")));
            fb.setPoundage(cursor.getString(cursor.getColumnIndex("poundage")));
            fb.setRedeemAmount(cursor.getString(cursor.getColumnIndex("redeemAmount")));
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
        sqliteDatabase = dbHelper.getReadableDatabase();
        cursor = sqliteDatabase.query("fund_sum", null,
                "fundCode='"+fundCode+"'", null, null, null, null);
        if(cursor.moveToNext()){
            cv.put("fund_ProfitOrLossToday",cursor.getString(cursor.getColumnIndex("fund_ProfitOrLossToday")));
            cv.put("fund_ProfitOrLossSum",cursor.getString(cursor.getColumnIndex("fund_ProfitOrLossSum")));
            cv.put("fund_ProfitOrLossRate",cursor.getString(cursor.getColumnIndex("fund_ProfitOrLossRate")));
            cv.put("fund_MarketValue",cursor.getString(cursor.getColumnIndex("fund_MarketValue")));
            cv.put("fund_Position",cursor.getString(cursor.getColumnIndex("fund_Position")));
            cv.put("buyMoneySum",cursor.getString(cursor.getColumnIndex("buyMoneySum")));
        }
        cursor.close();
        sqliteDatabase.close();
        return cv;
    }

    /**
     * 计算基金总市值
     * @return
     */
    public static String queryFundMarketValueSum(){
        String result = "";
        sqliteDatabase = dbHelper.getReadableDatabase();
        cursor = sqliteDatabase.query("fund_sum", null,
                null, null, null, null, null);
        Double mvs = 0.0;
        while (cursor.moveToNext()){
            mvs += Double.parseDouble(cursor.getString(cursor.getColumnIndex("fund_MarketValue")));
        }
        result = String.format("%.2f",mvs);
        return result;
    }

    /**
     * 计算基金总盈亏
     * @return
     */
    public static String queryFundProfitLossSum(){
        String result = "";
        sqliteDatabase = dbHelper.getReadableDatabase();
        cursor = sqliteDatabase.query("fund_sum", null,
                null, null, null, null, null);
        Double pls = 0.0;
        while (cursor.moveToNext()){
            pls += Double.parseDouble(cursor.getString(cursor.getColumnIndex("fund_ProfitOrLossSum")));
        }
        result = String.format("%.2f",pls);
        return result;
    }

    /**
     * 计算基金总盈亏幅度
     * @return
     */
    public static String queryFundProfitLossSumRate(){
        String result = "";
        sqliteDatabase = dbHelper.getReadableDatabase();
        cursor = sqliteDatabase.query("fund_sum", null,
                null, null, null, null, null);
        //累计盈亏
        Double pls = 0.0;
        //本金
        Double bms = 0.0;
        while (cursor.moveToNext()){
            pls += Double.parseDouble(cursor.getString(cursor.getColumnIndex("fund_ProfitOrLossSum")));
            bms += Double.parseDouble(cursor.getString(cursor.getColumnIndex("buyMoneySum")));
        }
        result = String.format("%.2f",pls/bms);
        return result;
    }

    /**
     * 计算基金今日盈亏总和
     * @return
     */
    public static String queryFundProfitLossToday(){
        String result = "";
        sqliteDatabase = dbHelper.getReadableDatabase();
        cursor = sqliteDatabase.query("fund_sum", null,
                null, null, null, null, null);
        Double plt = 0.0;
        while (cursor.moveToNext()){
            plt += Double.parseDouble(cursor.getString(cursor.getColumnIndex("fund_ProfitOrLossToday")));
        }
        result = String.format("%.2f",plt);
        return result;
    }

    /**
     * 计算基金今日盈亏总幅度
     * @return
     */
    public static String queryFundProfitLossTodayRate(){
        String result = "";
        sqliteDatabase = dbHelper.getReadableDatabase();
        cursor = sqliteDatabase.query("fund_sum", null,
                null, null, null, null, null);
        //累计盈亏
        Double plt = 0.0;
        //本金
        Double bms = 0.0;
        while (cursor.moveToNext()){
            plt += Double.parseDouble(cursor.getString(cursor.getColumnIndex("fund_ProfitOrLossToday")));
            bms += Double.parseDouble(cursor.getString(cursor.getColumnIndex("buyMoneySum")));
        }
        result = String.format("%.2f",plt/bms);
        return result;
    }

}
