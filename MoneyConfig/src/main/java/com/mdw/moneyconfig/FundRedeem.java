package com.mdw.moneyconfig;

/**
 * 基金赎回记录实体类
 * @author zFish
 *
 */

public class FundRedeem {
    //基金代码
    String fundCode;
    //赎回价格
    String redeemPrice;
    //赎回数量
    String redeemAmount;
    //赎回费率
    String fundRedeemRate;
    //赎回日期
    String redeemDate;
    //收费模式
    Integer fundInsuranceType;
    //手续费
    String poundage;
    //赎回金额
    String redeemMoney;

    public String getFundCode() {
        return fundCode;
    }

    public void setFundCode(String fundCode) {
        this.fundCode = fundCode;
    }

    public String getRedeemPrice() {
        return redeemPrice;
    }

    public void setRedeemPrice(String redeemPrice) {
        this.redeemPrice = redeemPrice;
    }

    public String getRedeemAmount() {
        return redeemAmount;
    }

    public void setRedeemAmount(String redeemAmount) {
        this.redeemAmount = redeemAmount;
    }

    public String getFundRedeemRate() {
        return fundRedeemRate;
    }

    public void setFundRedeemRate(String fundRedeemRate) {
        this.fundRedeemRate = fundRedeemRate;
    }

    public String getRedeemDate() {
        return redeemDate;
    }

    public void setRedeemDate(String redeemDate) {
        this.redeemDate = redeemDate;
    }

    public Integer getFundInsuranceType() {
        return fundInsuranceType;
    }

    public void setFundInsuranceType(Integer fundInsuranceType) {
        this.fundInsuranceType = fundInsuranceType;
    }

    public String getPoundage() {
        return poundage;
    }

    public void setPoundage(String poundage) {
        this.poundage = poundage;
    }

    public String getRedeemMoney() {
        return redeemMoney;
    }

    public void setRedeemMoney(String redeemMoney) {
        this.redeemMoney = redeemMoney;
    }
}
