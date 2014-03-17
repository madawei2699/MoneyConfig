package com.mdw.moneyconfig;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 基金购买记录实体类
 * @author zFish
 *
 */

public class FundBuyInfo {
    //基金代码
    String fundCode;
    //购买金额
    String buyMoney;
    //购买数量
    String buyAmount;
    //购买价格
    String buyPrice;
    //手续费
    String poundage;
    //购买日期
    String buyDate;
    //收费模式
    Integer fundInsuranceType;
    //费率
    String fundRate;

    public String getFundCode() {
        return fundCode;
    }

    public void setFundCode(String fundCode) {
        this.fundCode = fundCode;
    }

    public String getBuyMoney() {
        return buyMoney;
    }

    public void setBuyMoney(String buyMoney) {
        this.buyMoney = buyMoney;
    }

    public String getBuyAmount() {
        return buyAmount;
    }

    public void setBuyAmount(String buyAmount) {
        this.buyAmount = buyAmount;
    }

    public String getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(String buyPrice) {
        this.buyPrice = buyPrice;
    }

    public String getPoundage() {
        return poundage;
    }

    public void setPoundage(String poundage) {
        this.poundage = poundage;
    }

    public String getBuyDate() {
        return buyDate;
    }

    public void setBuyDate(String buyDate) {
        this.buyDate = buyDate;
    }

    public Integer getFundInsuranceType() {
        return fundInsuranceType;
    }

    public void setFundInsuranceType(Integer fundInsuranceType) {
        this.fundInsuranceType = fundInsuranceType;
    }

    public String getFundRate() {
        return fundRate;
    }

    public void setFundRate(String fundRate) {
        this.fundRate = fundRate;
    }
}
