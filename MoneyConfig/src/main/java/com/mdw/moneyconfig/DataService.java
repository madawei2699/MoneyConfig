package com.mdw.moneyconfig;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.http.AndroidHttpClient;
import android.os.Message;

import com.mdw.moneyconfig.Utils;

/**
 * 数据处理类，从新浪财经接口获取股票基金数据存储至SQLite中
 * @author zFish
 *
 */

public class DataService implements Runnable {

    private android.os.Handler handler;

	//基金抓取数据
    private String []fundData;
    //股票抓取数据
    private String []stockData;
    //基金代码
    private String fundCode="";
    //股票代码
    private String stockCode="";
    //是否更新股票基金数据
    private boolean updateOrNot=false;
    // 创建ContentValues对象
    ContentValues values = new ContentValues();
    // 创建了一个DatabaseHelper对象，只执行这句话是不会创建或打开连接的
    DatabaseHelper dbHelper;

    public DataService(String code) {
    	//如果代码包含of则为开放式基金，如果包含sz或sh则为股票
    	if(code.contains("of")){
    		this.fundCode = code;
    	}else if(code.contains("sz") || code.contains("sh")){
    		this.stockCode = code;
    	}
        dbHelper = new DatabaseHelper(MyApplication.getInstance(), "moneyconfig_db");
    }

    public DataService(ContentValues values) {
        //如果代码包含of则为开放式基金，如果包含sz或sh则为股票
        this.values = values;
        this.fundCode = values.getAsString("fundCode");
        dbHelper = new DatabaseHelper(MyApplication.getInstance(), "moneyconfig_db");
    }

    public DataService(ContentValues values, android.os.Handler handler) {
        //如果代码包含of则为开放式基金，如果包含sz或sh则为股票
        this.values = values;
        this.fundCode = values.getAsString("fundCode");
        this.handler = handler;
        dbHelper = new DatabaseHelper(MyApplication.getInstance(), "moneyconfig_db");
    }

    //更新基金股票数据
    public DataService(android.os.Handler handler){
        this.handler = handler;
    	this.updateOrNot=true;
        dbHelper = new DatabaseHelper(MyApplication.getInstance(), "moneyconfig_db");
    }

    @Override
    public void run() {

        if(!fundCode.equals("")){
    		//基金代码不为空，则插入或更新基金数据
    		getAndStoreFundData(fundCode);
    	}else if(!stockCode.equals("")){
    		//股票代码不为空，则插入或更新基金数据
    		getAndStoreStockData(stockCode);
    	}else if(updateOrNot){
            SQLiteDatabase sqliteDatabase = dbHelper.getWritableDatabase();
            Cursor cursor = sqliteDatabase.rawQuery("select fundCode from fund_base", null);
            if(0!=cursor.getCount()){
            	// 将光标移动到下一行，从而判断该结果集是否还有下一条数据，如果有则返回true，没有则返回false  
                while (cursor.moveToNext()) {  
                	getAndStoreFundData(cursor.getString(cursor.getColumnIndex("fundCode")));
                }
            }
    	}
        if(handler != null){
            Message m = new Message();
            m.what = Constant.DATASERVICEOK;
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
                }
                sqliteDatabase.close();
            }
    	}
    }
    /**
     * 获取并存储股票数据
     * @return
     */
    public void getAndStoreStockData(String code){
    	
    }
}