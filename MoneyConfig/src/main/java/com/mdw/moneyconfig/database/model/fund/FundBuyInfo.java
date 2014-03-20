package com.mdw.moneyconfig.database.model.fund;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * 基金购买记录实体类
 * @author zFish
 *
 */

public class FundBuyInfo implements Comparable<FundBuyInfo>{
    //id
    Integer id;
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
    //赎回数量
    String redeemAmount;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRedeemAmount() {
        return redeemAmount;
    }

    public void setRedeemAmount(String redeemAmount) {
        this.redeemAmount = redeemAmount;
    }

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

    @Override
    public int compareTo(FundBuyInfo fundBuyInfo) {
        DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        int i=0;
        try {
            i = format1.parse(this.buyDate).compareTo(format1.parse(fundBuyInfo.getBuyDate()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return i;
    }
}
