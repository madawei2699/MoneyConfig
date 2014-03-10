package com.mdw.moneyconfig;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * 数据查询类，从新浪财经接口获取基金费率数据
 * @author zFish
 *
 */

public class SearchService implements Runnable {

    private Handler handler;
    // 查询数据地址
    private String url;

    public SearchService(String url, Handler handler){
        this.url = url;
        this.handler = handler;
    }

    @Override
    public void run() {

        if(handler != null){
            Message m = new Message();
            Bundle b = new Bundle();
            m.what = Constant.SEARCHSERVICEOK;
            try {
                Document doc = Jsoup.connect(url).get();
                //获取基金费率表格
                Elements fundRateTable = doc.getElementsByClass("tableContainer");
                fundRateTable.attr("style","width:550px;");
                fundRateTable.select("table").attr("style","width:550px;");
                //转成HTML
                String htmlData = fundRateTable.outerHtml();
                htmlData = "<html><head>"+"<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />"
                        +"<meta http-equiv=\"Content-Type\" content=\"text/html;charset=gb2312\"> "
                        + "</head><body>" + htmlData +"</body></html>";
                b.putString("html",htmlData);
                m.setData(b);
            } catch (IOException e){
                e.printStackTrace();
            }
            handler.sendMessage(m);// 执行耗时的方法之后发送消给handler
        }

    }

}