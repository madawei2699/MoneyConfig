package com.mdw.moneyconfig;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
    //基金抓取数据
    private String []fundData;
    // 创建ContentValues对象
    ContentValues values = new ContentValues();
    // 创建了一个DatabaseHelper对象，只执行这句话是不会创建或打开连接的
    DatabaseHelper dbHelper;


    public FundPriceService(String fundCode,String fundDate, Handler handler){
        this.fundCode = fundCode;
        this.fundDate = fundDate;
        this.handler = handler;
        dbHelper = new DatabaseHelper(MyApplication.getInstance(),
                "moneyconfig_db", 2);
    }

    @Override
    public void run() {
        if(handler != null){
            Message m = new Message();
            Bundle b = new Bundle();
            m.what = Constant.FUNDPRICEOK;
            if((!fundCode.equals(""))&&(!fundDate.equals(""))&&Utils.detectNetwork()){
                //基金代码不为空，则插入或更新基金数据
                getAndStoreFundData(fundCode);
                //查询基金历史净值
                b.putString("jjjz",getFundPrice());
            }
            m.setData(b);
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

    /**
     * 获取并存储基金数据,如果code不为空且网络可用则发送http请求获取数据
     * @return
     */
    public void getAndStoreFundData(String code){
        if((!code.equals(""))&&Utils.detectNetwork()){
            // 设置访问的Web站点
            String path = Utils.getPropertiesURL("url") + code;
            //设置Http请求参数
            Map<String, String> params = new HashMap<String, String>();
            String result = sendHttpClientPost(path, params, Utils.getPropertiesURL("encode"));
            //检查数据是否为空
            String data = result.split("=")[1].replaceAll("\"", "").replaceAll(";", "").replaceAll("\\n","");
            if(!data.equals(""))
            {
                fundData = data.split(",");

                // 向该对象中插入键值对，其中键是列名，值是希望插入到这一列的值，值必须和数据库当中的数据类型一致
                values.put("price", fundData[1]);
                // 保留四位小数
                values.put("updown", String.format("%1$.4f", Double.parseDouble(fundData[1]) -
                        Double.parseDouble(fundData[3])));
                values.put("scope", fundData[4]);
                values.put("date", fundData[5]);
                // 只有调用了DatabaseHelper的getWritableDatabase()方法或者getReadableDatabase()方法之后，才会创建或打开一个连接
                SQLiteDatabase sqliteDatabase = dbHelper.getWritableDatabase();
                Cursor cursor = sqliteDatabase.rawQuery("select fundCode from fund_base where fundCode='" + code + "'", null);
                // 更新基金数据
                if(0!=cursor.getCount()){
                    values.put("name", fundData[0]);
                    //更新基金数据
                    sqliteDatabase.update("fund_base", values, "fundCode="+"'"+code+"'", null);
                }else {
                    values.put("name", fundData[0]);
                    values.put("fundCode", code);
                    //插入基金数据
                    sqliteDatabase.insert("fund_base",null,values);
                }
                sqliteDatabase.close();
            }
        }
    }

    /**
     * 获取基金指定日期历史净值
     * @return
     */
    public String getFundPrice(){
        String jjjz = "";
        // 设置访问的Web站点
        String path = Utils.getPropertiesURL("SearchPriceByDate").replaceAll("fundCode",fundCode.replace("of","")).replaceAll("fundDate",fundDate);
        //设置Http请求参数
        Map<String, String> params = new HashMap<String, String>();
        String result = sendHttpClientPost(path, params, Utils.getPropertiesURL("encode"));
        //解析JSON格式数据并检查数据是否为空
        JSONObject jo = null;
        try {
            jo = new JSONObject(result.replaceAll("\"","\\\""));
            JSONObject jo_result = (JSONObject)jo.get("result");
            JSONObject jo_data = (JSONObject)jo_result.get("data");
            if(jo_data.get("total_num").toString().equals("1")){
                JSONArray ja_data = (JSONArray) jo_data.get("data");
                JSONObject o = (JSONObject)ja_data.get(0);
                jjjz = o.getString("jjjz");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jjjz;
    }
}