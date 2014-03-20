package com.mdw.moneyconfig.database.model.fund;

/**
 * 基金概览实体类
 * @author zFish
 *
 */

public class FundSum {
    //基金代码
    String fundCode;
    //该基金本金
    String buyMoneySum;
    //持仓
    String fund_Position;
    //今日盈亏
    String fund_ProfitOrLossToday;
    //累计盈亏
    String fund_ProfitOrLossSum;
    //盈亏幅度
    String fund_ProfitOrLossRate;
    //市值
    String fund_MarketValue;
    //止损
    String stopLoss;
    //止盈
    String stopProfit;

    public String getFundCode() {
        return fundCode;
    }

    public void setFundCode(String fundCode) {
        this.fundCode = fundCode;
    }

    public String getBuyMoneySum() {
        return buyMoneySum;
    }

    public void setBuyMoneySum(String buyMoneySum) {
        this.buyMoneySum = buyMoneySum;
    }

    public String getFund_Position() {
        return fund_Position;
    }

    public void setFund_Position(String fund_Position) {
        this.fund_Position = fund_Position;
    }

    public String getFund_ProfitOrLossToday() {
        return fund_ProfitOrLossToday;
    }

    public void setFund_ProfitOrLossToday(String fund_ProfitOrLossToday) {
        this.fund_ProfitOrLossToday = fund_ProfitOrLossToday;
    }

    public String getFund_ProfitOrLossSum() {
        return fund_ProfitOrLossSum;
    }

    public void setFund_ProfitOrLossSum(String fund_ProfitOrLossSum) {
        this.fund_ProfitOrLossSum = fund_ProfitOrLossSum;
    }

    public String getFund_ProfitOrLossRate() {
        return fund_ProfitOrLossRate;
    }

    public void setFund_ProfitOrLossRate(String fund_ProfitOrLossRate) {
        this.fund_ProfitOrLossRate = fund_ProfitOrLossRate;
    }

    public String getFund_MarketValue() {
        return fund_MarketValue;
    }

    public void setFund_MarketValue(String fund_MarketValue) {
        this.fund_MarketValue = fund_MarketValue;
    }

    public String getStopLoss() {
        return stopLoss;
    }

    public void setStopLoss(String stopLoss) {
        this.stopLoss = stopLoss;
    }

    public String getStopProfit() {
        return stopProfit;
    }

    public void setStopProfit(String stopProfit) {
        this.stopProfit = stopProfit;
    }
}
