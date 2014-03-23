package com.mdw.moneyconfig.fund;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.mdw.moneyconfig.utils.Constant;
import com.mdw.moneyconfig.utils.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Iterator;

/**
 * 数据查询类，从新浪财经接口获取基金基本信息数据
 * @author zFish
 *
 */

public class FundInfoWebService implements Runnable {

    private Handler handler;
    // 查询数据地址
    private String fundInfoURL;
    private String fundInvestmentURL;

    public FundInfoWebService(String fundInfoURL, String fundInvestmentURL, Handler handler){
        this.fundInfoURL = fundInfoURL;
        this.fundInvestmentURL = fundInvestmentURL;
        this.handler = handler;
    }

    @Override
    public void run() {

        if(handler != null){
            Message m = new Message();
            Bundle b = new Bundle();
            m.what = Constant.SEARCHSERVICEOK;
            try {
                Document docInfo = Jsoup.connect(fundInfoURL).get();
                Document docInvestment = Jsoup.connect(fundInvestmentURL).get();
                //获取基金费率表格
                Elements fundInfoTable = docInfo.getElementsByClass("tableContainer");
                fundInfoTable.attr("style","width:550px;");
                fundInfoTable.select("table").attr("style","width:550px;");
                fundInfoTable.select("a").removeAttr("href");
                //转成HTML
                String htmlData = fundInfoTable.outerHtml();

                //获取基金收益表格
                Elements fundPerformanceTable = docInvestment.getElementsByAttributeValue("id", "box-fund-performance-rank");
                fundPerformanceTable.attr("width","600px");
                fundPerformanceTable.select("table").attr("width","600px");
                String htmlPerformanceData = fundPerformanceTable.outerHtml();
                //获取基金投资组合表格
                Elements fundInvestmentTable = docInvestment.getElementsByAttributeValue("id", "table-investment-association");
                Integer i = fundInvestmentTable.select("iframe").size();
                String s;
                for(int k =0;k<i;k++){
                    s = fundInvestmentTable.select("iframe").get(k).attr("src").toString();
                    fundInvestmentTable.select("iframe").get(k).attr("src", Utils.getPropertiesURL("fundInfoBaseURLWeb") + s);
                }
                fundInvestmentTable.select("label").remove();
                String htmlInvestmentData = fundInvestmentTable.outerHtml();

                htmlData = "<html><head>"+"<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />"
                        +"<link rel=\"stylesheet\" type=\"text/css\" href=\"base.css\" />"
                        +"<script type=\"text/javascript\" src=\"http://finance.sina.com.cn/xincaifund/temp/js/stockhq.js\"></script>"
                        +"<script type=\"text/javascript\" src=\"http://finance.sina.com.cn/xincaifund/temp/js/config.js\"></script>"
                        +"<script type=\"text/javascript\" src=\"http://finance.sina.com.cn/xincaifund/temp/js/view.js\"></script>"
                        +"<script type=\"text/javascript\" src=\"http://finance.sina.com.cn/xincaifund/temp/js/modular.js\"></script>"
                        +"<script type=\"text/javascript\" src=\"http://finance.sina.com.cn/xincaifund/temp/js/control.js\"></script>"
                        +"<script type=\"text/javascript\" src=\"http://finance.sina.com.cn/xincaifund/temp/js/jquery.tmpl.min.js\"></script>"
                        +"<script type=\"text/javascript\" src=\"http://finance.sina.com.cn/xincaifund/temp/js/jquery.plugin.js\"></script>"
                        +"<meta http-equiv=\"Content-Type\" content=\"text/html;charset=gb2312\"> "
                        + "</head><body>" + htmlData + htmlPerformanceData + htmlInvestmentData
                        + "<script>\n" +
                        "jQuery(function(){\n" +
                        "\tcontrol.init();\n" +
                        "});\n" +
                        "</script>" +
                        "</body></html>";
                b.putString("html",htmlData);
                m.setData(b);
            } catch (IOException e){
                e.printStackTrace();
            }
            handler.sendMessage(m);// 执行耗时的方法之后发送消给handler
        }

    }

}