package com.mdw.moneyconfig;

/**
 * 基金实时数据实体类
 * @author zFish
 *
 */

public class FundBase {
    //基金代码
    String fundCode;
    //基金名称
    String name;
    //基金现价
    String price;
    //涨跌
    String updown;
    //涨跌幅
    String scope;
    //日期
    String date;

    public String getFundCode() {
        return fundCode;
    }

    public void setFundCode(String fundCode) {
        this.fundCode = fundCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getUpdown() {
        return updown;
    }

    public void setUpdown(String updown) {
        this.updown = updown;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
