package com.mdw.moneyconfig;

import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据查询类，从新浪财经接口获取基金历史净值数据
 * @author zFish
 *
 */

public class FundPriceService implements Runnable {

    private Handler handler;
    // 基金代码
    private String fundCode;
    // 净值日期
    private String fundDate;


    public FundPriceService(String fundCode,String fundDate, Handler handler){
        this.fundCode = fundCode;
        this.fundDate = fundDate;
        this.handler = handler;
    }

    @Override
    public void run() {

        if(handler != null){
            Message m = new Message();
            Bundle b = new Bundle();
            m.what = Constant.FUNDPRICEOK;
            try {
                if((!fundCode.equals(""))&&(!fundDate.equals(""))&&Utils.detectNetwork()){
                    // 设置访问的Web站点
                    String path = Utils.getPropertiesURL("SearchPriceByDate").replaceAll("fundCode",fundCode).replaceAll("fundDate",fundDate);
                    //设置Http请求参数
                    Map<String, String> params = new HashMap<String, String>();
                    String result = sendHttpClientPost(path, params, Utils.getPropertiesURL("encode"));
                    //检查数据是否为空
                    JSONObject jo = new JSONObject(result);
                    if(jo.get("total_num").toString().equals("1")){
                        b.putString("jjjz",jo.get("jjjz").toString());
                    }
                }
                m.setData(b);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            handler.sendMessage(m);// 执行耗时的方法之后发送消给handler
        }

    }

    /**
     * 发送Http请求到Web站点
     * @param path Web站点请求地址
     * @param map Http请求参数
     * @param encode 编码格式
     * @return Web站点响应的字符串
     */
    private String sendHttpClientPost(String path,Map<String, String> map,String encode)
    {
        List<NameValuePair> list=new ArrayList<NameValuePair>();
        //实例化一个默认的Http客户端，使用的是AndroidHttpClient
        AndroidHttpClient client=AndroidHttpClient.newInstance("");
        if(map!=null&&!map.isEmpty())
        {
            for(Map.Entry<String, String> entry:map.entrySet())
            {
                //解析Map传递的参数，使用一个键值对对象BasicNameValuePair保存。
                list.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
        }
        try {
            //实现将请求 的参数封装封装到HttpEntity中。
            UrlEncodedFormEntity entity=new UrlEncodedFormEntity(list, encode);
            //使用HttpPost请求方式
            HttpPost httpPost=new HttpPost(path);
            //设置请求参数到Form中。
            httpPost.setEntity(entity);
            //执行请求，并获得响应数据
            HttpResponse httpResponse= client.execute(httpPost);
            //判断是否请求成功，为200时表示成功，其他均问有问题。
            if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
            {
                //通过HttpEntity获得响应流
                InputStream inputStream=httpResponse.getEntity().getContent();
                return changeInputStream(inputStream,encode);
            }
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally{
            //关闭连接
            client.close();
        }

        return "";
    }
    /**
     * 把Web站点返回的响应流转换为字符串格式
     * @param inputStream 响应流
     * @param encode 编码格式
     * @return 转换后的字符串
     */
    private  String changeInputStream(InputStream inputStream,
                                      String encode) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;
        String result="";
        if (inputStream != null) {
            try {
                while ((len = inputStream.read(data)) != -1) {
                    outputStream.write(data,0,len);
                }
                result=new String(outputStream.toByteArray(),encode);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

}